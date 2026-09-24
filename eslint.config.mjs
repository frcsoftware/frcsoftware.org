import js from '@eslint/js';
import astro from 'eslint-plugin-astro';
import globals from 'globals';
import tseslint from 'typescript-eslint';

import { defineConfig } from 'eslint/config';
export default defineConfig([
    { ignores: ['dist/', '.astro/', 'node_modules/'] },

    js.configs.recommended,

    ...tseslint.configs.recommended,

    ...astro.configs['flat/recommended'],

    {
        rules: {
            '@typescript-eslint/no-unused-vars': [
                'warn',
                { argsIgnorePattern: '^_' },
            ],
            '@typescript-eslint/no-explicit-any': 'error',
            'no-undef': 'off',
        },
    },
    {
        files: ['scripts/*', 'src/plugins/*'],
        languageOptions: {
            globals: {
                // scripts and plugins run in a node env
                ...globals.node,
            },
        },
    },
    {
        // also catch astro virtual files
        files: ['**/*.astro', '**/*.astro/**/*.ts'],
        languageOptions: {
            globals: {
                ImageMetadata: 'readonly',
            },
        },
    },
]);
