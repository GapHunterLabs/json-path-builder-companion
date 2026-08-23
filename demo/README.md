# Demo data for screenshots

Realistic sample order data ("acmecorp" — not a real company, just a
stand-in) in `sample-orders.json`.

## How to get the screenshot

1. Launch the plugin sandbox from the `json-path-builder-companion`
   folder: `./gradlew runIde`
2. In the sandbox IDE, open this `demo/` folder as the project.
3. Enter Full Screen (`View > Appearance > Enter Full Screen`, or search
   "Enter Full Screen" via Find Action).
4. Open the **JSON Path Builder** tool window (bottom of the IDE).
5. Paste the contents of `sample-orders.json` into the sample text area,
   and type `$.orders[*].items[*].sku` into the JSONPath field. You
   should see 3 highlighted matches and "3 match(es)" in the status bar.
6. Take the screenshot (`Win+Shift+S` or your usual tool) with the
   expression, sample JSON, and highlighted matches all visible, and
   save it directly into
   `json-path-builder-companion/docs/screenshots/`.
7. Close the sandbox window when done.
