import type { StarlightUserConfig } from '@astrojs/starlight/types';
import { sidebarSections, type SidebarItem } from './sidebarConfig';

type StarlightSidebarItem = NonNullable<StarlightUserConfig['sidebar']>[number];

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

export const sidebarTopics = [
    {
        label: 'Learning Course',
        id: 'learning-course',
        link: '/learning-course/',
        icon: 'notes',
        items: sidebarSections['/learning-course']![0]!.items.map(convertItem),
    },
    {
        label: "Educator's Guide",
        id: 'educators-guide',
        link: '/educators-guide/introduction/',
        icon: 'open-book',
        items: sidebarSections['/educators-guide']![0]!.items.map(convertItem),
    },
    {
        label: 'Best Practices',
        id: 'best-practices',
        link: '/best-practices/overview/',
        icon: 'approve-check-circle',
        items: sidebarSections['/best-practices']![0]!.items.map(convertItem),
    },
    {
        label: 'Resources',
        id: 'resources',
        link: '/resources/overview/',
        icon: 'document',
        items: sidebarSections['/resources']![0]!.items.map(convertItem),
    },
    {
        label: 'Contribution',
        id: 'contribution',
        link: '/contribution/',
        icon: 'code-branch',
        items: [
            { label: 'Overview', link: '/contribution/' },
            ...sidebarSections['/contribution']![0]!.items.map(convertItem),
        ],
    },
];
