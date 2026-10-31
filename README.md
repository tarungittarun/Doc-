# Doc App

A lightweight, client-side document editor. Create, edit, search, and export documents — everything runs in the browser with no build step and no backend. Documents persist in `localStorage`.

## Features

- **Create / edit documents** with live autosave
- **Document list** with search and last-updated timestamps
- **Markdown support** in the editor and preview (headings, bold/italic, code, links, lists, blockquotes)
- **Export** to Markdown (`.md`) or plain text (`.txt`)
- **Delete** documents
- Fully responsive (sidebar stacks on mobile)
- Zero dependencies — just open `index.html`

## Files

- `index.html` — app shell
- `style.css` — styles
- `app.js` — application logic (state, storage, rendering, markdown preview, export)

## Usage

Open `index.html` in any modern browser, or serve the folder with a static server:

```bash
python3 -m http.server 8000
# then visit http://localhost:8000
```

## License

MIT
