FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Pre-install the WPILib VS Code extension into the vscode user's extension directory
# at image-build time. This guarantees the files exist before VS Code Server starts
# and scans ~/.vscode-server/extensions/, regardless of when postCreateCommand runs.
# The marketplace version is blocked ("use WPILib Installer"), so we use the VSIX directly.
RUN EXT_DIR=/home/vscode/.vscode-server/extensions/wpilibsuite.vscode-wpilib-2026.2.1 \
 && mkdir -p "$EXT_DIR" \
 && wget -q 'https://github.com/wpilibsuite/vscode-wpilib/releases/download/v2026.2.1/vscode-wpilib-2026.2.1.vsix' -O /tmp/vscode-wpilib.vsix \
 && unzip -q /tmp/vscode-wpilib.vsix 'extension/*' -d /tmp/vscode-wpilib-unpack \
 && cp -r /tmp/vscode-wpilib-unpack/extension/. "$EXT_DIR/" \
 && chown -R vscode:vscode /home/vscode/.vscode-server \
 && rm -rf /tmp/vscode-wpilib-unpack /tmp/vscode-wpilib.vsix
