  <template>
  <el-dialog v-model="rtc.visible" :title="rtc.roomTitle" width="920px" :close-on-click-modal="false"
    :before-close="handleClose" class="rtc-dialog">
    <div v-if="rtc.isIncoming" class="incoming-panel">
      <el-avatar :size="56">{{ callerName.charAt(0) }}</el-avatar>
      <div class="incoming-title">{{ callerName }} 邀请你加入视频通话</div>
      <div class="incoming-subtitle">{{ rtc.roomTitle }}</div>
      <div class="incoming-actions">
        <el-button @click="rtc.rejectIncoming">拒绝</el-button>
        <el-button type="primary" @click="rtc.acceptIncoming">接听</el-button>
      </div>
    </div>

    <template v-else>
      <div class="rtc-stage">
        <div class="video-grid" :class="{ single: rtc.remoteVideos.length <= 1 }">
          <RtcMediaTile v-if="rtc.localStream" :stream="rtc.localStream" kind="video" muted local label="我" />
          <div v-else class="placeholder-tile">正在获取摄像头...</div>

          <RtcMediaTile v-for="media in rtc.remoteVideos" :key="media.producerId" :stream="media.stream" kind="video"
            :label="media.user?.nickname || media.user?.username || '成员'" />

          <div v-if="rtc.remoteVideos.length === 0" class="placeholder-tile">等待其他成员加入</div>
        </div>

        <div class="member-strip">
          <span v-for="member in rtc.members" :key="member.id" class="member-pill" :class="{ joined: member.isJoined }">
            {{ member.nickname || member.username || member.id }}
          </span>
        </div>

        <RtcMediaTile v-for="media in rtc.remoteAudios" :key="media.producerId" :stream="media.stream" kind="audio" />
      </div>

      <div class="status-line">{{ rtc.statusText }}</div>
    </template>

    <template #footer>
      <div v-if="!rtc.isIncoming" class="call-actions">
        <el-tooltip :content="rtc.isMicMuted ? '打开麦克风' : '关闭麦克风'" placement="top">
          <el-button :icon="Microphone" circle @click="rtc.toggleMic" />
        </el-tooltip>
        <el-tooltip :content="rtc.isCameraOff ? '打开摄像头' : '关闭摄像头'" placement="top">
          <el-button :icon="VideoCamera" circle @click="rtc.toggleCamera" />
        </el-tooltip>
        <el-tooltip content="离开通话" placement="top">
          <el-button :icon="Close" circle type="danger" @click="rtc.leaveCall" />
        </el-tooltip>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Close, Microphone, VideoCamera } from '@element-plus/icons-vue'
import { useRtcStore } from '@/stores/rtcStore'
import RtcMediaTile from './RtcMediaTile.vue'

const rtc = useRtcStore()

const callerName = computed(() => {
  const user = rtc.pendingInvite?.fromUser
  return user?.nickname || user?.username || '好友'
})

const handleClose = () => {
  if (rtc.isIncoming) {
    void rtc.rejectIncoming()
  } else {
    void rtc.leaveCall()
  }
}

onMounted(() => {
  try {
    rtc.ensureSocket()
  } catch {
    // 未登录时不需要提前连接，登录后发起或收到通话时会再连接。
  }
})
</script>

<style scoped>
.incoming-panel {
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.incoming-title {
  font-size: 18px;
  font-weight: 600;
}

.incoming-subtitle {
  color: #606266;
}

.incoming-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.rtc-stage {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
  min-height: 360px;
}

.video-grid.single {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.placeholder-tile {
  min-height: 180px;
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  background: #f3f6fa;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
}

.member-strip {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.member-pill {
  padding: 4px 8px;
  border-radius: 999px;
  background: #f2f3f5;
  color: #606266;
  font-size: 12px;
}

.member-pill.joined {
  background: #ecf5ff;
  color: #409eff;
}

.status-line {
  color: #606266;
  font-size: 13px;
  margin-top: 10px;
}

.call-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
