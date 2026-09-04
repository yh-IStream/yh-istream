import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'
import transformerDirectives from '@unocss/transformer-directives'
import transformerVariantGroup from '@unocss/transformer-variant-group'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons({
      scale: 1.2,
      warn: true,
    }),
  ],
  transformers: [transformerDirectives(), transformerVariantGroup()],
  shortcuts: {
    'flex-center': 'flex items-center justify-center',
    'flex-between': 'flex items-center justify-between',
    'flex-col-center': 'flex flex-col items-center justify-center',
    'text-ellipsis': 'truncate',
    'wh-full': 'w-full h-full',
    'card': 'bg-white/80 dark:bg-dark-700/60 rounded-xl p-4 backdrop-blur-md border border-white/20 dark:border-white/8 shadow-sm',
    'glass': 'bg-white/60 dark:bg-dark-800/50 backdrop-blur-xl border border-white/30 dark:border-white/10 rounded-2xl',
    'glass-subtle': 'bg-white/40 dark:bg-dark-800/40 backdrop-blur-lg border border-white/20 dark:border-white/8 rounded-xl',
    'glow': 'shadow-[0_0_20px_rgba(20,184,166,0.15)]',
    'glow-strong': 'shadow-[0_0_30px_rgba(20,184,166,0.25)]',
    'text-glow': 'text-teal-400 dark:text-teal-300',
    'stream-gradient': 'bg-gradient-to-r from-teal-500 via-cyan-500 to-blue-500',
    'stream-gradient-subtle': 'bg-gradient-to-r from-teal-400/20 via-cyan-400/20 to-blue-400/20',
  },
  theme: {
    colors: {
      primary: {
        DEFAULT: '#14b8a6',
        50: '#f0fdfa',
        100: '#ccfbf1',
        200: '#99f6e4',
        300: '#5eead4',
        400: '#2dd4bf',
        500: '#14b8a6',
        600: '#0d9488',
        700: '#0f766e',
        800: '#115e59',
        900: '#134e4a',
      },
    },
  },
  rules: [
    [/^m-([\d.]+)$/, ([, d]) => ({ margin: `${d}px` })],
    [/^p-([\d.]+)$/, ([, d]) => ({ padding: `${d}px` })],
  ],
})