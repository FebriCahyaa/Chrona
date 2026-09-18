# Phase 6C — Release Candidate & Final Release Gate

Status: COMPLETE

Phase 6C is the final planned Chrona phase. It turns the Phase 6A static checks and Phase 6B
runtime automation into a single release-candidate gate.

## Scope

- Verify release source identity and Android 17 configuration.
- Verify manifest export boundary.
- Verify the release workflow places the RC gate before repository mutation/publication.
- Verify source-tree release-artifact hygiene.
- When an APK is supplied, verify checksum and, when SDK tools exist, APK signature and identity.
- Emit a machine-readable release candidate provenance manifest.

## Exit criteria

- Phase 6A static gate remains valid.
- Phase 6B runtime/device workflow remains present and authoritative for physical execution.
- Phase 6C RC gate passes in source-only mode.
- CI release workflow invokes Phase 6C before `git push` and GitHub Release publication.
- Release candidate metadata is reproducible from the source commit and artifact checksum.

## Publication boundary

Chrona does not publish a public release automatically during development. Publication remains an
explicit maintainer action after the release environment secrets, signed APK, and Runtime QA results
have been reviewed.
