/**
 * Remark plugin to handle image attributes encoded in the URL hash
 *
 * Usage in MDX:
 * ![Alt text](./img/image.webp#w=80)           // Width 80%
 * ![Alt text](./img/image.webp#w=60&border)   // Width 60% with default border
 * ![Alt text](./img/image.webp#border)        // Default border (5px solid #ADADAD)
 * ![Alt text](./img/image.webp#align=left)    // Left aligned
 *
 * Supported attributes:
 * - w: Width as percentage number (default: 100)
 * - border: Just "border" for default (5px solid #ADADAD), or border=value (underscores = spaces)
 * - align: left | center | right (default: center)
 */

import { visit } from 'unist-util-visit';
import type { Root } from 'mdast';

export function remarkImageAttributes() {
    return (tree: Root) => {
        visit(tree, 'paragraph', (node, index, parent) => {
            if (!parent || index === undefined) return;

            const children = node.children;

            // A shared wrapper cannot carry separate alignments for multiple images.
            // In that case, give each image its own block-level alignment instead.
            const multipleImages =
                children.filter((child) => child.type === 'image').length > 1;
            if (multipleImages) {
                node.data = node.data || {};
                node.data.hName = 'div';
                node.data.hProperties = node.data.hProperties || {};
                node.data.hProperties.class = 'img-wrapper img-multiple';
            }

            // Look for images with hash attributes in URL
            for (const child of children) {
                if (child.type === 'image') {
                    const url = child.url;

                    // Check for hash in URL
                    const hashIndex = url.indexOf('#');
                    if (hashIndex !== -1) {
                        const attributesStr = url.substring(hashIndex + 1);
                        const cleanUrl = url.substring(0, hashIndex);

                        // Update image URL to remove hash
                        child.url = cleanUrl;

                        // Parse attributes
                        const attributes = parseAttributes(attributesStr);

                        // Get alignment (default: center)
                        const align = attributes.align || 'center';
                        // Use 'w' for width (number becomes percentage)
                        const width = attributes.w
                            ? `${attributes.w}%`
                            : '100%';
                        const border = attributes.border || '';

                        // Build inline styles for the image
                        let imgStyle = `width: ${width};`;
                        if (border) {
                            imgStyle += ` border: ${border};`;
                        }

                        if (multipleImages) {
                            // Keep image styles isolated instead of aligning the entire
                            // paragraph (which would also align sibling images).
                            const margins =
                                align === 'left'
                                    ? ' margin-left: 0; margin-right: auto;'
                                    : align === 'right'
                                      ? ' margin-left: auto; margin-right: 0;'
                                      : ' margin-left: auto; margin-right: auto;';
                            imgStyle += ` display: block;${margins}`;
                        }

                        // Set image properties
                        child.data = child.data || {};
                        child.data.hProperties = child.data.hProperties || {};
                        child.data.hProperties.style = imgStyle;

                        // Store attributes as data-* for Slides component to read
                        if (attributes.w) {
                            child.data.hProperties['data-slide-width'] =
                                attributes.w;
                        }
                        if (border) {
                            child.data.hProperties['data-slide-border'] =
                                border;
                        }
                        if (attributes.align) {
                            child.data.hProperties['data-slide-align'] =
                                attributes.align;
                        }

                        // Wrap the paragraph to act as a container
                        if (!multipleImages) {
                            node.data = node.data || {};
                            node.data.hName = 'div';
                            node.data.hProperties = node.data.hProperties || {};
                            node.data.hProperties.class = `img-wrapper img-align-${align}`;
                        }
                    } else if (multipleImages) {
                        // Unattributed images retain their own default left alignment.
                        child.data = child.data || {};
                        child.data.hProperties = child.data.hProperties || {};
                        child.data.hProperties.style =
                            'display: block; margin-left: 0; margin-right: auto;';
                    } else if (children.length === 1) {
                        // Standalone image without attributes - still wrap and left-align
                        node.data = node.data || {};
                        node.data.hName = 'div';
                        node.data.hProperties = node.data.hProperties || {};
                        node.data.hProperties.class =
                            'img-wrapper img-align-left';
                    }
                }
            }
        });
    };
}

function parseAttributes(str: string): Record<string, string> {
    const attrs: Record<string, string> = {};

    // Split by & for multiple attributes
    const parts = str.split('&');

    for (const part of parts) {
        if (part.includes('=')) {
            const [key, ...valueParts] = part.split('=');
            if (!key || valueParts.length === 0) continue;
            const value = valueParts.join('=').replace(/_/g, ' ');
            attrs[key] = value;
        } else if (part === 'border') {
            // Standalone border keyword
            attrs.border = '5px solid #ADADAD';
        }
    }

    return attrs;
}

export default remarkImageAttributes;
