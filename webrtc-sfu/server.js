require('dotenv').config();

const { randomUUID } = require('crypto');
const cors = require('cors');
const express = require('express');
const http = require('http');
const jwt = require('jsonwebtoken');
const mediasoup = require('mediasoup');
const { Server } = require('socket.io');

const PORT = Number(process.env.PORT || 3000);
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-please-change-it-in-production-2026';
const ANNOUNCED_IP = process.env.ANNOUNCED_IP || '127.0.0.1';
const RTC_MIN_PORT = Number(process.env.RTC_MIN_PORT || 2000);
const RTC_MAX_PORT = Number(process.env.RTC_MAX_PORT || 2100);
const REJOIN_GRACE_MS = Number(process.env.REJOIN_GRACE_MS || 30000);

const app = express();
const httpServer = http.createServer(app);
const io = new Server(httpServer, { cors: { origin: '*' } });

app.use(cors({ origin: '*' }));
app.use(express.json());

let worker;
const rooms = new Map();
const peers = new Map();
const invitations = new Map();
const userSockets = new Map();
const onlineUsers = new Map();
const participantCleanupTimers = new Map();

const mediaCodecs = [
  {
    kind: 'audio',
    mimeType: 'audio/opus',
    clockRate: 48000,
    channels: 2
  },
  {
    kind: 'video',
    mimeType: 'video/VP8',
    clockRate: 90000,
    parameters: { 'x-google-start-bitrate': 1000 }
  }
];

app.get('/healthz', (_req, res) => {
  res.json({ ok: true, rooms: rooms.size, online: onlineUsers.size });
});

function toId(value) {
  return String(value || '').trim();
}

function emitToUser(userId, eventName, payload) {
  const sockets = userSockets.get(toId(userId));
  if (!sockets) return;

  for (const socketId of sockets) {
    const targetSocket = io.sockets.sockets.get(socketId);
    targetSocket?.emit(eventName, payload);
  }
}

function emitToRoomPeers(roomId, eventName, payload, excludedSocketId) {
  const room = rooms.get(roomId);
  if (!room) return;

  for (const peerId of room.peers) {
    if (peerId === excludedSocketId) continue;
    const peer = peers.get(peerId);
    peer?.socket.emit(eventName, payload);
  }
}

function ensureRoomParticipantNames(room) {
  if (!room.participantNames) {
    room.participantNames = new Map();
  }
  return room.participantNames;
}

function getRoomMembersSnapshot(room) {
  const participantNames = ensureRoomParticipantNames(room);
  const joinedUserIds = new Set();

  for (const peerId of room.peers) {
    const peer = peers.get(peerId);
    if (peer) joinedUserIds.add(peer.user.id);
  }

  return [...room.participants].map((userId) => ({
    id: userId,
    username: participantNames.get(userId) || onlineUsers.get(userId)?.username || `user-${userId}`,
    nickname: participantNames.get(userId) || onlineUsers.get(userId)?.username || `user-${userId}`,
    isCreator: room.creatorUserId === userId,
    isJoined: joinedUserIds.has(userId)
  }));
}

function emitRoomMembers(roomId) {
  const room = rooms.get(roomId);
  if (!room) return;

  emitToRoomPeers(roomId, 'roomMembersUpdate', {
    roomId,
    members: getRoomMembersSnapshot(room)
  });
}

async function ensureRoomRouter(room) {
  if (room.router) return room.router;
  room.router = await worker.createRouter({ mediaCodecs });
  return room.router;
}

async function createWebRtcTransport(router) {
  return router.createWebRtcTransport({
    listenIps: [
      {
        ip: '0.0.0.0',
        announcedIp: ANNOUNCED_IP
      }
    ],
    enableUdp: true,
    enableTcp: true,
    preferUdp: true
  });
}

function getOrCreateRoom(roomId, creatorUser, roomName, roomType = 'direct') {
  const effectiveRoomId = roomId || randomUUID().split('-')[0];
  let room = rooms.get(effectiveRoomId);

  if (!room) {
    room = {
      id: effectiveRoomId,
      name: roomName || '视频通话',
      type: roomType,
      router: null,
      creatorUserId: creatorUser.id,
      participants: new Set([creatorUser.id]),
      participantNames: new Map([[creatorUser.id, creatorUser.nickname || creatorUser.username]]),
      peers: new Set()
    };
    rooms.set(effectiveRoomId, room);
  }

  if (!room.creatorUserId) {
    room.creatorUserId = creatorUser.id;
  }
  if (roomName) {
    room.name = roomName;
  }
  ensureRoomParticipantNames(room).set(creatorUser.id, creatorUser.nickname || creatorUser.username);
  room.participants.add(creatorUser.id);

  return room;
}

function destroyRoom(roomId) {
  const room = rooms.get(roomId);
  if (!room) return;

  for (const timerKey of [...participantCleanupTimers.keys()]) {
    if (timerKey.startsWith(`${roomId}:`)) {
      clearTimeout(participantCleanupTimers.get(timerKey));
      participantCleanupTimers.delete(timerKey);
    }
  }

  room.router?.close();
  rooms.delete(roomId);
}

function cleanupEmptyRoom(roomId) {
  const room = rooms.get(roomId);
  if (!room || room.peers.size > 0) return;

  const hasPendingInvite = [...invitations.values()].some(
    (invite) => invite.roomId === roomId && invite.status === 'pending'
  );
  if (!hasPendingInvite) destroyRoom(roomId);
}

function cleanupPeer(socketId, options = {}) {
  const peer = peers.get(socketId);
  if (!peer) return;

  const {
    notifyPeerLeft = false,
    leftReason = '离开了通话',
    removeParticipant = true,
    skipEmptyRoomCleanup = false
  } = options;

  const room = rooms.get(peer.roomId);
  if (room) {
    room.peers.delete(socketId);
    if (removeParticipant) {
      room.participants.delete(peer.user.id);
      ensureRoomParticipantNames(room).delete(peer.user.id);
      if (room.creatorUserId === peer.user.id) {
        room.creatorUserId = [...room.participants][0] || '';
      }
    }
  }

  for (const consumer of peer.consumers.values()) consumer.close();
  for (const producer of peer.producers.values()) producer.close();
  for (const transport of peer.transports.values()) transport.close();

  if (room && notifyPeerLeft) {
    emitToRoomPeers(peer.roomId, 'peerLeft', {
      roomId: peer.roomId,
      user: peer.user,
      reason: leftReason
    });
  }

  if (room) emitRoomMembers(peer.roomId);
  if (room && !skipEmptyRoomCleanup) cleanupEmptyRoom(peer.roomId);
  peers.delete(socketId);
}

function scheduleParticipantCleanup(roomId, user) {
  const key = `${roomId}:${user.id}`;
  clearTimeout(participantCleanupTimers.get(key));

  const timer = setTimeout(() => {
    participantCleanupTimers.delete(key);
    const room = rooms.get(roomId);
    if (!room) return;

    const stillJoined = [...room.peers].some((peerId) => peers.get(peerId)?.user.id === user.id);
    if (stillJoined) return;

    room.participants.delete(user.id);
    ensureRoomParticipantNames(room).delete(user.id);
    emitRoomMembers(roomId);
    cleanupEmptyRoom(roomId);
  }, REJOIN_GRACE_MS);

  timer.unref?.();
  participantCleanupTimers.set(key, timer);
}

function endRoom(roomId, reason, endedByUserId = null) {
  const room = rooms.get(roomId);
  if (!room) return;

  const payload = { roomId, reason, endedByUserId };
  for (const participantUserId of room.participants) {
    if (participantUserId !== endedByUserId) {
      emitToUser(participantUserId, 'roomEnded', payload);
    }
  }

  for (const invite of [...invitations.values()]) {
    if (invite.roomId === roomId) {
      invitations.delete(invite.id);
    }
  }

  for (const peerId of [...room.peers]) {
    cleanupPeer(peerId, { removeParticipant: true, skipEmptyRoomCleanup: true });
  }
  destroyRoom(roomId);
}

function createInvite(room, fromUser, targetUserId, callType) {
  const normalizedTargetUserId = toId(targetUserId);
  if (!normalizedTargetUserId || normalizedTargetUserId === fromUser.id) return null;

  room.participants.add(normalizedTargetUserId);
  const inviteId = randomUUID();
  const invite = {
    id: inviteId,
    roomId: room.id,
    roomName: room.name,
    roomType: room.type,
    fromUser,
    toUserId: normalizedTargetUserId,
    callType,
    status: 'pending'
  };

  invitations.set(inviteId, invite);
  emitToUser(normalizedTargetUserId, 'incomingInvite', {
    inviteId,
    roomId: room.id,
    roomName: room.name,
    roomType: room.type,
    callType,
    fromUser
  });

  return invite;
}

io.use((socket, next) => {
  try {
    const rawToken = socket.handshake.auth?.token;
    if (!rawToken) return next(new Error('UNAUTHORIZED: missing token'));

    const token = String(rawToken).replace(/^Bearer\s+/i, '').replace(/"/g, '');
    const payload = jwt.verify(token, JWT_SECRET);
    const userId = toId(payload.userId || payload.sub);
    if (!userId) return next(new Error('UNAUTHORIZED: missing user id'));

    socket.user = {
      id: userId,
      username: payload.username || `user-${userId}`,
      nickname: payload.nickname || payload.username || `user-${userId}`,
      role: payload.role || 'user'
    };
    return next();
  } catch (err) {
    return next(new Error(`UNAUTHORIZED: ${err.message}`));
  }
});

io.on('connection', (socket) => {
  const user = socket.user;
  if (!userSockets.has(user.id)) userSockets.set(user.id, new Set());
  userSockets.get(user.id).add(socket.id);
  onlineUsers.set(user.id, user);

  for (const invite of invitations.values()) {
    if (invite.toUserId === user.id && invite.status === 'pending') {
      socket.emit('incomingInvite', {
        inviteId: invite.id,
        roomId: invite.roomId,
        roomName: invite.roomName,
        roomType: invite.roomType,
        callType: invite.callType,
        fromUser: invite.fromUser
      });
    }
  }

  socket.on('createRoomInvite', ({ targetUserId, roomId, roomName, callType = 'video' }, callback = () => {}) => {
    try {
      const room = getOrCreateRoom(toId(roomId), user, roomName || `${user.nickname}的视频通话`, 'direct');
      const invite = createInvite(room, user, targetUserId, callType);
      if (!invite) return callback({ error: 'targetUserId is invalid' });

      return callback({
        inviteId: invite.id,
        roomId: room.id,
        roomName: room.name,
        members: getRoomMembersSnapshot(room)
      });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('createGroupRoom', ({ roomId, roomName, participantIds = [], callType = 'video' }, callback = () => {}) => {
    try {
      const normalizedRoomId = toId(roomId);
      if (!normalizedRoomId) return callback({ error: 'roomId is required' });

      const room = getOrCreateRoom(normalizedRoomId, user, roomName || '群视频通话', 'group');
      const ids = new Set((Array.isArray(participantIds) ? participantIds : []).map(toId).filter(Boolean));
      ids.add(user.id);

      for (const participantId of ids) {
        room.participants.add(participantId);
        if (participantId !== user.id) {
          createInvite(room, user, participantId, callType);
        }
      }

      return callback({
        roomId: room.id,
        roomName: room.name,
        members: getRoomMembersSnapshot(room)
      });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('respondRoomInvite', ({ inviteId, accept }, callback = () => {}) => {
    try {
      const invite = invitations.get(toId(inviteId));
      if (!invite || invite.status !== 'pending') {
        return callback({ error: 'invite not found or already handled' });
      }
      if (invite.toUserId !== user.id) {
        return callback({ error: 'invite does not belong to current user' });
      }

      invitations.delete(invite.id);
      invite.status = accept ? 'accepted' : 'rejected';
      emitToUser(invite.fromUser.id, 'inviteResponded', {
        inviteId: invite.id,
        accepted: Boolean(accept),
        roomId: invite.roomId,
        by: user
      });

      if (!accept) {
        cleanupEmptyRoom(invite.roomId);
        return callback({ ok: true, accepted: false });
      }

      const room = rooms.get(invite.roomId);
      if (!room) return callback({ error: 'room no longer exists' });

      room.participants.add(user.id);
      ensureRoomParticipantNames(room).set(user.id, user.nickname || user.username);
      emitRoomMembers(invite.roomId);

      return callback({
        ok: true,
        accepted: true,
        roomId: invite.roomId,
        roomName: room.name,
        roomType: room.type
      });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('joinRoom', async ({ roomId }, callback = () => {}) => {
    try {
      const normalizedRoomId = toId(roomId);
      const room = rooms.get(normalizedRoomId);
      if (!room) return callback({ error: 'room not found' });
      if (!room.participants.has(user.id)) return callback({ error: 'you are not invited to this room' });

      const existingPeer = peers.get(socket.id);
      if (existingPeer && existingPeer.roomId !== normalizedRoomId) {
        cleanupPeer(socket.id, { notifyPeerLeft: true, leftReason: '切换了通话' });
      }

      const router = await ensureRoomRouter(room);
      room.peers.add(socket.id);
      ensureRoomParticipantNames(room).set(user.id, user.nickname || user.username);
      peers.set(socket.id, {
        user,
        socket,
        roomId: normalizedRoomId,
        transports: new Map(),
        producers: new Map(),
        consumers: new Map()
      });

      const existingProducers = [];
      for (const peerId of room.peers) {
        if (peerId === socket.id) continue;
        const otherPeer = peers.get(peerId);
        if (!otherPeer) continue;

        for (const producer of otherPeer.producers.values()) {
          existingProducers.push({
            producerId: producer.id,
            kind: producer.kind,
            user: otherPeer.user
          });
        }
      }

      emitRoomMembers(normalizedRoomId);
      return callback({
        rtpCapabilities: router.rtpCapabilities,
        existingProducers,
        isCreator: room.creatorUserId === user.id,
        members: getRoomMembersSnapshot(room),
        roomName: room.name,
        roomType: room.type
      });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('leaveRoom', (_data, callback = () => {}) => {
    const peer = peers.get(socket.id);
    if (!peer) return callback({ error: 'peer not joined' });
    const roomId = peer.roomId;
    cleanupPeer(socket.id, { notifyPeerLeft: true });
    return callback({ ok: true, roomId });
  });

  socket.on('endRoom', (_data, callback = () => {}) => {
    const peer = peers.get(socket.id);
    if (!peer) return callback({ error: 'peer not joined' });
    endRoom(peer.roomId, `${user.nickname || user.username} 结束了通话`, user.id);
    return callback({ ok: true, roomId: peer.roomId });
  });

  socket.on('createWebRtcTransport', async ({ direction } = {}, callback = () => {}) => {
    try {
      const peer = peers.get(socket.id);
      if (!peer) return callback({ error: 'peer not joined' });

      const room = rooms.get(peer.roomId);
      if (!room) return callback({ error: 'room not found' });

      const router = await ensureRoomRouter(room);
      const transport = await createWebRtcTransport(router);
      transport.appData = { direction: direction === 'send' ? 'send' : 'recv' };
      peer.transports.set(transport.id, transport);

      return callback({
        id: transport.id,
        iceParameters: transport.iceParameters,
        iceCandidates: transport.iceCandidates,
        dtlsParameters: transport.dtlsParameters
      });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('connectTransport', async ({ transportId, dtlsParameters }, callback = () => {}) => {
    try {
      const peer = peers.get(socket.id);
      const transport = peer?.transports.get(transportId);
      if (!transport) return callback({ error: 'transport not found' });
      await transport.connect({ dtlsParameters });
      return callback({ ok: true });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('produce', async ({ transportId, kind, rtpParameters }, callback = () => {}) => {
    try {
      const peer = peers.get(socket.id);
      const transport = peer?.transports.get(transportId);
      if (!transport) return callback({ error: 'transport not found' });

      const producer = await transport.produce({
        kind,
        rtpParameters,
        appData: { user: peer.user }
      });
      peer.producers.set(producer.id, producer);

      producer.observer.on('close', () => {
        peer.producers.delete(producer.id);
        emitToRoomPeers(peer.roomId, 'producerClosed', { producerId: producer.id }, socket.id);
      });

      producer.on('transportclose', () => producer.close());

      emitToRoomPeers(peer.roomId, 'newProducer', {
        producerId: producer.id,
        kind: producer.kind,
        user: peer.user
      }, socket.id);

      return callback({ id: producer.id });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('pauseProducer', async ({ producerId }, callback = () => {}) => {
    try {
      const producer = peers.get(socket.id)?.producers.get(toId(producerId));
      if (!producer) return callback({ error: 'producer not found' });
      await producer.pause();
      return callback({ ok: true });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('resumeProducer', async ({ producerId }, callback = () => {}) => {
    try {
      const producer = peers.get(socket.id)?.producers.get(toId(producerId));
      if (!producer) return callback({ error: 'producer not found' });
      await producer.resume();
      return callback({ ok: true });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('consume', async ({ producerId, rtpCapabilities }, callback = () => {}) => {
    try {
      const peer = peers.get(socket.id);
      if (!peer) return callback({ error: 'peer not joined' });

      const room = rooms.get(peer.roomId);
      if (!room?.router) return callback({ error: 'room media router is not ready' });
      if (!room.router.canConsume({ producerId, rtpCapabilities })) {
        return callback({ error: 'cannot consume' });
      }

      let recvTransport = [...peer.transports.values()].find((transport) => transport.appData.direction === 'recv');
      if (!recvTransport) {
        recvTransport = await createWebRtcTransport(room.router);
        recvTransport.appData = { direction: 'recv' };
        peer.transports.set(recvTransport.id, recvTransport);
      }

      const consumer = await recvTransport.consume({
        producerId,
        rtpCapabilities,
        paused: true
      });
      peer.consumers.set(consumer.id, consumer);

      consumer.on('transportclose', () => peer.consumers.delete(consumer.id));
      consumer.on('producerclose', () => peer.consumers.delete(consumer.id));

      let producerUser = null;
      for (const roomPeerId of room.peers) {
        const roomPeer = peers.get(roomPeerId);
        if (!roomPeer) continue;
        const producer = roomPeer.producers.get(producerId);
        if (producer) {
          producerUser = roomPeer.user;
          break;
        }
      }

      return callback({
        transportOptions: {
          id: recvTransport.id,
          iceParameters: recvTransport.iceParameters,
          iceCandidates: recvTransport.iceCandidates,
          dtlsParameters: recvTransport.dtlsParameters
        },
        consumerOptions: {
          id: consumer.id,
          producerId,
          kind: consumer.kind,
          rtpParameters: consumer.rtpParameters
        },
        user: producerUser
      });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('resume', async ({ consumerId }, callback = () => {}) => {
    try {
      const consumer = peers.get(socket.id)?.consumers.get(toId(consumerId));
      if (!consumer) return callback({ error: 'consumer not found' });
      await consumer.resume();
      return callback({ ok: true });
    } catch (err) {
      return callback({ error: err.message });
    }
  });

  socket.on('disconnect', () => {
    const sockets = userSockets.get(user.id);
    if (sockets) {
      sockets.delete(socket.id);
      if (sockets.size === 0) {
        userSockets.delete(user.id);
        onlineUsers.delete(user.id);
      }
    }

    const disconnectedPeer = peers.get(socket.id);
    cleanupPeer(socket.id, {
      notifyPeerLeft: true,
      leftReason: '掉线了',
      removeParticipant: false,
      skipEmptyRoomCleanup: true
    });
    if (disconnectedPeer) {
      scheduleParticipantCleanup(disconnectedPeer.roomId, disconnectedPeer.user);
    }
  });
});

async function runMediasoup() {
  worker = await mediasoup.createWorker({
    logLevel: 'warn',
    rtcMinPort: RTC_MIN_PORT,
    rtcMaxPort: RTC_MAX_PORT
  });

  worker.on('died', () => {
    console.error('mediasoup worker died');
    process.exit(1);
  });
}

(async () => {
  await runMediasoup();
  httpServer.listen(PORT, () => {
    console.log(`WebRTC SFU listening on :${PORT}`);
    console.log(`mediasoup announced IP: ${ANNOUNCED_IP}, RTC ports: ${RTC_MIN_PORT}-${RTC_MAX_PORT}`);
  });
})();
