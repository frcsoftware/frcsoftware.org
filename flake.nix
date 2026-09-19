{
  description = "A flake to provide a dev environment for FRC/FTC Software development";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixpkgs-unstable";
  };

  outputs = inputs: {
    devShells = builtins.mapAttrs (
      system: pkgs:
      {
        default = pkgs.mkShell {
          packages = [
            pkgs.nodejs_24
            pkgs.pnpm_12
          ];
        };
      }
    ) inputs.nixpkgs.legacyPackages;

    formatter = builtins.mapAttrs (system: pkgs: pkgs.nixfmt-tree) inputs.nixpkgs.legacyPackages;
  };
}
