import js from '@eslint/js';
import markdown from '@eslint/markdown';
import sentencesPerLine from 'eslint-plugin-sentences-per-line';
import astro from 'eslint-plugin-astro';
import globals from 'globals';
import tseslint from 'typescript-eslint';

import { defineConfig } from 'eslint/config';

const codeFiles = ['**/*.{js,mjs,cjs,ts,mts,cts,astro}'];

export default defineConfig([
    { ignores: ['dist/', '.astro/', 'node_modules/'] },

    {
        files: codeFiles,
        extends: [
            js.configs.recommended,
            tseslint.configs.recommended,
            astro.configs['flat/recommended'],
        ],
    },

    {
        files: codeFiles,
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
        files: ['src/content/**/*.mdx'],
        ignores: ['src/content/docs/resources/examples.mdx'],
        plugins: { markdown, 'sentences-per-line': sentencesPerLine },
        language: 'markdown/gfm',
        languageOptions: { frontmatter: 'yaml' },
        rules: { 'sentences-per-line/one': 'error' },
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
