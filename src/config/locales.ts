// localization configuration; all locales must live here

/** BCP-47 tag of the content that lives at unprefixed URLs. */
export const defaultLang = 'en';

// adding a new locale requires adding a directory under src/content/docs/ and
// adding an entry here. The directory name must match `lang`.
const configuredLocales: Record<string, { label: string; lang: string }> = {
    root: { label: 'English', lang: defaultLang },
};

export const langByLocaleDir: Record<string, string> = Object.fromEntries(
    Object.entries(configuredLocales)
        .filter(([dir]) => dir !== 'root')
        .map(([dir, { lang }]) => [dir, lang]),
);

export const localeDirs = Object.keys(langByLocaleDir);

export const isMultilingual = localeDirs.length > 0;

export const locales = isMultilingual ? configuredLocales : undefined;

const DOCS_ROOT = 'src/content/docs/';

// locale directory if the file path is under src/content/docs/<localeDir>/...
// undefined for the root locale
export function localeDirFromDocsPath(
    filePath: string | undefined,
): string | undefined {
    if (!filePath) return undefined;

    const normalized = filePath.replace(/\\/g, '/');
    const index = normalized.lastIndexOf(DOCS_ROOT);
    if (index === -1) return undefined;

    const segment = normalized
        .slice(index + DOCS_ROOT.length)
        .split('/')[0]
        ?.replace(/\.mdx?$/, '');

    return segment && segment in langByLocaleDir ? segment : undefined;
}

export function langFromDocsPath(filePath: string | undefined): string {
    const dir = localeDirFromDocsPath(filePath);
    return dir ? langByLocaleDir[dir]! : defaultLang;
}

// Page URLs are extensionless, so a last segment with a dot is a file in
// public/, which lives at one path no matter the locale.
function isPageUrl(url: string): boolean {
    if (!url.startsWith('/') || url.startsWith('//')) return false;

    const [path = ''] = url.split(/[?#]/);
    const lastSegment = path.replace(/\/$/, '').split('/').pop() ?? '';
    return !lastSegment.includes('.');
}

/**
 * Prefixes a root-relative page URL with a locale directory. Anything already
 * in the locale, external, or pointing at public/ is returned unchanged.
 *
 * `localeDir` doubles as the BCP-47 tag, so `Astro.currentLocale` can be passed
 * straight in.
 */
export function localizeHref(
    href: string,
    localeDir: string | undefined,
): string {
    if (!localeDir || !(localeDir in langByLocaleDir)) return href;
    if (!isPageUrl(href)) return href;
    if (href === `/${localeDir}` || href.startsWith(`/${localeDir}/`)) {
        return href;
    }

    return `/${localeDir}${href}`;
}
