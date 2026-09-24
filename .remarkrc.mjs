import remarkFrontmatter from 'remark-frontmatter';
import remarkMdx from 'remark-mdx';
import remarkGfm from 'remark-gfm';
import remarkPresetLintRecommended from 'remark-preset-lint-recommended';
import remarkNoInlineCodeFences from './src/plugins/remark-no-inline-code-fences.mjs';
import remarkNoHtmlLinks from './src/plugins/remark-no-html-links.mjs';
import remarkLintNoDeadUrls from 'remark-lint-no-dead-urls';
import remarkForceRootRelative from './src/plugins/remark-force-root-relative.mjs';
import remarkLintNoLiteralUrls from 'remark-lint-no-literal-urls';

export default {
    plugins: [
        remarkFrontmatter,
        remarkMdx,
        remarkGfm,
        remarkPresetLintRecommended,
        [remarkLintNoLiteralUrls, false],
        remarkNoInlineCodeFences,
        remarkNoHtmlLinks,
        // only run dead link checker in CI to save time in dev
        process.env.CI
            ? [
                  remarkLintNoDeadUrls,
                  {
                      skipLocalhost: false,
                      skipOffline: true,
                      skipUrlPatterns: [
                          'https://github.com/signup',
                          'https://code.visualstudio.com/',
                          'https://www.conventionalcommits.org/en/v1.0.0/',
                          'https://vale.sh/',
                          'https://squoosh.app/',
                      ], // Add known flaky URL patterns here
                  },
              ]
            : () => undefined,
        remarkForceRootRelative,
    ],
};
