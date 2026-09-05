import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import starlightLinksValidator from 'starlight-links-validator';
import starlightSidebarTopics from 'starlight-sidebar-topics';
import { sidebarTopics } from './src/config/sidebarTopics';
import remarkGlossary from './src/plugins/remark-glossary';
import remarkCenter from './src/plugins/remark-center';
import remarkFigure from './src/plugins/remark-figure';
import remarkImageAttributes from './src/plugins/remark-image-attributes';
import { remarkMdxGlobalImports } from './src/plugins/remark-mdx-global-imports';
import remarkCodeRegion from './src/plugins/remark-code-region';
import { unified } from '@astrojs/markdown-remark';
import rehypeTargetBlank from './src/plugins/rehype-external-links';

export default defineConfig({
    site: 'https://frcsoftware.org',
    prefetch: true,

    markdown: {
        processor: unified({
            remarkPlugins: [
                remarkCenter,
                remarkFigure,
                remarkGlossary,
                remarkImageAttributes,
                remarkMdxGlobalImports,
                remarkCodeRegion,
            ],
            rehypePlugins: [rehypeTargetBlank],
            remarkRehype: {
                footnoteLabel: 'References',
                footnoteLabelTagName: 'h4',
                // override properties so footnote label is visible
                footnoteLabelProperties: {},
            },
        }),
    },

    integrations: [
        starlight({
            title: 'FRCSoftware.org',
            favicon: '/favicon.svg',
            head: [
                {
                    tag: 'meta',
                    attrs: {
                        property: 'og:image',
                        content: 'https://frcsoftware.org/favicon.svg',
                    },
                },
                {
                    tag: 'meta',
                    attrs: {
                        property: 'og:image:alt',
                        content: 'FRCSoftware.org logo icon',
                    },
                },
                {
                    tag: 'meta',
                    attrs: {
                        property: 'og:description',
                        content:
                            'The comprehensive learning guide for FRC programming',
                    },
                },
            ],
            logo: {
                src: './src/assets/universal/favicon-white.svg',
            },
            customCss: ['./src/styles/global.css'],
            components: {
                Header: './src/starlightOverrides/Header.astro',
                Footer: './src/starlightOverrides/Footer.astro',
                Sidebar: './src/starlightOverrides/Sidebar.astro',
                Pagination: './src/starlightOverrides/Pagination.astro',
                Hero: './src/starlightOverrides/Hero.astro',
                TableOfContents:
                    './src/starlightOverrides/TableOfContents.astro',
            },
            // TOC is disabled globally but can be enabled per-directory in src/config/tocConfig.ts
            // or per-page via frontmatter (tableOfContents: true)
            tableOfContents: { minHeadingLevel: 2, maxHeadingLevel: 3 },
            plugins: [
                // Separates sidebar into topics that are switchable with a dropdown
                starlightSidebarTopics(sidebarTopics, {
                    exclude: ['/', '/test-content-figure'],
                }),
                starlightLinksValidator(),
            ],
        }),
    ],
});
