FROM node:22-bookworm AS builder
WORKDIR /app

# Use Aliyun mirror for faster apt downloads
RUN sed -i 's/deb.debian.org/mirrors.cloud.aliyuncs.com/g' /etc/apt/sources.list.d/debian.sources 2>/dev/null || \
    sed -i 's/deb.debian.org/mirrors.cloud.aliyuncs.com/g' /etc/apt/sources.list && \
    apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    make \
    g++ \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

COPY package.json package-lock.json ./

# Force mediasoup to compile from source so the binary is linked against
# bookworm's glibc 2.36 (the prebuilt binary targets glibc 2.38+).
ENV MEDIASOUP_SKIP_WORKER_PREBUILT_DOWNLOAD=true
RUN npm ci 2>&1

# Verify the worker binary was built and check its linkage
RUN test -f /app/node_modules/mediasoup/worker/out/Release/mediasoup-worker && \
    ldd /app/node_modules/mediasoup/worker/out/Release/mediasoup-worker 2>&1

FROM node:22-bookworm AS runtime
WORKDIR /app

COPY --from=builder /app/node_modules ./node_modules
COPY . .

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000/healthz', r => { process.exit(r.statusCode !== 200 ? 1 : 0) }).on('error', () => process.exit(1))"

CMD ["node", "server.js"]
