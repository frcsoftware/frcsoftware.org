import {
    createWriteStream,
    writeFileSync,
    readFileSync,
    copyFileSync,
    existsSync,
    mkdirSync,
} from 'fs';
import { resolve } from 'path';
import { fileURLToPath } from 'url';
import { parse } from 'yaml';
import { pipeline } from 'stream/promises';

const ROOT = fileURLToPath(new URL('..', import.meta.url));

const DICT_DIR = resolve(ROOT, '.styles/config/dictionaries');
const DIC_PATH = resolve(DICT_DIR, 'en_US.dic');
const AFF_PATH = resolve(DICT_DIR, 'en_US.aff');

const GLOSSARY_PATH = resolve(ROOT, 'src/data/glossary.yaml');
const ACCEPT_WORDS_PATH = resolve(ROOT, 'vale-accept-words.txt');
const SPELLING_RULE_PATH = resolve(ROOT, 'scripts/vale-spelling-rule.yml');

const VOCAB_DIR = resolve(ROOT, '.styles/config/vocabularies/Frcsoftware');
const STYLE_DIR = resolve(ROOT, '.styles/Frcsoftware');

async function downloadFile(url: string, path: string) {
    const response = await fetch(url);
    if (!response.ok)
        throw new Error(
            `Error downloading dictionaries: HTTP error! status: ${response.status}`,
        );

    if (!response.body)
        throw new Error(
            `Error downloading dictionaries: No body in response for ${url}`,
        );
    const fileStream = createWriteStream(path);
    await pipeline(response.body, fileStream);
}

// Vale's spelling check needs a Hunspell dictionary; we pin a specific
// LibreOffice release rather than relying on whatever Vale bundles.
async function ensureDictionaries() {
    if (existsSync(DIC_PATH) && existsSync(AFF_PATH)) return;

    mkdirSync(DICT_DIR, { recursive: true });
    const version = 'libreoffice-26.2.5.1';
    await Promise.all([
        downloadFile(
            `https://raw.githubusercontent.com/LibreOffice/dictionaries/refs/tags/${version}/en/en_US.dic`,
            DIC_PATH,
        ),
        downloadFile(
            `https://raw.githubusercontent.com/LibreOffice/dictionaries/refs/tags/${version}/en/en_US.aff`,
            AFF_PATH,
        ),
    ]);
}

// Hunspell .dic files are "<word count>\n<word>/<affix flags>\n...".
function loadDictionaryWords(): Set<string> {
    const [, ...lines] = readFileSync(DIC_PATH, 'utf-8').split('\n');
    const words = new Set<string>();
    for (const line of lines) {
        const word = (line.split('/')[0] ?? '').trim();
        if (word) words.add(word.toLowerCase());
    }
    return words;
}

function escapeRegExp(value: string): string {
    return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

type AcceptWord = { term: string; caseSensitive: boolean };

// vale-accept-words.txt is a flat word list split into "[case-insensitive]"
// and "[case-sensitive]" sections; words before any section header are
// treated as case-insensitive.
function loadAcceptWords(): AcceptWord[] {
    const words: AcceptWord[] = [];
    let caseSensitive = false;

    for (const rawLine of readFileSync(ACCEPT_WORDS_PATH, 'utf-8').split(
        '\n',
    )) {
        const line = rawLine.trim();
        if (!line || line.startsWith('#')) continue;

        const section = /^\[(.+)]$/.exec(line)?.[1];
        if (section) {
            if (section !== 'case-sensitive' && section !== 'case-insensitive')
                throw new Error(
                    `Unknown section "[${section}]" in ${ACCEPT_WORDS_PATH}`,
                );
            caseSensitive = section === 'case-sensitive';
            continue;
        }

        words.push({ term: line, caseSensitive });
    }

    return words;
}

function loadGlossaryTerms(): AcceptWord[] {
    const glossary = parse(readFileSync(GLOSSARY_PATH, 'utf-8')) as Record<
        string,
        { definition: string; caseSensitive?: boolean }
    >;

    return Object.entries(glossary).map(([term, { caseSensitive }]) => ({
        term,
        caseSensitive: caseSensitive ?? false,
    }));
}

// Builds Vale's Vocab accept list (`.styles/config/vocabularies/Frcsoftware/accept.txt`)
// from two committed sources:
//   - vale-accept-words.txt: plain jargon words.
//   - src/data/glossary.yaml: terms that also get a tooltip definition on the site.
// Both mark a term as case-sensitive the same way, and a case-sensitive term is
// enforced with its exact casing (e.g. "WPILib") via Vale.Terms, UNLESS its
// lowercase form is itself a real English word (e.g. "CAN"), in which case
// enforcing casing would flag ordinary prose ("can you...") as an error, so it
// falls back to case-insensitive acceptance.
function buildAcceptEntries(dictionaryWords: Set<string>): string[] {
    const entries = new Set<string>();

    for (const { term, caseSensitive } of [
        ...loadGlossaryTerms(),
        ...loadAcceptWords(),
    ]) {
        const enforceCase =
            caseSensitive && !dictionaryWords.has(term.toLowerCase());
        entries.add(
            enforceCase ? escapeRegExp(term) : `(?i)${escapeRegExp(term)}`,
        );
    }

    return [...entries].sort((a, b) =>
        a.toLowerCase().localeCompare(b.toLowerCase()),
    );
}

await ensureDictionaries();

const acceptEntries = buildAcceptEntries(loadDictionaryWords());
mkdirSync(VOCAB_DIR, { recursive: true });
writeFileSync(
    resolve(VOCAB_DIR, 'accept.txt'),
    acceptEntries.join('\n') + '\n',
);
console.log(`Wrote ${acceptEntries.length} accepted terms to Vale vocabulary.`);

mkdirSync(STYLE_DIR, { recursive: true });
copyFileSync(SPELLING_RULE_PATH, resolve(STYLE_DIR, 'Spelling.yml'));
