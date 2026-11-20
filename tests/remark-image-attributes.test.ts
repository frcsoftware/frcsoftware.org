import assert from 'node:assert/strict';
import test from 'node:test';
import type { Image, Paragraph, Root } from 'mdast';
import { remarkImageAttributes } from '../src/plugins/remark-image-attributes';

function image(url: string): Image {
    return { type: 'image', url, alt: 'Example' };
}

function transform(...children: Image[]): Paragraph {
    const paragraph: Paragraph = { type: 'paragraph', children };
    const tree: Root = { type: 'root', children: [paragraph] };
    remarkImageAttributes()(tree);
    return paragraph;
}

test('multiple images keep independent width, border and alignment', () => {
    const first = image('./plain.webp');
    const second = image('./styled.webp#w=60&border&align=right');
    const paragraph = transform(first, second);

    assert.equal(
        paragraph.data?.hProperties?.class,
        'img-wrapper img-multiple',
    );
    assert.equal(paragraph.data?.hName, 'div');
    assert.equal(
        first.data?.hProperties?.style,
        'display: block; margin-left: 0; margin-right: auto;',
    );
    assert.equal(first.data?.hProperties?.['data-slide-width'], undefined);
    assert.equal(first.url, './plain.webp');
    assert.equal(second.url, './styled.webp');
    assert.match(String(second.data?.hProperties?.style), /width: 60%/);
    assert.match(
        String(second.data?.hProperties?.style),
        /border: 5px solid #ADADAD/,
    );
    assert.match(
        String(second.data?.hProperties?.style),
        /margin-left: auto; margin-right: 0/,
    );
    assert.equal(second.data?.hProperties?.['data-slide-width'], '60');
    assert.equal(second.data?.hProperties?.['data-slide-align'], 'right');
});

test('different alignments in one paragraph do not overwrite each other', () => {
    const first = image('./one.webp#align=left');
    const second = image('./two.webp#align=center');
    transform(first, second);

    assert.match(
        String(first.data?.hProperties?.style),
        /margin-left: 0; margin-right: auto/,
    );
    assert.match(
        String(second.data?.hProperties?.style),
        /margin-left: auto; margin-right: auto/,
    );
});

test('single-image paragraphs preserve existing wrapper behavior', () => {
    const img = image('./single.webp#w=50&align=right');
    const paragraph = transform(img);

    assert.equal(paragraph.data?.hName, 'div');
    assert.equal(
        paragraph.data?.hProperties?.class,
        'img-wrapper img-align-right',
    );
    assert.equal(img.data?.hProperties?.style, 'width: 50%;');
    assert.equal(img.url, './single.webp');
});

test('unattributed single image remains left aligned', () => {
    const img = image('./plain.webp');
    const paragraph = transform(img);

    assert.equal(
        paragraph.data?.hProperties?.class,
        'img-wrapper img-align-left',
    );
    assert.equal(img.data, undefined);
});
