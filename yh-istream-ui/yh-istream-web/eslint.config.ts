import eslintPluginVue from 'eslint-plugin-vue'
import vueTsEslintConfig from '@vue/eslint-config-typescript'

export default [
  ...vueTsEslintConfig(),
  ...eslintPluginVue.configs['flat/recommended'],
  {
    rules: {
      'vue/multi-word-component-names': 'off',
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'vue/no-v-html': 'error',
    },
  },
  {
    ignores: ['dist/', 'node_modules/', 'src/auto-imports.d.ts', 'src/components.d.ts', 'src/typed-router.d.ts'],
  },
]