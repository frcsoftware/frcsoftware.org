import { platform } from 'node:process';
import { resolve } from 'node:path';

const gradlew = resolve(
    'examples',
    platform === 'win32' ? 'gradlew.bat' : 'gradlew',
);

const runPrettierOn =
    '**/*.{' +
    [
        'js',
        'mjs',
        'ts',
        'json',
        'json5',
        'jsonc',
        'css',
        'md',
        'mdx',
        'yaml',
        'yml',
        'astro',
    ].join(',') +
    '}';

/** @type {import('lint-staged').Configuration} */
export default {
    [runPrettierOn]: (files) =>
        `prettier --write --ignore-unknown ${files.join(' ')}`,
    '{src,public}/**/*.{png,jpg,jpeg,webp}': (images) =>
        `node scripts/images.lint.js ${images.join(' ')}`,
    '**/*.{astro,ts,mjs,js}': (files) => `eslint --fix ${files.join(' ')}`,
    'src/content/**/*.{md,mdx}': (files) => [
        `pnpm remark ${files.join(' ')} --ext mdx --frail --no-stdout --quiet`,
    ],
    'examples/**/*.{java,gradle}': () => [
        `${gradlew} -p examples spotlessApply`,
    ],
    // Yes, I know this should be a FunctionTask but those are kinda bad until https://github.com/lint-staged/lint-staged/issues/1826 is resolved
    'package.json': () => 'pnpm tsx scripts/syncLockfile.lint.ts',
};
