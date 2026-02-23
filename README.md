# FRCSoftware.org

A community-driven learning course and resource hub for FRC software. Built with [Astro](https://astro.build) and [Starlight](https://starlight.astro.build).

## Project Structure

```
.
├── public/                    # Static assets (favicon)
├── src/
│   ├── assets/
│   │   ├── content/          # Optimized images for documentation
│   │   ├── footer/           # Footer assets
│   │   ├── header/           # Header assets
│   │   ├── home/             # Homepage assets
│   │   └── universal/        # Shared assets
│   ├── components/           # Custom Astro components
│   │   ├── Aside.astro       # Styled callout boxes (note, tip, caution, danger, example)
│   │   ├── Countdown.astro   # Countdown timer component
│   │   ├── Glossary.astro    # Glossary term definitions
│   │   ├── HomeCard.astro    # Homepage card component
│   │   ├── LinkButton.astro  # Styled link buttons
│   │   ├── Slides.astro      # Image/video slideshow with lightbox
│   │   └── YouTube.astro     # YouTube video embeds
│   ├── config/               # Sidebar configuration
│   ├── content/
│   │   └── docs/             # MDX documentation pages
│   ├── data/                 # Static data files
│   ├── plugins/              # Remark/Rehype plugins (center, figure, glossary, image-attributes)
│   ├── starlightOverrides/   # Custom Starlight component overrides
│   │   ├── Footer.astro      # Custom footer
│   │   ├── Header.astro      # Custom header with navigation
│   │   ├── Hero.astro        # Custom hero component
│   │   ├── Pagination.astro  # Custom pagination
│   │   └── Sidebar.astro     # Custom sidebar
│   └── styles/               # Global styles
├── astro.config.mjs          # Astro configuration
├── package.json
└── tsconfig.json
```

## Custom Components

### Aside

Styled callout boxes with optional collapse functionality.

```mdx
import Aside from '@components/Aside.astro';

<Aside type="tip">This is a tip!</Aside>
<Aside type="note" title="Custom Title">Content here</Aside>
<Aside type="caution" collapse>Collapsible content</Aside>
```

Types: `note`, `tip`, `caution`, `danger`, `example`

### Slides

Image and video slideshow with lightbox support. Images are automatically optimized when placed in `src/assets/content/`.

**Format:** Each slide is an image immediately followed by its caption:

```mdx
import Slides from '@components/Slides.astro';

<Slides>
  ![alt text](/path/to/image1.webp)
  Caption for slide 1

  ![](/path/to/image2.webp)
  Caption for slide 2

  ![](https://www.youtube.com/embed/VIDEO_ID)
  Caption for YouTube video
</Slides>
```

**With custom scale** (0.1 to 1, default is 0.8):

```mdx
<Slides scale={0.6}>
  ![](/path/to/image.webp)
  A smaller slideshow at 60% width
</Slides>
```

Props:
- `scale`: Controls the width of the slideshow (default: `0.8` = 80% width)

Supported media types:
- Images (`.webp`, `.png`, `.jpg`, `.jpeg`) - automatically optimized
- YouTube videos (watch URLs, embed URLs, or short URLs) - displayed at 16:9
- Video files (`.webm`, `.mp4`)

### YouTube

YouTube video embed with optional caption.

```mdx
import YouTube from '@components/YouTube.astro';

<YouTube id="VIDEO_ID" />
<YouTube url="https://www.youtube.com/watch?v=VIDEO_ID">
  Optional caption with **markdown** support
</YouTube>
```

### LinkButton

Styled button link component.

```mdx
import LinkButton from '@components/LinkButton.astro';

<LinkButton href="/path">Button Text</LinkButton>
<LinkButton href="/path" center>Centered Button</LinkButton>
<LinkButton href="/path" blank={false}>Internal Link (no new tab)</LinkButton>
```

Props:
- `href` (required): Link URL
- `center`: Centers the button (default: `false`)
- `blank`: Opens in new tab (default: `true`)

## Centered Text

Use the `:::center` block directive to center text content:

```markdown
:::center
**Centered text with markdown**
:::
```

This works with any content including text, images, videos, or other elements. The directive uses the [remark-directive](https://github.com/remarkjs/remark-directive) syntax (already included in Starlight).

## CustomCard
Cards with styling custom for the website. Can be used with [CardGrid](https://starlight.astro.build/components/card-grids/)

```mdx
import CustomCard from '@components/CustomCard.astro';

<CustomCard title="Visible title" subTitle="Italicized unlinked text next to title" href="hyperlink for the title">

   Any markdown text, images, components or styling.

</CustomCard>

<CustomCard title="Visible title" subTitle="Italicized unlinked text next to title" href="hyperlink for the title" />
```

Props:
- `title`: Bold card title
- `subTitle`: Italicized text next to the title
- `href`: Link to make the title text a hyperlink

Use it with CardGrid as follows:

```mdx
import { CardGrid } from '@astrojs/starlight/components';
import CustomCard from '@components/CustomCard.astro';

<CardGrid>

<CustomCard title="Visible title" subTitle="Italicized unlinked text next to title" href="hyperlink for the title">

   Any markdown text, images, components or styling.

</CustomCard>

<CustomCard title="Visible title" subTitle="Italicized unlinked text next to title" href="hyperlink for the title">

   Any markdown text, images, components or styling.

</CustomCard>

</CardGrid>
```

## Images

### Figure Directive

Use the `:::figure` block directive for creating semantic figures with captions:

```markdown
:::figure
![Alt text](/path/to/image.webp)
Caption text here
:::
```

**With custom width** (percentage of container):

```markdown
:::figure{w=70}
![Alt text](/path/to/image.webp)
Caption text here (at 70% width)
:::
```

**With border:**

```markdown
:::figure{border}
![Alt text](/path/to/image.webp)
Caption with default border
:::
```

**With both width and border:**

```markdown
:::figure{w=60 border}
![Alt text](/path/to/image.webp)
Caption at 60% width with border
:::
```

Attributes:
- `w`: Width as a percentage (e.g., `w=70` for 70% width). Omit for 100% width.
- `border`: Adds a gray border around the image

The figure directive:
- Centers the image and caption automatically
- Renders as semantic `<figure>` and `<figcaption>` HTML elements
- Supports markdown formatting in captions (links, bold, etc.)
- Images are automatically optimized when using relative paths

### Basic Markdown Images

Standard markdown images work and are automatically centered:

```markdown
![Alt text](/path/to/image.webp)
```

### Image Attributes

Add width, alignment, and border to images using URL hash syntax:

```markdown
![Alt text](/path/to/image.webp#w=80)
```

**Width** (percentage of container):

```markdown
![Alt text](/path/to/image.webp#w=60)
```

**Alignment** (left, center, right - default is center):

```markdown
![Alt text](/path/to/image.webp#align=left)
![Alt text](/path/to/image.webp#align=right)
```

**Border** (adds a gray border):

```markdown
![Alt text](/path/to/image.webp#border)
```

**Combine multiple attributes** with `&`:

```markdown
![Alt text](/path/to/image.webp#w=50&border&align=right)
```

Attributes:
- `w`: Width as a percentage (e.g., `w=60` for 60% width)
- `align`: Alignment (`left`, `center`, `right`). Default is `center`.
- `border`: Adds default border (5px solid gray), or specify custom: `border=2px_solid_red` (use underscores for spaces)

### Image Location

Place images in `src/assets/content/` for automatic optimization. The path should match the folder structure without the `src/assets/content` prefix:

- File location: `src/assets/content/learning-course/stage1/image.webp`
- Component path: `src="/learning-course/stage1/image.webp"`

For multiple images, use the Slides component instead.

## Features

- **Custom Header**: Green branded header with navigation tabs, search, and theme toggle
- **Theme Switching**: Light/dark mode with persistent preference
- **Image Optimization**: Automatic image optimization for images in `src/assets/content/`
- **Image Lightbox**: Click any image to view full-screen with keyboard navigation and captions
- **Glossary System**: Automatic tooltip definitions for technical terms
- **Responsive Design**: Mobile-friendly with unified hamburger menu navigation

## Getting Started

### Prerequisites

- **Node.js** (version 18 or higher): Download from [nodejs.org](https://nodejs.org/)
- **Git**: Download from [git-scm.com](https://git-scm.com/)
- A code editor (e.g., [VS Code](https://code.visualstudio.com/))

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/frcsoftware/frcsoftware.github.io.git
   cd frcsoftware.github.io
   ```

2. **Install dependencies**
   ```bash
   pnpm install
   ```

3. **Start the development server**
   ```bash
   pnpm dev
   ```

4. **Open in browser**

   Visit [http://localhost:4321](http://localhost:4321) to see the site running locally.

5. **Make changes**

   Edit files in `src/content/docs/` to modify content. The browser will automatically reload when you save changes.

### Verify Installation

To check your Node.js and pnpm versions:
```bash
node --version   # Should be 18.x or higher
pnpm --version   # Should be 10.x or higher
```

## Commands

| Command           | Action                                       |
| :---------------- | :------------------------------------------- |
| `pnpm install`    | Install dependencies                         |
| `pnpm dev`        | Start local dev server at `localhost:4321`   |
| `pnpm build`      | Build production site to `./dist/`           |
| `pnpm preview`    | Preview build locally before deploying       |

## Deployment

This site is configured for deployment on GitHub Pages using the workflow in `.github/workflows/deploy.yml`.

To deploy:
1. Enable GitHub Pages in repository settings and set the source to **GitHub Actions**.
2. Push to `main` (or run the workflow manually from the Actions tab).

## Contributing

See the [Contribution Guide](/contribution/methodsofcontributing/) on the website for details on how to contribute to FRCSoftware.org.

## Links

- [FRCSoftware.org](https://frcsoftware.org/)
- [GitHub Repository](https://github.com/frcsoftware/frcsoftware.github.io)
