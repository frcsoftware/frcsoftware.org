// Prefixes root-relative links in localized content with their locale, so a
// translated page can carry the same link text as the English one it mirrors.
//
// Untranslated pages are served as fallbacks rendering the default-language
// source, so their links are not localized and following one leaves the locale.

import { visit } from 'unist-util-visit';
import type { Root, Definition, Link } from 'mdast';
import type { VFile } from 'vfile';
import { localeDirFromDocsPath, localizeHref } from '../config/locales';

export function remarkLocalizeLinks() {
    return (tree: Root, file: VFile) => {
        const localeDir = localeDirFromDocsPath(file.path);
        if (!localeDir) return;

        visit(tree, ['link', 'definition'], (node) => {
            const target = node as Link | Definition;
            target.url = localizeHref(target.url, localeDir);
        });
    };
}

export default remarkLocalizeLinks;
