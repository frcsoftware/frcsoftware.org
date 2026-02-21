/**
 * Remark plugin to handle :::figure directive blocks
 *
 * Usage in MDX:
 * :::figure
 * ![Alt text](./img/image.webp)
 * Caption text goes here
 * :::
 *
 * Or with attributes:
 * :::figure{w=80}
 * ![Alt text](./img/image.webp)
 * Caption text
 * :::
 *
 * :::figure{border}
 * ![Alt text](./img/image.webp)
 * Caption with default border
 * :::
 *
 * This plugin transforms the directive into a proper <figure> element
 * with <figcaption> for the text content.
 */

import { visit } from 'unist-util-visit';
import type { Root } from 'mdast';

interface ContainerDirective {
  type: 'containerDirective';
  name: string;
  attributes?: Record<string, string>;
  children: any[];
  data?: {
    hName?: string;
    hProperties?: Record<string, any>;
  };
}

export function remarkFigure() {
  return (tree: Root) => {
    visit(tree, 'containerDirective', (node: ContainerDirective) => {
      if (node.name !== 'figure') return;

      const attrs = node.attributes || {};

      // Build styles from attributes
      let style = '';

      // Width attribute (just a number, becomes percentage)
      if (attrs.width) {
        style += `width: ${attrs.width};`;
      } else if (attrs.w) {
        style += `width: ${attrs.w}%;`;
      }

      // Border attribute
      if ('border' in attrs) {
        // If border has a value, use it; otherwise use default
        const borderValue = attrs.border || '5px solid #ADADAD';
        style += ` --figure-border: ${borderValue.replace(/_/g, ' ')};`;
      }

      // Transform to figure element
      node.data = node.data || {};
      node.data.hName = 'figure';
      node.data.hProperties = node.data.hProperties || {};
      node.data.hProperties.class = 'md-figure' + ('border' in attrs ? ' md-figure-border' : '');

      if (style) {
        node.data.hProperties.style = style.trim();
      }

      // Process children to separate images from caption text
      const newChildren: any[] = [];

      for (const child of node.children) {
        if (child.type === 'paragraph' && child.children) {
          const images: any[] = [];
          const textNodes: any[] = [];

          // Separate images from text within the paragraph
          for (const subChild of child.children) {
            if (subChild.type === 'image') {
              images.push(subChild);
            } else if (subChild.type === 'text' && subChild.value.trim()) {
              textNodes.push(subChild);
            } else if (subChild.type !== 'text' || subChild.value.trim()) {
              // Keep other non-empty nodes as text content
              textNodes.push(subChild);
            }
          }

          // Add images as their own paragraph
          if (images.length > 0) {
            newChildren.push({
              type: 'paragraph',
              children: images,
              data: child.data
            });
          }

          // Add text as figcaption
          if (textNodes.length > 0) {
            newChildren.push({
              type: 'paragraph',
              children: textNodes,
              data: {
                hName: 'figcaption',
                hProperties: { class: 'md-figcaption' }
              }
            });
          }
        } else {
          newChildren.push(child);
        }
      }

      node.children = newChildren;
    });
  };
}

export default remarkFigure;
