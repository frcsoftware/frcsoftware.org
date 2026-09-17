import { visit } from 'unist-util-visit';
import type { Root } from 'hast';
export default function rehypeTargetBlank() {
    return (tree: Root) => {
        visit(tree, 'element', (node) => {
            if (
                node.tagName !== 'a' ||
                typeof node.properties.href !== 'string' ||
                !URL.canParse(node.properties.href) ||
                // only match http/s
                !/^https?:/.test(URL.parse(node.properties.href)!.protocol)
            ) {
                // only run on valid external links
                return;
            }
            const target = node.properties.target;
            node.properties.target = target ? target : '_blank';
            let rel = node.properties.rel;
            if (!rel) {
                rel = ['noreferrer', 'noopener'];
            } else {
                // just in case someone overrides the remark rule
                if (!(rel.includes('opener') || rel.includes('noopener'))) {
                    rel.push('noopener');
                }
                if (!rel.includes('noreferrer')) {
                    rel.push('noreferrer');
                }
            }
            node.properties.rel = rel;
        });
    };
}
