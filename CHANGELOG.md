<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# JSON Path Builder Companion Changelog

## [Unreleased]

## [0.1.2]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.1.1]

### Added

- Review/star CTA: after 5 debounced sessions of use (a real pause
  after typing a JSONPath that actually matched something -- never a
  raw keystroke), a one-time notification asks whether to rate the
  plugin on Marketplace, with a permanent "Don't ask again" option.

## [0.1.0]

### Added

- "JSON Path Builder" tool window: a JSONPath field and a sample-JSON
  text area, with every matched node highlighted live as you type and
  a real match count.
- Covers the common JSONPath subset: `$` root, `.key`, `['key']`,
  `[n]`, `.*`/`[*]` wildcard, `..key` recursive descent.
- An invalid expression shows the real parse error and position; an
  invalid sample shows an honest "not valid JSON" instead of silently
  matching nothing.
- 100% static PSI analysis, no external JSONPath library, no network
  calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/json-path-builder-companion/compare/0.1.2...HEAD
[0.1.2]: https://github.com/GapHunterLabs/json-path-builder-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/json-path-builder-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/json-path-builder-companion/commits/0.1.0
