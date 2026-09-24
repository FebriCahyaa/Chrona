# Google Font Assets

`GoogleSansFlex.ttf` is a symlink refreshed by `scripts/assets/fetch_google_assets.py`,
which targets the current default branch of Google Fonts `googlesans-flex`
and records the resolved upstream path in `.generated/assets/fonts/metadata.json`.
This source package was prepared in an environment without outbound network
access, so the unverified upstream binary isn't bundled here; run the script
on a network-enabled machine or let CI run it before building.

Material Symbols icons are fetched separately, per icon, as Android vector
drawables into `res/drawable/` — see `scripts/assets/fetch_material_symbols_icons.py`
and `scripts/assets/material_symbols_icons.txt`.
