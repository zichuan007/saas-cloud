import type { Directive } from 'vue';

export const vFadeIn: Directive<HTMLElement, number> = {
  mounted(el, binding) {
    const delay = binding.value ?? 0;

    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    el.style.transition = `opacity 0.6s ease ${delay}ms, transform 0.6s ease ${delay}ms`;

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
            observer.unobserve(el);
          }
        }
      },
      { threshold: 0.1 },
    );

    observer.observe(el);

    (el as any).__fadeInObserver = observer;
  },
  unmounted(el) {
    const observer = (el as any).__fadeInObserver as
      | IntersectionObserver
      | undefined;
    if (observer) {
      observer.disconnect();
      delete (el as any).__fadeInObserver;
    }
  },
};
