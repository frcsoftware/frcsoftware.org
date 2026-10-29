/// <reference types="astro/client" />

declare module 'virtual:starlight/components/Pagination' {
    const Pagination: typeof import('@astrojs/starlight/components/Pagination.astro').default;
    export default Pagination;
}

declare module 'virtual:starlight/components/MobileMenuFooter' {
    const MobileMenuFooter: typeof import('@astrojs/starlight/components/MobileMenuFooter.astro').default;
    export default MobileMenuFooter;
}

declare module 'virtual:starlight/components/EditLink' {
    const EditLink: typeof import('@astrojs/starlight/components/EditLink.astro').default;
    export default EditLink;
}
