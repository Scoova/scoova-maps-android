# Changelog

All notable changes to `info.scoo-va:maps` are documented in this file. This
project follows [Semantic Versioning](https://semver.org/).

## 1.1.0 — 2026-05-25

### Added
- `staticMapUrl(opts)` — pure URL builder for the static-map endpoint, ready
  to drop into an `ImageView` loader or any HTML `<img>`.
- `staticMapBytes(opts)` — blocking PNG fetcher (`HttpURLConnection`),
  forwards `Accept-Language` when a locale is provided.
- `styleUrl(styleName, StyleUrlOptions)` — MapLibre-compatible style URL
  builder that does not require an existing MapLibre `Style.Builder`.
- `ScoovaMapDefaults.styleUrlForLocale(locale)` — helper that returns the
  canonical Scoova style URL with `?locale=…` appended.
- `StaticMapMarker`, `StaticMapPath`, `StaticMapOptions`, `StyleUrlOptions`,
  `SCOOVA_DEFAULT_API_BASE`, `SCOOVA_DEFAULT_TILES_BASE`.
- LICENSE (Apache-2.0), CHANGELOG, `.gitignore`.
- Monitor-style Maven publishing configuration: `library` publication with
  full POM + sources/javadoc JARs, GitHub Packages + Maven Central
  repositories, GPG signing wired in.

### Changed
- License is now Apache-2.0.
- Artifact coordinates: `info.scoo-va:maps:1.1.0`.

## 1.0.0 — 2026-05-05

- Initial release: defaults, inline style builder (`buildInlineStyle`),
  `routeFeature`, `markerFeature`, `bboxOf`.
