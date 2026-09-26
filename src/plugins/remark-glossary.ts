import { visit, SKIP } from 'unist-util-visit';
import type { Root, RootContent, Text } from 'mdast';
import type { VFile } from 'vfile';
import type { MdxJsxTextElement } from 'mdast-util-mdx-jsx';
import { matcherFor } from '../data/loadGlossary';
import { langFromDocsPath } from '../config/locales';

export function remarkGlossary() {
    return (tree: Root, file: VFile) => {
        if (file.path?.endsWith('glossary.mdx')) return;

        const lang = langFromDocsPath(file.path);
        const { pattern, canonicalFor } = matcherFor(lang);

        const seen = new Map<string, boolean>();

        visit(tree, 'text', (node: Text, index, parent) => {
            if (!parent || index === undefined) return;

            if (parent.type === 'link' || parent.type === 'mdxJsxTextElement') {
                return;
            }

            const text = node.value;
            const matches = [...text.matchAll(pattern)];

            if (matches.length === 0) return;

            const newNodes: RootContent[] = [];
            let lastIndex = 0;

            matches.forEach((match) => {
                const matchStart = match.index;
                const matchEnd = matchStart + match[0].length;
                const matchedTerm = match[0];

                const lowered = matchedTerm.toLowerCase();
                if (seen.has(lowered)) {
                    return;
                }
                seen.set(lowered, true);

                if (matchStart > lastIndex) {
                    newNodes.push({
                        type: 'text',
                        value: text.slice(lastIndex, matchStart),
                    });
                }

                const attributes: MdxJsxTextElement['attributes'] = [
                    {
                        type: 'mdxJsxAttribute',
                        name: 'term',
                        value: matchedTerm,
                    },
                ];

                // Lets the component resolve a translated term, and look the
                // definition up directly instead of scanning the collection.
                const canonical = canonicalFor(matchedTerm);
                if (canonical !== undefined) {
                    attributes.push(
                        {
                            type: 'mdxJsxAttribute',
                            name: 'termId',
                            value: canonical,
                        },
                        {
                            type: 'mdxJsxAttribute',
                            name: 'lang',
                            value: lang,
                        },
                    );
                }

                const glossaryNode: MdxJsxTextElement = {
                    type: 'mdxJsxTextElement',
                    name: 'Glossary',
                    attributes,
                    children: [],
                };
                newNodes.push(glossaryNode);

                lastIndex = matchEnd;
            });

            if (lastIndex == 0) {
                // no unseen matches
                return;
            }

            if (lastIndex < text.length) {
                newNodes.push({
                    type: 'text',
                    value: text.slice(lastIndex),
                });
            }

            parent.children.splice(index, 1, ...newNodes);
            return [SKIP, index + newNodes.length];
        });
    };
}

export default remarkGlossary;
