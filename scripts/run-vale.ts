// runs vale but excludes any locale directories

import { spawnSync } from 'child_process';
import { localeDirs } from '../src/config/locales';

const args = ['src/content'];

if (localeDirs.length > 0) {
    args.unshift(
        `--glob=!{${localeDirs.map((dir) => `**/${dir}/**`).join(',')}}`,
    );
}

const { status, error } = spawnSync('vale', args, { stdio: 'inherit' });

if (error) throw error;
process.exit(status ?? 1);
