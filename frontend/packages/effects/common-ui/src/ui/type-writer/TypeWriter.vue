<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

interface Props {
  texts: string[];
  typeSpeed?: number;
  deleteSpeed?: number;
  pauseDuration?: number;
  loop?: boolean;
  cursorChar?: string;
}

const props = withDefaults(defineProps<Props>(), {
  cursorChar: '|',
  deleteSpeed: 40,
  loop: true,
  pauseDuration: 2000,
  typeSpeed: 80,
});

const displayText = ref('');
const showCursor = ref(true);
let timer: ReturnType<typeof setTimeout> | null = null;
let running = true;

async function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    timer = setTimeout(resolve, ms);
  });
}

async function typeText(text: string) {
  for (let i = 0; i <= text.length && running; i++) {
    displayText.value = text.slice(0, i);
    await sleep(props.typeSpeed);
  }
}

async function deleteText() {
  const text = displayText.value;
  for (let i = text.length; i >= 0 && running; i--) {
    displayText.value = text.slice(0, i);
    await sleep(props.deleteSpeed);
  }
}

async function run() {
  if (!props.texts.length) return;

  do {
    for (const text of props.texts) {
      if (!running) return;
      await typeText(text);
      await sleep(props.pauseDuration);
      if (!running) return;
      if (props.loop || props.texts.indexOf(text) < props.texts.length - 1) {
        await deleteText();
        await sleep(300);
      }
    }
  } while (props.loop && running);

  showCursor.value = false;
}

onMounted(() => {
  run();
});

onUnmounted(() => {
  running = false;
  if (timer) clearTimeout(timer);
});
</script>

<template>
  <span class="type-writer">
    <span>{{ displayText }}</span>
    <span v-if="showCursor" class="type-writer-cursor">{{ cursorChar }}</span>
  </span>
</template>

<style scoped>
.type-writer-cursor {
  animation: blink 1s step-end infinite;
  font-weight: 100;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
