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
    'card': 'bg-white rounded-lg shadow-sm p-4 dark:bg-dark-800',
  },
  theme: {
    colors: {
      primary: {
        DEFAULT: '#18a058',
        50: '#f0faf3',
        100: '#d7f4e0',
        200: '#b2e9c5',
        300: '#80d8a3',
        400: '#4dc07e',
        500: '#18a058',
        600: '#0f8c46',
        700: '#0c6e38',
        800: '#0b572e',
        900: '#094726',
      },
    },
  },
  rules: [
    [/^m-([\d.]+)$/, ([, d]) => ({ margin: `${d}px` })],
    [/^p-([\d.]+)$/, ([, d]) => ({ padding: `${d}px` })],
  ],
})