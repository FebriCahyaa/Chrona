#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

if ! command -v apt-get >/dev/null; then
  echo 'This installer expects a Debian/Ubuntu runner.' >&2
  exit 2
fi

sudo install -d -m 0755 /etc/apt/keyrings
curl -fsSL https://dl.google.com/linux/linux_signing_key.pub | sudo tee /etc/apt/keyrings/google.asc >/dev/null
echo 'deb [arch=amd64 signed-by=/etc/apt/keyrings/google.asc] https://dl.google.com/android/cli/latest/debian/ stable main' | sudo tee /etc/apt/sources.list.d/android-cli.list >/dev/null
sudo apt-get update
sudo apt-get install -y android-cli
android --version
