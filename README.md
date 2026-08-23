# JSON Path Builder Companion

A tool window with a JSONPath field and a sample-JSON text area — type
an expression, type or paste sample JSON, and every matched node is
highlighted live as you type, with a real match count.

## Why it exists

Building a JSONPath expression today usually means trial-and-error
against an external website (pasting JSON into a browser tab, copying
the expression back) or guessing blind in code and re-running until it
works. Neither is available offline, and neither shows the match
highlighted directly against your own sample data as you type.

## Why built this way

- **Real JSON PSI, not a hand-rolled JSON parser.** The sample JSON is
  parsed via `PsiFileFactory.createFileFromText` into a real, in-memory
  `JsonFile` (the same bundled JSON plugin every `.json` file in the IDE
  already uses) — same "don't reinvent a parser for a format the
  platform already parses correctly" principle already proven by
  `json-schema-companion`/`asyncapi-companion`/`openapi-companion`'s
  `JsonPointer` (RFC 6901), which this evaluator's tree-navigation style
  mirrors directly.
- **Live recompute on every keystroke, no debouncing** — same design as
  `regex-preview-companion`: parsing a typically-short sample and
  walking a JSONPath expression against it is the same cost class as a
  regex match, not something that needs throttling.
- **An invalid sample shows an honest error, not silent "0 matches".**
  The JSON PSI parser is error-tolerant (it still produces a partial
  tree for malformed input), so this checks for real `PsiErrorElement`s
  in the parsed tree before evaluating anything — same check already
  proven in this catalog's `InMemoryValidator` (`bean-copy-companion`,
  `test-scaffold-companion`, and others).

## v0.1 scope — the common JSONPath subset, stated honestly

Supported: `$` root, `.key`, `['key']`/`["key"]`, `[n]` array index,
`.*`/`[*]` wildcard (every direct child), `..key` recursive descent
(every value named `key` at any depth).

**Not supported in v0.1** (real, documented limitations, not bugs):
filter expressions (`[?(...)]`), array slices (`[start:end:step]`),
union syntax (`['a','b']`), script expressions, and recursive wildcard
(`..*`).

## Usage

Open the **JSON Path Builder** tool window (bottom of the IDE). Type a
JSONPath expression in the top field, and paste or type sample JSON
below — matches highlight live.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
