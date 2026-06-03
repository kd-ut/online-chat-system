<template>
  <div class="animated-bg">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>
    <div class="orb orb-4"></div>

    <div class="shape shape-1"></div>
    <div class="shape shape-2"></div>
    <div class="shape shape-3"></div>
    <div class="shape shape-4"></div>
    <div class="shape shape-5"></div>
    <div class="shape shape-6"></div>
    <div class="shape shape-7"></div>
    <div class="shape shape-8"></div>

    <div class="particle p-1"></div>
    <div class="particle p-2"></div>
    <div class="particle p-3"></div>
    <div class="particle p-4"></div>
    <div class="particle p-5"></div>
    <div class="particle p-6"></div>
    <div class="particle p-7"></div>
    <div class="particle p-8"></div>
    <div class="particle p-9"></div>
    <div class="particle p-10"></div>
    <div class="particle p-11"></div>
    <div class="particle p-12"></div>
  </div>
</template>

<style scoped>
.animated-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1040 30%, #2d1b69 60%, #1a1040 100%);
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  will-change: transform;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: #5b6abf;
  top: -15%;
  left: -10%;
  animation: orbFloat1 20s ease-in-out infinite;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: #8b9cf7;
  bottom: -10%;
  right: -5%;
  animation: orbFloat2 25s ease-in-out infinite;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: #e87461;
  top: 40%;
  right: 20%;
  animation: orbFloat3 18s ease-in-out infinite;
}

.orb-4 {
  width: 350px;
  height: 350px;
  background: #5b6abf;
  bottom: 20%;
  left: 15%;
  opacity: 0.3;
  animation: orbFloat4 22s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(100px, 80px) scale(1.1); }
  50% { transform: translate(50px, 150px) scale(0.95); }
  75% { transform: translate(-50px, 60px) scale(1.05); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(-80px, -60px) scale(1.08); }
  50% { transform: translate(-120px, -120px) scale(0.92); }
  75% { transform: translate(-30px, -80px) scale(1.05); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(60px, -40px) scale(1.12); }
  66% { transform: translate(-40px, 50px) scale(0.9); }
}

@keyframes orbFloat4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-60px, -50px) scale(1.1); }
  66% { transform: translate(40px, 40px) scale(0.93); }
}

.shape {
  position: absolute;
  border-radius: 50%;
  will-change: transform;
}

.shape-1 {
  width: 60px;
  height: 60px;
  border: 2px solid rgba(162, 155, 254, 0.3);
  top: 12%;
  left: 8%;
  animation: floatShape1 12s ease-in-out infinite;
}

.shape-2 {
  width: 40px;
  height: 40px;
  background: rgba(108, 92, 231, 0.15);
  top: 25%;
  right: 12%;
  animation: floatShape2 10s ease-in-out infinite;
}

.shape-3 {
  width: 50px;
  height: 50px;
  border: 2px solid rgba(253, 121, 168, 0.25);
  bottom: 30%;
  left: 20%;
  border-radius: 8px;
  animation: floatShape3 14s ease-in-out infinite;
}

.shape-4 {
  width: 35px;
  height: 35px;
  background: rgba(162, 155, 254, 0.1);
  bottom: 15%;
  right: 25%;
  animation: floatShape4 11s ease-in-out infinite;
}

.shape-5 {
  width: 45px;
  height: 45px;
  border: 2px solid rgba(108, 92, 231, 0.2);
  top: 55%;
  left: 5%;
  border-radius: 8px;
  animation: floatShape5 13s ease-in-out infinite;
}

.shape-6 {
  width: 30px;
  height: 30px;
  background: rgba(253, 121, 168, 0.12);
  top: 10%;
  right: 35%;
  animation: floatShape2 9s ease-in-out infinite reverse;
}

.shape-7 {
  width: 55px;
  height: 55px;
  border: 2px solid rgba(162, 155, 254, 0.2);
  bottom: 45%;
  right: 8%;
  clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
  border: none;
  background: rgba(108, 92, 231, 0.08);
  animation: floatShape7 15s ease-in-out infinite;
}

.shape-8 {
  width: 25px;
  height: 25px;
  border: 2px solid rgba(253, 121, 168, 0.3);
  top: 45%;
  left: 35%;
  border-radius: 4px;
  animation: floatShape1 8s ease-in-out infinite reverse;
}

@keyframes floatShape1 {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(180deg); }
}

@keyframes floatShape2 {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-25px) scale(1.15); }
}

@keyframes floatShape3 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(20px, -20px) rotate(120deg); }
  66% { transform: translate(-15px, -10px) rotate(240deg); }
}

@keyframes floatShape4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(15px, -20px) scale(0.85); }
}

@keyframes floatShape5 {
  0%, 100% { transform: translateY(0) rotate(0deg) scale(1); }
  50% { transform: translateY(-35px) rotate(180deg) scale(1.1); }
}

@keyframes floatShape7 {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-25px) rotate(30deg); }
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  animation: twinkle 3s ease-in-out infinite;
}

.p-1 { top: 15%; left: 20%; animation-delay: 0s; }
.p-2 { top: 35%; left: 70%; animation-delay: 0.4s; }
.p-3 { top: 60%; left: 30%; animation-delay: 0.8s; }
.p-4 { top: 80%; left: 60%; animation-delay: 1.2s; }
.p-5 { top: 20%; left: 85%; animation-delay: 1.6s; }
.p-6 { top: 70%; left: 10%; animation-delay: 2s; }
.p-7 { top: 45%; left: 45%; animation-delay: 2.4s; }
.p-8 { top: 90%; left: 40%; animation-delay: 0.6s; }
.p-9 { top: 5%; left: 50%; animation-delay: 1.8s; }
.p-10 { top: 50%; left: 90%; animation-delay: 1s; }
.p-11 { top: 30%; left: 5%; animation-delay: 2.2s; }
.p-12 { top: 75%; left: 80%; animation-delay: 0.2s; }

@keyframes twinkle {
  0%, 100% { opacity: 0.2; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.3); }
}
</style>
