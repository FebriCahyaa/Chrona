# Phase 6A — Static Release Gate

Status: COMPLETE

Phase 6A establishes the final static quality gate before runtime/device verification.

Scope:
- Android 17 toolchain consistency.
- Version metadata and release-tag consistency.
- Environment-backed release signing.
- Release workflow gate presence.
- Manifest exported-component boundary.
- Documentation link integrity.
- Private-key, credential, and binary hygiene.

The gate is implemented in `scripts/phase6-release-gate.sh` and accepts an optional release tag.
When a tag is supplied, the script requires its semantic version to match `versionName`.

Runtime behavior, actual APK signature validity, UI rendering, performance, and device compatibility
remain Phase 6B/6C concerns and are not claimed by this static gate.
