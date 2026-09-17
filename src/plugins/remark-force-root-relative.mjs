import { visit } from 'unist-util-visit';

export default function remarkForceRootRelative() {
    /**
     * @param {import("mdast").Root} tree
     * @param {import("vfile").VFile} file
     */
    return (tree, file) => {
        visit(tree, ['link', 'image', 'definition'], (node) => {
            if (
                (node.type !== 'link' &&
                    node.type !== 'image' &&
                    node.type !== 'definition') ||
                !URL.canParse(node.url)
            ) {
                return;
            }
            // can't use the site property from astro.config.mjs, see https://github.com/withastro/starlight/pull/3572
            const hostname = 'frcsoftware.org';
            const url = new URL(node.url);
            if (url.hostname === hostname) {
                file.message(
                    'Use root-relative URL syntax for internal links.',
                    node,
                ).fatal = true;
            }
        });
    };
}
