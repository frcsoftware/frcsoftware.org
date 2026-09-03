{
  description = "A flake to provide a dev environment for FRC/FTC Software development";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixpkgs-unstable";
  };

  outputs = inputs: {
    devShells = builtins.mapAttrs (
      system: pkgs:
      let
        pnpm_11_24 = pkgs.pnpm_11.overrideAttrs (old: rec {
          version = "11.24.0";
          src = pkgs.fetchurl {
            url = "https://registry.npmjs.org/pnpm/-/pnpm-${version}.tgz";
            hash = "sha256-0eqyQzFyZhzDahjshfzpP3cdsZYnFzKcwB7JwoJMok8=";
          };
        });
      in
      {
        default = pkgs.mkShell {
          packages = [
            pkgs.nodejs_24
            pnpm_11_24
          ];
        };
      }
    ) inputs.nixpkgs.legacyPackages;

    formatter = builtins.mapAttrs (system: pkgs: pkgs.nixfmt-tree) inputs.nixpkgs.legacyPackages;
  };
}
