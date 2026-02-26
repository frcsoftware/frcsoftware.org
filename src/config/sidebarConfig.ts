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

// Define which URL paths belong to which sidebar section
export const sidebarSections: Record<string, SidebarSection[]> = {
  // Home page - minimal sidebar or none
  '/': [],

  // Feature Guide section
  '/feature-guide': [
    {
      label: 'Website Feature Guide',
      items: [
        { label: 'Overview', slug: 'feature-guide' },
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
      ],
    },
  ],

  // Contribution section
  '/contribution': [
    {
      label: 'Contribution',
      items: [
        { label: 'Methods of Contributing', slug: 'contribution/methodsofcontributing' },
        { label: 'Style Guide', slug: 'contribution/styleguide' },
        { label: 'Contributors', slug: 'contribution/contributors' },
        { label: 'Roadmap', slug: 'contribution/roadmap' },
      ],
    },
  ],

  // Getting Started section
  '/getting-started': [
    {
      label: 'Getting Started',
      items: [
        { label: 'Intro to Programmming', slug: 'getting-started/intro-to-prog' },
        { label: 'Required Tools', slug: 'getting-started/required-tools' },
        { label: 'VS Code Overview', slug: 'getting-started/vscode-overview' },
        { label: 'AdvantageScope Overview', slug: 'getting-started/ascope-overview' },
        { label: 'Forking and Cloning', slug: 'getting-started/forking-and-cloning' },
      ],
    },
  ],

  // Resources section (content lives at /resources but navbar says "Other Resources")
  // '/resources': [
  //   {
  //     label: 'Resources',
  //     items: [
  //       { label: 'Overview', slug: 'resources' },
  //       { label: 'Glossary', slug: 'resources/glossary' },
  //     ],
  //   },
  // ],
};

/**
 * Gets the sidebar configuration for a given URL path
 * Matches the most specific path prefix
 */
export function getSidebarForPath(pathname: string): SidebarSection[] {
  // Normalize pathname
  const normalizedPath = pathname.endsWith('/') ? pathname.slice(0, -1) : pathname;

  // Try to find exact match first
  if (sidebarSections[normalizedPath]) {
    return sidebarSections[normalizedPath];
  }

  // Find the longest matching prefix
  let bestMatch = '';
  for (const key of Object.keys(sidebarSections)) {
    if (key !== '/' && normalizedPath.startsWith(key) && key.length > bestMatch.length) {
      bestMatch = key;
    }
  }

  if (bestMatch) {
    return sidebarSections[bestMatch];
  }

  // Default to home (empty sidebar)
  return sidebarSections['/'] || [];
}

/**
 * Flattens sidebar items into a linear list of links for prev/next navigation
 */
function flattenSidebarItems(items: SidebarItem[]): { label: string; href: string }[] {
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
export function getPrevNextLinks(pathname: string): { prev: { label: string; href: string } | null; next: { label: string; href: string } | null } {
  const sections = getSidebarForPath(pathname);

  // Flatten all sections into a single list
  const allLinks: { label: string; href: string }[] = [];
  for (const section of sections) {
    allLinks.push(...flattenSidebarItems(section.items));
  }

  // Normalize the current path
  const normalizedPath = pathname.endsWith('/') ? pathname : pathname + '/';

  // Find current page index
  const currentIndex = allLinks.findIndex(link => link.href === normalizedPath);

  if (currentIndex === -1) {
    return { prev: null, next: null };
  }

  return {
    prev: currentIndex > 0 ? allLinks[currentIndex - 1] : null,
    next: currentIndex < allLinks.length - 1 ? allLinks[currentIndex + 1] : null,
  };
}
