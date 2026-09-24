import type {} from 'mdast';

// mdast-util-to-hast reads these off node.data to control the hast output,
// but doesn't ship a `Data` augmentation that our remark plugins pick up.
declare module 'mdast' {
    interface Data {
        hName?: string;
        hProperties?: Record<string, unknown>;
    }
}
