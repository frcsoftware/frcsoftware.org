import { defineCollection } from 'astro:content';
import { file } from 'astro/loaders';
import { z } from 'astro/zod';
import { docsLoader } from '@astrojs/starlight/loaders';
import { docsSchema } from '@astrojs/starlight/schema';

export const collections = {
    docs: defineCollection({ loader: docsLoader(), schema: docsSchema() }),
    glossary: defineCollection({
        loader: file('src/data/glossary.yaml'),
        schema: z.object({
            definition: z.string(),
            caseSensitive: z.boolean().optional(),
        }),
    }),
};
