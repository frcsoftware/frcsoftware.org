import { visit } from 'unist-util-visit';
import { readFileSync, existsSync } from 'fs';
import { resolve } from 'path';
import type { Root } from 'mdast';
import { VFile } from 'vfile';
import { localeDirFromDocsPath } from '../config/locales';

export default function remarkCodeRegion() {
    return (tree: Root, file: VFile) => {
        const examplesDir = resolve(process.cwd(), 'examples');
        // check locale directory for code region sources first, then fall back to default examples dir
        const localeDir = localeDirFromDocsPath(file.path);
        let codeRegionSources = file.data.astro?.frontmatter?.codeRegionSources;
        if (
            codeRegionSources == null ||
            typeof codeRegionSources !== 'object'
        ) {
            codeRegionSources = {};
        }

        visit(tree, 'code', (node) => {
            const meta: string = node.meta || '';

            const token = meta.match(/^(\S+)/);
            if (!token?.[1]) return;

            const raw = token[1];
            const hashIdx = raw.indexOf('#');
            let filePath = hashIdx === -1 ? raw : raw.slice(0, hashIdx);
            if (filePath === '' && codeRegionSources.default) {
                filePath = codeRegionSources.default;
            } else {
                const entries = Object.entries(codeRegionSources);
                for (const [source, pathAlias] of entries) {
                    if (
                        typeof source === 'string' &&
                        typeof pathAlias === 'string' &&
                        filePath === '{' + source + '}'
                    ) {
                        filePath = pathAlias;
                        break;
                    }
                }
            }
            const regionName = hashIdx !== -1 ? raw.slice(hashIdx + 1) : null;

            node.meta = meta.slice(raw.length).trim();

            const candidates = localeDir
                ? [
                      resolve(examplesDir, localeDir, filePath),
                      resolve(examplesDir, filePath),
                  ]
                : [resolve(examplesDir, filePath)];
            const srcPath = candidates.find(existsSync);
            if (!srcPath) {
                throw Error(
                    `Code region source "${filePath}" not found for ${file.path ?? 'unknown file'} ` +
                        `(looked in ${candidates.join(', ')})`,
                );
            }

            const content = readFileSync(srcPath, 'utf-8');
            const lines = content.split('\n');

            const markerRE = /^\s*(?:\/\/|#|--|<!--|-->)?\s*\[\/?\w+\]\s*$/;

            if (!regionName) {
                node.value = dedent(
                    lines.filter((l) => !markerRE.test(l)).join('\n'),
                );
                return;
            }

            const escapedName = escapeRegex(regionName);
            const startRE = new RegExp(
                `^\\s*(?:\\/\\/|#|--|<!--|-->)?\\s*\\[${escapedName}\\]\\s*$`,
            );
            const endRE = new RegExp(
                `^\\s*(?:\\/\\/|#|--|<!--|-->)?\\s*\\[\\/${escapedName}\\]\\s*$`,
            );

            const regionLines: string[] = [];
            let inRegion = false;
            let found = false;

            for (const line of lines) {
                if (!inRegion && startRE.test(line)) {
                    inRegion = true;
                    found = true;
                    continue;
                }
                if (inRegion && endRE.test(line)) {
                    inRegion = false;
                    continue;
                }
                if (inRegion && !markerRE.test(line)) regionLines.push(line);
            }

            const referencedBy = `referenced by ${file.path ?? 'unknown file'}`;

            if (!found)
                throw Error(
                    `Region "${regionName}" not found in ${srcPath} (${referencedBy})`,
                );

            if (inRegion)
                throw Error(
                    `Unclosed region "${regionName}" in ${srcPath} — missing [/${regionName}] (${referencedBy})`,
                );

            node.value = dedent(regionLines.join('\n'));
        });
    };
}

function escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function dedent(str: string): string {
    const lines = str.split('\n');
    const indent = lines
        .filter((l) => l.trim().length > 0)
        .reduce(
            (min, l) =>
                Math.min(min, l.match(/^[ \t]*/)?.[0].length ?? Infinity),
            Infinity,
        );
    if (indent === 0 || !Number.isFinite(indent)) return str;
    return lines.map((l) => l.slice(indent)).join('\n');
}
