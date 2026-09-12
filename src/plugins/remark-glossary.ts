import { readFileSync } from 'node:fs';
import { visit, SKIP } from 'unist-util-visit';
import { parse } from 'yaml';
import type { Root, RootContent, Text } from 'mdast';
import type { VFile } from 'vfile';
import type { MdxJsxTextElement } from 'mdast-util-mdx-jsx';

const glossary = parse(
    readFileSync(new URL('../data/glossary.yaml', import.meta.url), 'utf-8'),
) as Record<string, { definition: string; caseSensitive?: boolean }>;

const sortedTerms = Object.entries(glossary).sort(
    ([a], [b]) => b.length - a.length,
);

const pattern = new RegExp(
    `(?<![\\p{L}\\p{N}_])(${sortedTerms
        .map(([term, { caseSensitive }]) =>
            caseSensitive ? `(?-i:${escapeRegex(term)})` : escapeRegex(term),
        )
        .join('|')})(?![\\p{L}\\p{N}_])`,
    'giu',
);

function escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function remarkGlossary() {
    return (tree: Root, file: VFile) => {
        if (file.path?.endsWith('glossary.mdx')) return;

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

                if (matchStart > lastIndex) {
                    newNodes.push({
                        type: 'text',
                        value: text.slice(lastIndex, matchStart),
                    });
                }

                const glossaryNode: MdxJsxTextElement = {
                    type: 'mdxJsxTextElement',
                    name: 'Glossary',
                    attributes: [
                        {
                            type: 'mdxJsxAttribute',
                            name: 'term',
                            value: matchedTerm,
                        },
                    ],
                    children: [],
                };
                newNodes.push(glossaryNode);

                lastIndex = matchEnd;
            });

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
