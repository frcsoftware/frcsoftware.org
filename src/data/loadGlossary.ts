// Loads and merges the glossary YAML files and compiles the term matchers.
// remark-glossary, the `glossary` collection, and setup-vale.ts all read
// glossary data through here.
//
// glossary.yaml is the root source of truth for every term; locale glossaries key off the canonical English term,
// which is the identity of every entry. A glossary.<locale>.yaml sibling keys off those same
// terms and may translate `term` (the word matched and displayed) and
// `definition`. Anything it omits falls back to English.

import { readFileSync, existsSync } from 'node:fs';
import { parse } from 'yaml';
import { defaultLang, langByLocaleDir } from '../config/locales';

interface RawEntry {
    definition?: string;
    /** Word to match in prose and display. Defaults to the English term. */
    term?: string;
    caseSensitive?: boolean;
    /** Whether the English term is also matched in this locale's prose. */
    matchEnglish?: boolean;
}

// A type alias, not an interface: only the former gets the implicit index
// signature that Astro's `file()` parser output requires.
export type GlossaryEntry = {
    lang: string;
    /** Canonical English term; stable across locales. */
    key: string;
    /** Word as matched and displayed in this locale. */
    term: string;
    definition: string;
    caseSensitive: boolean;
};

function read(fileName: string): Record<string, RawEntry> | undefined {
    const url = new URL(`./${fileName}`, import.meta.url);
    if (!existsSync(url)) return undefined;
    return parse(readFileSync(url, 'utf-8')) as Record<string, RawEntry>;
}

interface Glossary {
    english: Record<string, RawEntry>;
    byLang: Map<string, Record<string, RawEntry>>;
    langs: string[];
}

function loadAll(englishSource?: string): Glossary {
    const english =
        (englishSource !== undefined
            ? (parse(englishSource) as Record<string, RawEntry>)
            : read('glossary.yaml')) ?? {};

    const byLang = new Map<string, Record<string, RawEntry>>();
    for (const [dir, lang] of Object.entries(langByLocaleDir)) {
        const raw = read(`glossary.${dir}.yaml`);
        if (!raw) continue;

        for (const key of Object.keys(raw)) {
            if (!(key in english)) {
                throw new Error(
                    `glossary.${dir}.yaml defines "${key}", which is not a term in glossary.yaml. ` +
                        `Locale glossaries key off the canonical English term.`,
                );
            }
        }
        byLang.set(lang, raw);
    }

    return { english, byLang, langs: [defaultLang, ...byLang.keys()] };
}

const cached = loadAll();

/** Every language with glossary data, default language first. */
export const glossaryLangs = cached.langs;

function mergeEntry(
    glossary: Glossary,
    lang: string,
    key: string,
): GlossaryEntry {
    const base = glossary.english[key]!;
    const override = glossary.byLang.get(lang)?.[key];

    return {
        lang,
        key,
        term: override?.term ?? key,
        definition: override?.definition ?? base.definition ?? '',
        caseSensitive: override?.caseSensitive ?? base.caseSensitive ?? false,
    };
}

/**
 * Effective glossary for a language: every canonical term, with translations
 * applied and English filled in for anything the locale omits.
 */
export function glossaryFor(lang: string): GlossaryEntry[] {
    return Object.keys(cached.english).map((key) =>
        mergeEntry(cached, lang, key),
    );
}

/**
 * Every language's entries, keyed `<lang>:<englishTerm>` for the content
 * collection. The key is opaque; display and matching use `term`.
 *
 * Reads from disk rather than the module-scope cache so that editing any
 * glossary file invalidates the collection during `astro dev`.
 */
export function mergedGlossaryEntries(
    englishSource?: string,
): Record<string, GlossaryEntry> {
    const glossary = loadAll(englishSource);
    const merged: Record<string, GlossaryEntry> = {};

    for (const lang of glossary.langs) {
        for (const key of Object.keys(glossary.english)) {
            merged[`${lang}:${key}`] = mergeEntry(glossary, lang, key);
        }
    }
    return merged;
}

export function escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/** Collapses runs of whitespace so multi-word terms survive a soft line break. */
function normalizeSpace(str: string): string {
    return str.replace(/\s+/g, ' ');
}

function toPattern(alternative: string, caseSensitive: boolean): string {
    // A soft line break stays inside one mdast text node, so a space in a
    // multi-word term has to match a newline too.
    const escaped = escapeRegex(alternative).replace(/\s+/g, '\\s+');
    return caseSensitive ? `(?-i:${escaped})` : escaped;
}

export interface GlossaryMatcher {
    pattern: RegExp;
    /** Resolves matched source text back to its canonical English term. */
    canonicalFor(matched: string): string | undefined;
}

function buildMatcher(lang: string): GlossaryMatcher {
    const override = cached.byLang.get(lang);

    const alternatives: {
        text: string;
        key: string;
        caseSensitive: boolean;
    }[] = [];
    const canonical = new Map<string, string>();

    for (const entry of glossaryFor(lang)) {
        const forms = [entry.term];
        // A locale's prose often still uses the English term, so match both
        // unless the locale opts out (e.g. the English letters spell an
        // ordinary word in that language).
        if (
            entry.term !== entry.key &&
            (override?.[entry.key]?.matchEnglish ?? true)
        ) {
            forms.push(entry.key);
        }

        for (const text of forms) {
            alternatives.push({
                text,
                key: entry.key,
                caseSensitive: entry.caseSensitive,
            });
            canonical.set(normalizeSpace(text).toLowerCase(), entry.key);
        }
    }

    // Alternation is first-match-wins, not longest-match, so longer alternatives
    // come first. The sort is on the matched text rather than the canonical key,
    // which says nothing about a translation's length, and the tiebreak keeps
    // equal-length terms (PDH/CAN/PWM) in a stable order.
    alternatives.sort(
        (a, b) => b.text.length - a.text.length || a.text.localeCompare(b.text),
    );

    const pattern = new RegExp(
        `(?<![\\p{L}\\p{N}\\p{M}_])(${alternatives
            .map(({ text, caseSensitive }) => toPattern(text, caseSensitive))
            .join('|')})(?![\\p{L}\\p{N}\\p{M}_])`,
        'giu',
    );

    return {
        pattern,
        canonicalFor: (matched) =>
            canonical.get(normalizeSpace(matched).toLowerCase()),
    };
}

const matchers = new Map(
    glossaryLangs.map((lang) => [lang, buildMatcher(lang)] as const),
);

export function matcherFor(lang: string): GlossaryMatcher {
    return matchers.get(lang) ?? matchers.get(defaultLang)!;
}
