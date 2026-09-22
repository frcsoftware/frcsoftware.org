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
