import { defineCollection } from 'astro:content';
import { file } from 'astro/loaders';
import { z } from 'astro/zod';
import { docsLoader, i18nLoader } from '@astrojs/starlight/loaders';
import { docsSchema, i18nSchema } from '@astrojs/starlight/schema';
import { mergedGlossaryEntries } from './data/loadGlossary';

export const collections = {
    docs: defineCollection({ loader: docsLoader(), schema: docsSchema() }),
    i18n: defineCollection({
        loader: i18nLoader(),
        schema: i18nSchema({
            extend: z.object({
                'aside.example': z.string().optional(),
                'aside.exercise': z.string().optional(),
                'aside.video': z.string().optional(),
                'aside.wip': z.string().optional(),
                'banner.development': z.string().optional(),
                'header.menuToggle': z.string().optional(),
                'banner.developmentLinkText': z.string().optional(),
                'courseSection.underConstruction': z.string().optional(),
                'glossary.definitionNotFound': z.string().optional(),
                'footer.github': z.string().optional(),
                'footer.discord': z.string().optional(),
                'card.copyLink': z.string().optional(),
                'slides.previous': z.string().optional(),
                'slides.next': z.string().optional(),
                'slides.goTo': z.string().optional(),
                'slides.closeLightbox': z.string().optional(),
                'video.unsupported': z.string().optional(),
                'video.youTubeTitle': z.string().optional(),
            }),
        }),
    }),
    // merge glossary entries from all languages into a single collection, keyed by `<lang>:<englishTerm>`
    glossary: defineCollection({
        loader: file('src/data/glossary.yaml', {
            parser: (text) => mergedGlossaryEntries(text),
        }),
        schema: z.object({
            lang: z.string(),
            key: z.string(),
            term: z.string(),
            definition: z.string(),
            caseSensitive: z.boolean(),
        }),
    }),
};
