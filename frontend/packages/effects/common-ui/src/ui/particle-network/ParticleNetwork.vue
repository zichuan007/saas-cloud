<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';

import { usePreferences } from '@vben-core/preferences';

interface Props {
  particleCount?: number;
  particleColor?: string;
  lineColor?: string;
  maxDistance?: number;
  speed?: number;
  interactive?: boolean;
  opacity?: number;
}

const props = withDefaults(defineProps<Props>(), {
  interactive: true,
  lineColor: '',
  maxDistance: 120,
  opacity: 0.6,
  particleColor: '',
  particleCount: 60,
  speed: 0.5,
});

const { isDark } = usePreferences();

const canvasRef = ref<HTMLCanvasElement>();
let ctx: CanvasRenderingContext2D | null = null;
let animationId = 0;
let particles: Particle[] = [];
const mouse = { x: -1000, y: -1000 };

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
}

function getThemeColor(type: 'line' | 'particle'): string {
  if (type === 'particle' && props.particleColor) return props.particleColor;
  if (type === 'line' && props.lineColor) return props.lineColor;
  return isDark.value
    ? 'rgba(148, 163, 184, 0.6)'
    : 'rgba(100, 116, 139, 0.5)';
}

function createParticles(width: number, height: number) {
  particles = [];
  for (let i = 0; i < props.particleCount; i++) {
    particles.push({
      radius: Math.random() * 2 + 1,
      vx: (Math.random() - 0.5) * props.speed,
      vy: (Math.random() - 0.5) * props.speed,
      x: Math.random() * width,
      y: Math.random() * height,
    });
  }
}

function draw() {
  if (!ctx || !canvasRef.value) return;

  const { height, width } = canvasRef.value;
  ctx.clearRect(0, 0, width, height);

  const particleRgba = getThemeColor('particle');
  const maxDistSq = props.maxDistance * props.maxDistance;

  for (const p of particles) {
    p.x += p.vx;
    p.y += p.vy;

    if (p.x < 0 || p.x > width) p.vx *= -1;
    if (p.y < 0 || p.y > height) p.vy *= -1;

    ctx.beginPath();
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
    ctx.fillStyle = particleRgba;
    ctx.fill();
  }

  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i]!.x - particles[j]!.x;
      const dy = particles[i]!.y - particles[j]!.y;
      const distSq = dx * dx + dy * dy;

      if (distSq < maxDistSq) {
        const alpha = (1 - distSq / maxDistSq) * props.opacity;
        ctx.beginPath();
        ctx.moveTo(particles[i]!.x, particles[i]!.y);
        ctx.lineTo(particles[j]!.x, particles[j]!.y);
        ctx.strokeStyle = isDark.value
          ? `rgba(148, 163, 184, ${alpha})`
          : `rgba(100, 116, 139, ${alpha})`;
        ctx.lineWidth = 0.5;
        ctx.stroke();
      }
    }
  }

  if (props.interactive) {
    for (const p of particles) {
      const dx = mouse.x - p.x;
      const dy = mouse.y - p.y;
      const distSq = dx * dx + dy * dy;
      const mouseDistSq = (props.maxDistance * 1.5) ** 2;

      if (distSq < mouseDistSq) {
        const alpha = (1 - distSq / mouseDistSq) * props.opacity;
        ctx.beginPath();
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(mouse.x, mouse.y);
        ctx.strokeStyle = isDark.value
          ? `rgba(96, 165, 250, ${alpha})`
          : `rgba(59, 130, 246, ${alpha})`;
        ctx.lineWidth = 0.8;
        ctx.stroke();
      }
    }
  }

  animationId = requestAnimationFrame(draw);
}

function handleResize() {
  if (!canvasRef.value) return;
  const parent = canvasRef.value.parentElement;
  if (!parent) return;

  const dpr = window.devicePixelRatio || 1;
  const rect = parent.getBoundingClientRect();

  canvasRef.value.width = rect.width * dpr;
  canvasRef.value.height = rect.height * dpr;
  canvasRef.value.style.width = `${rect.width}px`;
  canvasRef.value.style.height = `${rect.height}px`;

  ctx?.scale(dpr, dpr);
  createParticles(rect.width, rect.height);
}

function handleMouseMove(e: MouseEvent) {
  if (!canvasRef.value) return;
  const rect = canvasRef.value.getBoundingClientRect();
  mouse.x = e.clientX - rect.left;
  mouse.y = e.clientY - rect.top;
}

function handleMouseLeave() {
  mouse.x = -1000;
  mouse.y = -1000;
}

onMounted(() => {
  if (!canvasRef.value) return;
  ctx = canvasRef.value.getContext('2d');
  handleResize();
  draw();

  window.addEventListener('resize', handleResize);
  if (props.interactive) {
    canvasRef.value.addEventListener('mousemove', handleMouseMove);
    canvasRef.value.addEventListener('mouseleave', handleMouseLeave);
  }
});

onUnmounted(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', handleResize);
  if (canvasRef.value) {
    canvasRef.value.removeEventListener('mousemove', handleMouseMove);
    canvasRef.value.removeEventListener('mouseleave', handleMouseLeave);
  }
});

watch(isDark, () => {
  // theme changed, particles will update colors on next frame automatically
});
</script>

<template>
  <canvas
    ref="canvasRef"
    class="pointer-events-auto absolute inset-0 size-full"
  />
</template>
