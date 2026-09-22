// Prefixes root-relative links in localized content with their locale, so a
// translated page can carry the same link text as the English one it mirrors.
//
// Untranslated pages are served as fallbacks rendering the default-language
// source, so their links are not localized and following one leaves the locale.

import { visit } from 'unist-util-visit';
import type { Root, Definition, Link } from 'mdast';
import type { VFile } from 'vfile';
import { localeDirFromDocsPath } from '../config/locales';

/**
 * Whether a root-relative URL addresses a page rather than a file in public/.
 * Page URLs are extensionless; `/learning-course/.../slide1.webp` is an asset
 * that lives at one path regardless of locale.
 */
function isPageUrl(url: string): boolean {
    if (!url.startsWith('/') || url.startsWith('//')) return false;

    const [path = ''] = url.split(/[?#]/);
    const lastSegment = path.replace(/\/$/, '').split('/').pop() ?? '';
    return !lastSegment.includes('.');
}

export function remarkLocalizeLinks() {
    return (tree: Root, file: VFile) => {
        const localeDir = localeDirFromDocsPath(file.path);
        if (!localeDir) return;

        const prefix = `/${localeDir}`;

        visit(tree, ['link', 'definition'], (node) => {
            const target = node as Link | Definition;
            const { url } = target;

            if (!isPageUrl(url)) return;
            if (url === prefix || url.startsWith(`${prefix}/`)) return;

            target.url = prefix + url;
        });
    };
}

export default remarkLocalizeLinks;
