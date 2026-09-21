// Sidebar configuration, consumed by starlight-sidebar-topics in astro.config.mjs.
// Each topic is one entry in the sidebar's topic dropdown and owns its own page tree.

import type {
    StarlightIcon,
    StarlightUserConfig,
} from '@astrojs/starlight/types';

type StarlightSidebarItem = NonNullable<StarlightUserConfig['sidebar']>[number];

export interface SidebarTopic {
    label: string;
    id: string;
    link: string;
    icon: StarlightIcon;
    items: SidebarItem[];
}

export type SidebarItem = {
    label: string;
    slug?: string;
    items?: SidebarItem[];
    collapsed?: boolean;
};

const learningCourse: SidebarTopic = {
    label: 'Learning Course',
    id: 'learning-course',
    link: '/learning-course/',
    icon: 'notes',
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
                    label: 'Stage 0 Overview',
                    slug: 'learning-course/stage0/stage-overview',
                },
                {
                    label: 'Java Fundamentals',
                    slug: 'learning-course/stage0/java-fundamentals',
                },
                {
                    label: 'Debugging',
                    slug: 'learning-course/stage0/debugging',
                },
                {
                    label: 'Operators',
                    slug: 'learning-course/stage0/operators',
                },
                {
                    label: 'Conditionals',
                    slug: 'learning-course/stage0/conditionals',
                },
                { label: 'Loops', slug: 'learning-course/stage0/loops' },
                {
                    label: 'Classes and Objects',
                    slug: 'learning-course/stage0/classes-objects',
                },
                {
                    label: 'Methods and Mutable State',
                    slug: 'learning-course/stage0/classes-methods',
                },
                {
                    label: 'Arrays and For-Each Loops',
                    slug: 'learning-course/stage0/arrays',
                },
                {
                    label: 'Interfaces, Generics, and Lists',
                    slug: 'learning-course/stage0/interfaces-lists',
                },
                {
                    label: 'Additional Resources',
                    slug: 'learning-course/stage0/additional-resources',
                },
                {
                    label: 'Stage 0 Wrap-Up',
                    slug: 'learning-course/stage0/stage-wrap-up',
                },
            ],
        },
        {
            label: 'Stage 1',
            collapsed: true,
            items: [
                {
                    label: 'Stage 1 Overview',
                    slug: 'learning-course/stage1/stage-overview',
                },
                {
                    label: 'Stage 1A: Kitbot Intro',
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1A Overview',
                            slug: 'learning-course/stage1/stage1a/stage-overview',
                        },
                        {
                            label: 'Getting Started',
                            slug: 'learning-course/stage1/stage1a/getting-started',
                        },
                        {
                            label: 'Kitbot Drivetrain',
                            slug: 'learning-course/stage1/stage1a/kitbot-drivetrain',
                        },
                        {
                            label: 'Drivetrain Simulation',
                            slug: 'learning-course/stage1/stage1a/drivetrain-sim',
                        },
                        {
                            label: 'Simple Auto',
                            slug: 'learning-course/stage1/stage1a/simple-auto',
                        },
                        {
                            label: 'Additional Motors',
                            slug: 'learning-course/stage1/stage1a/kitbot-additional-motors',
                        },
                        {
                            label: 'Stage 1A Wrap-Up',
                            slug: 'learning-course/stage1/stage1a/stage-wrap-up',
                        },
                    ],
                },
                {
                    label: 'Stage 1B: Commands',
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1B Overview',
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
                        {
                            label: 'Stage 1B Wrap-Up',
                            slug: 'learning-course/stage1/stage1b/stage-wrap-up',
                        },
                    ],
                },
                {
                    label: 'Stage 1C: Control and Telemetry',
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1C Introduction',
                            slug: 'learning-course/stage1/stage1c/stage-overview',
                        },
                    ],
                },
            ],
        },
        {
            label: 'Stage 2',
            collapsed: true,
            items: [
                {
                    label: 'Stage 2 Overview',
                    slug: 'learning-course/stage2/stage-overview',
                },
            ],
        },
    ],
};

const educatorsGuide: SidebarTopic = {
    label: "Educator's Guide",
    id: 'educators-guide',
    link: '/educators-guide/introduction/',
    icon: 'open-book',
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
                { label: 'Stage 1A', slug: 'educators-guide/stage1/stage1a' },
                { label: 'Stage 1B', slug: 'educators-guide/stage1/stage1b' },
                { label: 'Stage 1C', slug: 'educators-guide/stage1/stage1c' },
            ],
        },
        { label: 'Stage 2', slug: 'educators-guide/stage2' },
    ],
};

const bestPractices: SidebarTopic = {
    label: 'Best Practices',
    id: 'best-practices',
    link: '/best-practices/overview/',
    icon: 'approve-check-circle',
    items: [
        { label: 'Overview', slug: 'best-practices/overview' },
        { label: 'Git Usage', slug: 'best-practices/git-usage' },
        { label: 'GitHub Usage', slug: 'best-practices/github-usage' },
        { label: 'Code Formatter', slug: 'best-practices/code-formatter' },
        { label: 'CI Checks', slug: 'best-practices/ci-checks' },
    ],
};

const resources: SidebarTopic = {
    label: 'Resources',
    id: 'resources',
    link: '/resources/overview/',
    icon: 'document',
    items: [
        { label: 'Overview', slug: 'resources/overview' },
        { label: 'Examples', slug: 'resources/examples' },
        { label: 'Documentation', slug: 'resources/docs' },
        { label: 'Hardware', slug: 'resources/hardware-intro' },
        { label: 'Glossary', slug: 'resources/glossary' },
    ],
};

const contribution: SidebarTopic = {
    label: 'Contribution',
    id: 'contribution',
    link: '/contribution/',
    icon: 'code-branch',
    items: [
        { label: 'Overview', slug: 'contribution' },
        {
            label: 'Methods of Contributing',
            slug: 'contribution/methodsofcontributing',
        },
        { label: 'Style Guide', slug: 'contribution/styleguide' },
        { label: 'Contributors', slug: 'contribution/contributors' },
        { label: 'Roadmap', slug: 'contribution/roadmap' },
    ],
};

const topics: SidebarTopic[] = [
    learningCourse,
    educatorsGuide,
    bestPractices,
    resources,
    contribution,
];

function convertItem(item: SidebarItem): StarlightSidebarItem {
    if (item.items) {
        return {
            label: item.label,
            collapsed: item.collapsed ?? false,
            items: item.items.map(convertItem),
        };
    }
    return { label: item.label, link: '/' + item.slug + '/' };
}

export const sidebarTopics = topics.map((topic) => ({
    ...topic,
    items: topic.items.map(convertItem),
}));
