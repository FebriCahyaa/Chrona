# Google Font Assets

The current upstream font assets are intentionally refreshed by `scripts/assets/fetch_google_assets.py`. This source package was prepared in an environment without outbound network access, so unverified third-party binaries are not bundled here.

The fetch script targets the current `main` branches of Google Fonts `googlesans-flex` and Google's `material-design-icons` repository and records the resolved upstream paths in `.generated/assets/fonts/metadata.json`.
