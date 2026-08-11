// Sidebar configuration for each main navigation section
// Maps navbar routes to their specific sidebar items

export type SidebarItem = {
    label: string;
    slug?: string;
    items?: SidebarItem[];
    collapsed?: boolean;
};

export type SidebarSection = {
    label: string;
    items: SidebarItem[];
};

export type Item = {
    label: string;
    href?: string;
    items?: Item[];
};

// Define which URL paths belong to which sidebar section
export const sidebarSections: Record<string, SidebarSection[]> = {
    // Learning Course section
    '/learning-course': [
        {
            label: 'Learning Course',
            items: [
                { label: 'Overview', slug: 'learning-course' },
                {
                    label: 'Website Feature Guide',
                    slug: 'learning-course/getting-started/website-feature-guide',
                },
                {
                    label: 'Course Setup',
                    collapsed: true,
                    items: [
                        {
                            label: 'Required Tools',
                            slug: 'learning-course/getting-started/required-tools',
                        },
                        {
                            label: 'VS Code Overview',
                            slug: 'learning-course/getting-started/vscode-overview',
                        },
                        {
                            label: 'Forking and Cloning',
                            slug: 'learning-course/getting-started/forking-and-cloning',
                        },
                    ],
                },
                {
                    label: 'Stage 0',
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 0 Introduction',
                            slug: 'learning-course/stage0/stage-overview',
                        },
                        {
                            label: 'Java Fundamentals',
                            slug: 'learning-course/stage0/java-fundamentals',
                        },
                        {
                            label: 'Operators',
                            slug: 'learning-course/stage0/operators',
                        },
                        {
                            label: 'Conditionals',
                            slug: 'learning-course/stage0/conditionals',
                        },
                        // {
                        //     label: 'Loops',
                        //     slug: 'learning-course/stage0/loops',
                        // },
                        {
                            label: 'Classes, Fields, and Methods',
                            slug: 'learning-course/stage0/classes-methods',
                        },
                        // {
                        //     label: 'Methods',
                        //     slug: 'learning-course/stage0/methods',
                        // },
                    ],
                },
                {
                    label: 'Stage 1',
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1 Introduction',
                            slug: 'learning-course/stage1/stage-overview',
                        },
                        {
                            label: 'Stage 1A',
                            collapsed: true,
                            items: [
                                {
                                    label: 'Stage 1A Introduction',
                                    slug: 'learning-course/stage1/stage1a/stage-overview',
                                },
                                {
                                    label: 'Stage 1A Vocabulary',
                                    slug: 'learning-course/stage1/stage1a/1a-vocabulary',
                                },
                                // {
                                //     label: 'TBD',
                                //     slug: 'stage-1a-commands/the-command-body',
                                // },
                                // {
                                //     label: 'TBD',
                                //     slug: 'stage-1a-commands/commands-and-mechanisms',
                                // },
                            ],
                        },
                        {
                            label: 'Stage 1B: Commands',
                            collapsed: true,
                            items: [
                                {
                                    label: 'Stage 1B Introduction',
                                    slug: 'learning-course/stage1/stage1b/stage-overview',
                                },
                                {
                                    label: 'The Concepts',
                                    slug: 'learning-course/stage1/stage1b/command-based-overview',
                                },
                                {
                                    label: 'The Body of a Command',
                                    slug: 'learning-course/stage1/stage1b/the-command-body',
                                },
                                {
                                    label: 'Commands & Mechanisms, Pt. 1',
                                    slug: 'learning-course/stage1/stage1b/commands-and-mechanisms',
                                },
                                {
                                    label: 'Triggers and Scheduling',
                                    slug: 'learning-course/stage1/stage1b/triggers',
                                },
                                {
                                    label: 'Commands & Mechanisms, Pt. 2',
                                    slug: 'learning-course/stage1/stage1b/commands-and-mechanisms-pt2',
                                },
                                {
                                    label: 'Bonus: Spot the Error',
                                    slug: 'learning-course/stage1/stage1b/spot-the-error',
                                },
                                {
                                    label: 'Exercise - Kitbot Rewrite, Pt. 1',
                                    slug: 'learning-course/stage1/stage1b/command-based-kitbot',
                                },
                                {
                                    label: 'Suppliers in Command-Based',
                                    slug: 'learning-course/stage1/stage1b/suppliers-in-command-based',
                                },
                                {
                                    label: 'Exercise - Kitbot Rewrite, Pt. 2',
                                    slug: 'learning-course/stage1/stage1b/command-based-kitbot-pt2',
                                },
                                {
                                    label: 'Bonus: Spot the Error, Pt 2',
                                    slug: 'learning-course/stage1/stage1b/spot-the-error-pt2',
                                },
                            ],
                        },
                    ],
                },
            ],
        },
    ],
    // Educator's Guide section
    '/educators-guide': [
        {
            label: "Educator's Guide",
            items: [
                { label: 'Introduction', slug: 'educators-guide/introduction' },
                {
                    label: 'The Stages',
                    slug: 'educators-guide/introduction/the-stages',
                },
                {
                    label: 'Preparing Yourself',
                    slug: 'educators-guide/introduction/preparation',
                },
                { label: 'Stage 0', slug: 'educators-guide/stage0' },
                {
                    label: 'Stage 1',
                    collapsed: true,
                    items: [
                        { label: 'Overview', slug: 'educators-guide/stage1' },
                        {
                            label: 'Stage 1A',
                            slug: 'educators-guide/stage1/stage1a',
                        },
                        {
                            label: 'Stage 1B',
                            slug: 'educators-guide/stage1/stage1b',
                        },
                    ],
                },
                { label: 'Stage 2', slug: 'educators-guide/stage2' },
            ],
        },
    ],

    // Best Practices section
    '/best-practices': [
        {
            label: 'Best Practices',
            items: [
                {
                    label: 'Overview',
                    slug: 'best-practices/overview',
                },
                {
                    label: 'Git Usage',
                    slug: 'best-practices/git-usage',
                },
                {
                    label: 'GitHub Usage',
                    slug: 'best-practices/github-usage',
                },
                {
                    label: 'Code Formatter',
                    slug: 'best-practices/code-formatter',
                },
                {
                    label: 'CI Checks',
                    slug: 'best-practices/ci-checks',
                },
            ],
        },
    ],

    // Other Resources section (maps to /resources in content)
    '/other-resources': [
        {
            label: 'Resources',
            items: [
                { label: 'Overview', slug: 'resources' },
                { label: 'Glossary', slug: 'resources/glossary' },
                { label: 'Documentation', slug: 'resources/docs' },
            ],
        },
    ],

    // Contribution section
    '/contribution': [
        {
            label: 'Contribution',
            items: [
                {
                    label: 'Methods of Contributing',
                    slug: 'contribution/methodsofcontributing',
                },
                { label: 'Style Guide', slug: 'contribution/styleguide' },
                { label: 'Contributors', slug: 'contribution/contributors' },
                { label: 'Roadmap', slug: 'contribution/roadmap' },
            ],
        },
    ],
};

/**
 * Gets the sidebar configuration for a given URL path
 * Matches the most specific path prefix
 */
export function getSidebarForPath(pathname: string): SidebarSection[] {
    // Normalize pathname
    const normalizedPath = pathname.endsWith('/')
        ? pathname.slice(0, -1)
        : pathname;

    // Try to find exact match first
    if (sidebarSections[normalizedPath]) {
        return sidebarSections[normalizedPath];
    }

    // Find the longest matching prefix
    let bestMatch = '';
    for (const key of Object.keys(sidebarSections)) {
        if (
            key !== '/' &&
            normalizedPath.startsWith(key) &&
            key.length > bestMatch.length
        ) {
            bestMatch = key;
        }
    }

    return sidebarSections[bestMatch || '/'] ?? sidebarSections['/'] ?? [];
}

/**
 * Flattens sidebar items into a linear list of links for prev/next navigation
 */
function flattenSidebarItems(
    items: SidebarItem[],
): { label: string; href: string }[] {
    const result: { label: string; href: string }[] = [];

    for (const item of items) {
        if (item.slug) {
            result.push({ label: item.label, href: '/' + item.slug + '/' });
        }
        if (item.items) {
            result.push(...flattenSidebarItems(item.items));
        }
    }

    return result;
}

/**
 * Gets prev/next navigation links for a given path
 */
export function getPrevNextLinks(pathname: string): {
    prev: { label: string; href: string } | null;
    next: { label: string; href: string } | null;
} {
    const sections = getSidebarForPath(pathname);

    // Flatten all sections into a single list
    const allLinks: { label: string; href: string }[] = [];
    for (const section of sections) {
        allLinks.push(...flattenSidebarItems(section.items));
    }

    // Normalize the current path
    const normalizedPath = pathname.endsWith('/') ? pathname : pathname + '/';

    // Find current page index
    const currentIndex = allLinks.findIndex(
        (link) => link.href === normalizedPath,
    );

    if (currentIndex === -1) {
        return { prev: null, next: null };
    }
    const prev = allLinks.at(currentIndex - 1) ?? null;
    const next = allLinks.at(currentIndex + 1) ?? null;
    return {
        prev,
        next,
    };
}
