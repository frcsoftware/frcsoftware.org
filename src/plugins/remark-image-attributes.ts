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
import type { Root, Image } from 'mdast';

interface ImageNode extends Image {
  data?: {
    hName?: string;
    hProperties?: Record<string, any>;
  };
}

interface ParentNode {
  type: string;
  children: any[];
  data?: {
    hName?: string;
    hProperties?: Record<string, any>;
  };
}

export function remarkImageAttributes() {
  return (tree: Root) => {
    visit(tree, 'paragraph', (node: ParentNode, index: number | undefined, parent: ParentNode | undefined) => {
      if (!parent || index === undefined) return;

      const children = node.children;

      // Look for images with hash attributes in URL
      for (let i = 0; i < children.length; i++) {
        const child = children[i];

        if (child.type === 'image') {
          const imageNode = child as ImageNode;
          const url = imageNode.url;

          // Check for hash in URL
          const hashIndex = url.indexOf('#');
          if (hashIndex !== -1) {
            const attributesStr = url.substring(hashIndex + 1);
            const cleanUrl = url.substring(0, hashIndex);

            // Update image URL to remove hash
            imageNode.url = cleanUrl;

            // Parse attributes
            const attributes = parseAttributes(attributesStr);

            // Get alignment (default: center)
            const align = attributes.align || 'center';
            // Use 'w' for width (number becomes percentage)
            const width = attributes.w ? `${attributes.w}%` : '100%';
            const border = attributes.border || '';

            // Build inline styles for the image
            let imgStyle = `width: ${width};`;
            if (border) {
              imgStyle += ` border: ${border};`;
            }

            // Set image properties
            imageNode.data = imageNode.data || {};
            imageNode.data.hProperties = imageNode.data.hProperties || {};
            imageNode.data.hProperties.style = imgStyle;

            // Store attributes as data-* for Slides component to read
            if (attributes.w) {
              imageNode.data.hProperties['data-slide-width'] = attributes.w;
            }
            if (border) {
              imageNode.data.hProperties['data-slide-border'] = border;
            }
            if (attributes.align) {
              imageNode.data.hProperties['data-slide-align'] = attributes.align;
            }

            // Wrap the paragraph to act as a container
            node.data = node.data || {};
            node.data.hName = 'div';
            node.data.hProperties = node.data.hProperties || {};
            node.data.hProperties.class = `img-wrapper img-align-${align}`;
          } else if (children.length === 1) {
            // Standalone image without attributes - still wrap and center
            node.data = node.data || {};
            node.data.hName = 'div';
            node.data.hProperties = node.data.hProperties || {};
            node.data.hProperties.class = 'img-wrapper img-align-center';
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
      // Replace underscores with spaces for border values
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
