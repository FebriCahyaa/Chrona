# Chrona App Links & Deep Links

Chrona accepts:

- `chrona://alarm/<alarmId>` for the local deep-link scheme.
- `https://<configured-host>/alarm/<alarmId>` for Android App Links.

The HTTPS host is configurable through the Gradle property
`-PchronaAppLinkHost=your.real.domain`; no fake production domain is embedded.

For verified HTTPS links, publish the release certificate association from
`docs/app-links/assetlinks.json.example` at
`https://<configured-host>/.well-known/assetlinks.json` after replacing the placeholder
SHA-256 certificate fingerprint.

The deep-link parser accepts only an alarm ID and opens the Alarm screen with that ID.
Malformed or unrelated URIs are ignored.
