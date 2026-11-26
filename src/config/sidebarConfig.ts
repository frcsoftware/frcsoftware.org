// Sidebar configuration, consumed by starlight-sidebar-topics in astro.config.mjs.
// Each topic is one entry in the sidebar's topic dropdown and owns its own page tree.

import type {
    StarlightIcon,
    StarlightUserConfig,
} from '@astrojs/starlight/types';
import { defaultLang } from './locales';

type StarlightSidebarItem = NonNullable<StarlightUserConfig['sidebar']>[number];

// Labels should be keyed by locale code as in `src/config/locales.ts`, e.g. `en-US`, `fr-FR`, etc.
export type LocalizedLabels = Record<string, string>;

export interface SidebarTopic {
    label: string;
    translations?: LocalizedLabels;
    id: string;
    link: string;
    icon: StarlightIcon;
    items: SidebarItem[];
}

export type SidebarItem = {
    label: string;
    translations?: LocalizedLabels;
    slug?: string;
    items?: SidebarItem[];
    collapsed?: boolean;
};

const learningCourse: SidebarTopic = {
    label: 'Learning Course',
    translations: { 'zh-CN': '学习课程' },
    id: 'learning-course',
    link: '/learning-course/',
    icon: 'notes',
    items: [
        {
            label: 'Overview',
            translations: { 'zh-CN': '概览' },
            slug: 'learning-course',
        },
        {
            label: 'Website Feature Guide',
            translations: { 'zh-CN': '网站功能指南' },
            slug: 'learning-course/getting-started/website-feature-guide',
        },
        {
            label: 'Course Setup',
            translations: { 'zh-CN': '课程准备' },
            collapsed: true,
            items: [
                {
                    label: 'Required Tools',
                    translations: { 'zh-CN': '开发必备工具' },
                    slug: 'learning-course/getting-started/required-tools',
                },
                {
                    label: 'VS Code Overview',
                    translations: { 'zh-CN': 'VS Code 概述' },
                    slug: 'learning-course/getting-started/vscode-overview',
                },
                {
                    label: 'Forking and Cloning',
                    translations: { 'zh-CN': '派生仓库与克隆仓库' },
                    slug: 'learning-course/getting-started/forking-and-cloning',
                },
            ],
        },
        {
            label: 'Stage 0',
            translations: { 'zh-CN': '阶段 0' },
            collapsed: true,
            items: [
                {
                    label: 'Stage 0 Overview',
                    translations: { 'zh-CN': '阶段 0 概览' },
                    slug: 'learning-course/stage0/stage-overview',
                },
                {
                    label: 'Java Fundamentals',
                    translations: { 'zh-CN': 'Java基础' },
                    slug: 'learning-course/stage0/java-fundamentals',
                },
                {
                    label: 'Debugging',
                    translations: { 'zh-CN': '调试' },
                    slug: 'learning-course/stage0/debugging',
                },
                {
                    label: 'Git - Uploading code',
                    translations: { 'zh-CN': 'Git - 上传代码' },
                    slug: 'learning-course/stage0/git',
                },
                {
                    label: 'Operators',
                    translations: { 'zh-CN': '运算符' },
                    slug: 'learning-course/stage0/operators',
                },
                {
                    label: 'Conditionals',
                    translations: { 'zh-CN': '条件语句' },
                    slug: 'learning-course/stage0/conditionals',
                },
                {
                    label: 'Loops',
                    translations: { 'zh-CN': '循环' },
                    slug: 'learning-course/stage0/loops',
                },
                {
                    label: 'Classes and Objects',
                    translations: { 'zh-CN': '类与对象' },
                    slug: 'learning-course/stage0/classes-objects',
                },
                {
                    label: 'Methods and Mutable State',
                    translations: {
                        'zh-CN': '方法与可变状态',
                    },
                    slug: 'learning-course/stage0/classes-methods',
                },
                {
                    label: 'Arrays and For-Each Loops',
                    translations: {
                        'zh-CN': '数组与 for-each 循环',
                    },
                    slug: 'learning-course/stage0/arrays',
                },
                {
                    label: 'Interfaces, Generics, and Lists',
                    translations: {
                        'zh-CN': '接口、泛型与列表',
                    },
                    slug: 'learning-course/stage0/interfaces-lists',
                },
                {
                    label: 'Additional Resources',
                    translations: { 'zh-CN': '补充资源' },
                    slug: 'learning-course/stage0/additional-resources',
                },
                {
                    label: 'Stage 0 Wrap-Up',
                    translations: { 'zh-CN': '阶段 0 总结' },
                    slug: 'learning-course/stage0/stage-wrap-up',
                },
            ],
        },
        {
            label: 'Stage 1',
            translations: { 'zh-CN': '阶段 1' },
            collapsed: true,
            items: [
                {
                    label: 'Stage 1 Overview',
                    translations: { 'zh-CN': '阶段 1 概览' },
                    slug: 'learning-course/stage1/stage-overview',
                },
                {
                    label: 'Stage 1A: Kitbot Intro',
                    translations: { 'zh-CN': '阶段 1A：Kitbot 入门' },
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1A Overview',
                            translations: { 'zh-CN': '阶段 1A 概览' },
                            slug: 'learning-course/stage1/stage1a/stage-overview',
                        },
                        {
                            label: 'Getting Started',
                            translations: { 'zh-CN': '入门' },
                            slug: 'learning-course/stage1/stage1a/getting-started',
                        },
                        {
                            label: 'Kitbot Drivetrain',
                            translations: { 'zh-CN': 'Kitbot 传动系统' },
                            slug: 'learning-course/stage1/stage1a/kitbot-drivetrain',
                        },
                        {
                            label: 'Drivetrain Simulation',
                            translations: { 'zh-CN': '传动系统仿真' },
                            slug: 'learning-course/stage1/stage1a/drivetrain-sim',
                        },
                        {
                            label: 'Simple Auto',
                            translations: { 'zh-CN': '简单自动程序' },
                            slug: 'learning-course/stage1/stage1a/simple-auto',
                        },
                        {
                            label: 'Additional Motors',
                            translations: { 'zh-CN': '额外电机' },
                            slug: 'learning-course/stage1/stage1a/kitbot-additional-motors',
                        },
                        {
                            label: 'Stage 1A Wrap-Up',
                            translations: { 'zh-CN': '阶段 1A 总结' },
                            slug: 'learning-course/stage1/stage1a/stage-wrap-up',
                        },
                    ],
                },
                {
                    label: 'Stage 1B: Commands',
                    translations: { 'zh-CN': '阶段 1B：命令' },
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1B Overview',
                            translations: { 'zh-CN': '阶段 1B 概览' },
                            slug: 'learning-course/stage1/stage1b/stage-overview',
                        },
                        {
                            label: 'The Concepts',
                            translations: { 'zh-CN': '核心概念' },
                            slug: 'learning-course/stage1/stage1b/command-based-overview',
                        },
                        {
                            label: 'The Body of a Command',
                            translations: { 'zh-CN': '命令的主体' },
                            slug: 'learning-course/stage1/stage1b/the-command-body',
                        },
                        {
                            label: 'Commands & Mechanisms, Pt. 1',
                            translations: { 'zh-CN': '命令与机构（第一部分）' },
                            slug: 'learning-course/stage1/stage1b/commands-and-mechanisms',
                        },
                        {
                            label: 'Triggers and Scheduling',
                            translations: { 'zh-CN': '触发器与调度' },
                            slug: 'learning-course/stage1/stage1b/triggers',
                        },
                        {
                            label: 'Commands & Mechanisms, Pt. 2',
                            translations: { 'zh-CN': '命令与机构（第二部分）' },
                            slug: 'learning-course/stage1/stage1b/commands-and-mechanisms-pt2',
                        },
                        {
                            label: 'Bonus: Spot the Error',
                            translations: {
                                'zh-CN': '拓展练习：找出代码错误（第一部分）',
                            },
                            slug: 'learning-course/stage1/stage1b/spot-the-error',
                        },
                        {
                            label: 'Exercise - Kitbot Rewrite, Pt. 1',
                            translations: {
                                'zh-CN': '练习：Kitbot 代码重构（第一部分）',
                            },
                            slug: 'learning-course/stage1/stage1b/command-based-kitbot',
                        },
                        {
                            label: 'Suppliers in Command-Based',
                            translations: {
                                'zh-CN':
                                    '基于命令的编程中的 Supplier（供给器）',
                            },
                            slug: 'learning-course/stage1/stage1b/suppliers-in-command-based',
                        },
                        {
                            label: 'Exercise - Kitbot Rewrite, Pt. 2',
                            translations: {
                                'zh-CN': '练习：Kitbot 代码重构（第二部分）',
                            },
                            slug: 'learning-course/stage1/stage1b/command-based-kitbot-pt2',
                        },
                        {
                            label: 'Bonus: Spot the Error, Pt 2',
                            translations: {
                                'zh-CN': '附加：找出错误（第二部分）',
                            },
                            slug: 'learning-course/stage1/stage1b/spot-the-error-pt2',
                        },
                        {
                            label: 'Stage 1B Wrap-Up',
                            translations: { 'zh-CN': '阶段 1B 总结' },
                            slug: 'learning-course/stage1/stage1b/stage-wrap-up',
                        },
                    ],
                },
                {
                    label: 'Stage 1C: Control and Telemetry',
                    translations: { 'zh-CN': '阶段 1C：控制与遥测' },
                    collapsed: true,
                    items: [
                        {
                            label: 'Stage 1C Introduction',
                            translations: { 'zh-CN': '阶段 1C 介绍' },
                            slug: 'learning-course/stage1/stage1c/stage-overview',
                        },
                    ],
                },
            ],
        },
        {
            label: 'Stage 2',
            translations: { 'zh-CN': '阶段 2' },
            collapsed: true,
            items: [
                {
                    label: 'Stage 2 Overview',
                    translations: { 'zh-CN': '阶段 2 概览' },
                    slug: 'learning-course/stage2/stage-overview',
                },
            ],
        },
    ],
};

const educatorsGuide: SidebarTopic = {
    label: "Educator's Guide",
    translations: { 'zh-CN': '教师指南' },
    id: 'educators-guide',
    link: '/educators-guide/introduction/',
    icon: 'open-book',
    items: [
        {
            label: 'Introduction',
            translations: { 'zh-CN': '介绍' },
            slug: 'educators-guide/introduction',
        },
        {
            label: 'The Stages',
            translations: { 'zh-CN': '各个阶段' },
            slug: 'educators-guide/introduction/the-stages',
        },
        {
            label: 'Preparing Yourself',
            translations: { 'zh-CN': '做好准备' },
            slug: 'educators-guide/introduction/preparation',
        },
        {
            label: 'Stage 0',
            translations: { 'zh-CN': '阶段 0' },
            slug: 'educators-guide/stage0',
        },
        {
            label: 'Stage 1',
            translations: { 'zh-CN': '阶段 1' },
            collapsed: true,
            items: [
                {
                    label: 'Overview',
                    translations: { 'zh-CN': '概览' },
                    slug: 'educators-guide/stage1',
                },
                {
                    label: 'Stage 1A',
                    translations: { 'zh-CN': '阶段 1A' },
                    slug: 'educators-guide/stage1/stage1a',
                },
                {
                    label: 'Stage 1B',
                    translations: { 'zh-CN': '阶段 1B' },
                    slug: 'educators-guide/stage1/stage1b',
                },
                {
                    label: 'Stage 1C',
                    translations: { 'zh-CN': '阶段 1C' },
                    slug: 'educators-guide/stage1/stage1c',
                },
            ],
        },
        {
            label: 'Stage 2',
            translations: { 'zh-CN': '阶段 2' },
            slug: 'educators-guide/stage2',
        },
    ],
};

const bestPractices: SidebarTopic = {
    label: 'Best Practices',
    translations: { 'zh-CN': '最佳实践' },
    id: 'best-practices',
    link: '/best-practices/overview/',
    icon: 'approve-check-circle',
    items: [
        {
            label: 'Overview',
            translations: { 'zh-CN': '概览' },
            slug: 'best-practices/overview',
        },
        {
            label: 'Git Usage',
            translations: { 'zh-CN': 'Git 使用' },
            slug: 'best-practices/git-usage',
        },
        {
            label: 'GitHub Usage',
            translations: { 'zh-CN': 'GitHub 使用' },
            slug: 'best-practices/github-usage',
        },
        {
            label: 'Code Formatter',
            translations: { 'zh-CN': '代码格式化' },
            slug: 'best-practices/code-formatter',
        },
        {
            label: 'CI Checks',
            translations: { 'zh-CN': 'CI 检查' },
            slug: 'best-practices/ci-checks',
        },
    ],
};

const resources: SidebarTopic = {
    label: 'Resources',
    translations: { 'zh-CN': '资源' },
    id: 'resources',
    link: '/resources/overview/',
    icon: 'document',
    items: [
        {
            label: 'Overview',
            translations: { 'zh-CN': '概览' },
            slug: 'resources/overview',
        },
        {
            label: 'Examples',
            translations: { 'zh-CN': '示例' },
            slug: 'resources/examples',
        },
        {
            label: 'Documentation',
            translations: { 'zh-CN': '文档' },
            slug: 'resources/docs',
        },
        {
            label: 'Hardware',
            translations: { 'zh-CN': '硬件' },
            slug: 'resources/hardware-intro',
        },
        {
            label: 'Glossary',
            translations: { 'zh-CN': '术语表' },
            slug: 'resources/glossary',
        },
    ],
};

const contribution: SidebarTopic = {
    label: 'Contribution',
    translations: { 'zh-CN': '贡献' },
    id: 'contribution',
    link: '/contribution/',
    icon: 'code-branch',
    items: [
        {
            label: 'Overview',
            translations: { 'zh-CN': '概览' },
            slug: 'contribution',
        },
        {
            label: 'Methods of Contributing',
            translations: { 'zh-CN': '贡献方式' },
            slug: 'contribution/methodsofcontributing',
        },
        {
            label: 'Style Guide',
            translations: { 'zh-CN': '风格指南' },
            slug: 'contribution/styleguide',
        },
        {
            label: 'Contributors',
            translations: { 'zh-CN': '贡献者' },
            slug: 'contribution/contributors',
        },
        {
            label: 'Roadmap',
            translations: { 'zh-CN': '路线图' },
            slug: 'contribution/roadmap',
        },
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
            translations: item.translations,
            collapsed: item.collapsed ?? false,
            items: item.items.map(convertItem),
        };
    }
    return {
        label: item.label,
        translations: item.translations,
        link: '/' + item.slug + '/',
    };
}

export const sidebarTopics = topics.map(({ translations, ...topic }) => ({
    ...topic,
    label: translations
        ? { [defaultLang]: topic.label, ...translations }
        : topic.label,
    items: topic.items.map(convertItem),
}));
