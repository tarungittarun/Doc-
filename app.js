/* Doc App — client-side document editor.
   Documents are stored in the browser's localStorage. No backend required. */

const STORAGE_KEY = 'docapp_docs';

const els = {
  newDocBtn: document.getElementById('newDocBtn'),
  exportBtn: document.getElementById('exportBtn'),
  exportTxtBtn: document.getElementById('exportTxtBtn'),
  searchInput: document.getElementById('searchInput'),
  docList: document.getElementById('docList'),
  emptyState: document.getElementById('emptyState'),
  editorShell: document.querySelector('.editor-shell'),
  editorEmpty: document.querySelector('.editor-empty'),
  titleInput: document.getElementById('titleInput'),
  contentInput: document.getElementById('contentInput'),
  deleteBtn: document.getElementById('deleteBtn'),
  previewBtn: document.getElementById('previewBtn'),
  preview: document.getElementById('preview'),
  status: document.getElementById('status'),
};

let docs = [];
let activeId = null;
let isPreview = false;
let autosaveTimer = null;

// ---------- storage helpers ----------

function loadDocs() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    docs = raw ? JSON.parse(raw) : [];
  } catch (e) {
    docs = [];
  }
  if (!Array.isArray(docs)) docs = [];
  docs.sort((a, b) => b.updatedAt - a.updatedAt);
}

function saveDocs() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(docs));
}

function now() { return Date.now(); }

// ---------- document model ----------

function createDoc(title = 'Untitled document', content = '') {
  const id = crypto.randomUUID ? crypto.randomUUID() : `${now()}-${Math.random().toString(36).slice(2)}`;
  const doc = { id, title, content, createdAt: now(), updatedAt: now() };
  docs.unshift(doc);
  saveDocs();
  return doc;
}

function updateDoc(id, patch) {
  const idx = docs.findIndex(d => d.id === id);
  if (idx === -1) return false;
  docs[idx] = { ...docs[idx], ...patch, updatedAt: now() };
  docs.sort((a, b) => b.updatedAt - a.updatedAt);
  saveDocs();
  return true;
}

function deleteDoc(id) {
  docs = docs.filter(d => d.id !== id);
  saveDocs();
  if (activeId === id) setActive(null);
}

// ---------- rendering ----------

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

function formatDate(ts) {
  const d = new Date(ts);
  const now = Date.now();
  const diff = now - ts;
  if (diff < 60000) return 'Just now';
  if (diff < 86400000) return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  return d.toLocaleDateString();
}

function renderList() {
  const query = els.searchInput.value.toLowerCase();
  const filtered = docs.filter(d =>
    d.title.toLowerCase().includes(query) || d.content.toLowerCase().includes(query)
  );

  els.docList.innerHTML = '';
  if (filtered.length === 0) {
    els.emptyState.hidden = false;
  } else {
    els.emptyState.hidden = true;
    filtered.forEach(doc => {
      const li = document.createElement('li');
      li.className = `doc-item${doc.id === activeId ? ' active' : ''}`;
      li.innerHTML = `
        <div>
          <h3>${escapeHtml(doc.title || 'Untitled document')}</h3>
          <span class="meta">${formatDate(doc.updatedAt)}</span>
        </div>
      `;
      li.addEventListener('click', () => setActive(doc.id));
      els.docList.appendChild(li);
    });
  }
}

function setActive(id) {
  activeId = id;
  const doc = id ? docs.find(d => d.id === id) : null;

  if (!doc) {
    els.editorShell.hidden = true;
    els.editorEmpty.hidden = false;
    els.preview.hidden = true;
    isPreview = false;
    renderList();
    return;
  }

  els.editorEmpty.hidden = true;
  els.editorShell.hidden = false;
  els.preview.hidden = true;
  isPreview = false;

  els.titleInput.value = doc.title || 'Untitled document';
  els.contentInput.value = doc.content || '';
  updateStatus(doc);
  renderList();
}

function updateStatus(doc) {
  const words = (doc.content || '').trim() ? (doc.content.match(/\S+/g) || []).length : 0;
  els.status.textContent = `${words} words · updated ${formatDate(doc.updatedAt)}`;
}

// ---------- markdown rendering (small subset) ----------

function markdownToHtml(text) {
  let html = escapeHtml(text).replace(/\r\n/g, '\n').replace(/\r/g, '\n');

  // code blocks
  html = html.replace(/```([\s\S]*?)```/g, (_m, code) => `<pre><code>${escapeHtml(code.trim())}</code></pre>`);

  // headings
  html = html.replace(/^#{1,6} (.*)$/gm, m => {
    const level = m.match(/^#+/)[0].length;
    const text = m.slice(level + 1);
    return `<h${level}>${text}</h${level}>`;
  });
  // horizontal rules
  html = html.replace(/^---$/gm, '<hr>');
  // bold / italic
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
  html = html.replace(/_(.+?)_/g, '<em>$1</em>');
  // inline code
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  // blockquotes
  html = html.replace(/^> (.*)$/gm, '<blockquote>$1</blockquote>');
  // links
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');
  // lists
  html = html.replace(/^[-*+] (.+)$/gm, '<li>$1</li>');
  html = html.replace(/(<li>.*<\/li>\n?)+/gs, m => `<ul>${m}</ul>`);
  // paragraphs
  html = html.split(/\n\n+/).map(block => {
    if (block.match(/^<(h[1-6]|ul|ol|blockquote|pre|hr)/)) return block;
    return `<p>${block.replace(/\n/g, '<br>')}</p>`;
  }).join('');

  return html;
}

// ---------- event wiring ----------

els.newDocBtn.addEventListener('click', () => {
  const doc = createDoc();
  setActive(doc.id);
  els.titleInput.focus();
});

els.searchInput.addEventListener('input', renderList);

els.titleInput.addEventListener('input', () => {
  if (!activeId) return;
  updateDoc(activeId, { title: els.titleInput.value });
  renderList();
  updateStatus(docs.find(d => d.id === activeId));
});

els.contentInput.addEventListener('input', () => {
  if (!activeId) return;
  updateDoc(activeId, { content: els.contentInput.value });
  updateStatus(docs.find(d => d.id === activeId));
  if (isPreview) renderPreview();
});

els.deleteBtn.addEventListener('click', () => {
  if (!activeId) return;
  if (!confirm('Delete this document?')) return;
  deleteDoc(activeId);
});

els.previewBtn.addEventListener('click', () => {
  if (!activeId) return;
  isPreview = !isPreview;
  els.previewBtn.textContent = isPreview ? 'Edit' : 'Preview';
  els.preview.hidden = !isPreview;
  if (isPreview) renderPreview();
});

function renderPreview() {
  const doc = docs.find(d => d.id === activeId);
  els.preview.innerHTML = doc ? markdownToHtml(doc.content || '') : '';
}

// Export

function exportDoc(ext) {
  if (!activeId) return;
  const doc = docs.find(d => d.id === activeId);
  const content = ext === 'txt'
    ? `${doc.title}\n${'='.repeat(doc.title.length)}\n\n${doc.content || ''}`
    : `# ${doc.title}\n\n${doc.content || ''}`;
  const blob = new Blob([content], { type: ext === 'txt' ? 'text/plain' : 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${(doc.title || 'untitled').replace(/[^a-z0-9]+/gi, '_')}.${ext}`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

els.exportBtn.addEventListener('click', () => exportDoc('md'));
els.exportTxtBtn.addEventListener('click', () => exportDoc('txt'));

// Autosave is handled on input; also save on visibility change to be safe
document.addEventListener('visibilitychange', () => {
  if (document.hidden) saveDocs();
});

// ---------- init ----------

loadDocs();
if (docs.length) setActive(docs[0].id);
renderList();
