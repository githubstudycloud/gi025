import { startTransition, useDeferredValue, useMemo, useState } from 'react';
import { buildDiffReport } from './diff';

const SAMPLE_LEFT = `# Weekly Release Notes
- Improve inventory sync
- Fix customer lookup timeout
- Add audit dashboard
Next review: Friday`;

const SAMPLE_RIGHT = `# Weekly Release Notes
- Improve inventory sync
- Fix customer search timeout
- Add audit dashboard
- Ship side-by-side diff UI
Next review: Monday`;

const CARD_CONFIG = [
  { key: 'unchanged', label: 'Unchanged', hint: 'matching rows', tone: 'equal' },
  { key: 'changed', label: 'Changed', hint: 'paired edits', tone: 'changed' },
  { key: 'added', label: 'Added', hint: 'new rows', tone: 'added' },
  { key: 'removed', label: 'Removed', hint: 'missing rows', tone: 'removed' },
  { key: 'total', label: 'Total Rows', hint: 'diff rows', tone: 'neutral' },
  { key: 'similarity', label: 'Similarity', hint: 'line match %', tone: 'neutral', suffix: '%' }
];

const STATUS_LABELS = {
  equal: 'Unchanged',
  changed: 'Changed',
  added: 'Added',
  removed: 'Removed'
};

function StatCard({ label, hint, tone, value, suffix, testId }) {
  return (
    <article className={`stat-card stat-card--${tone}`}>
      <span className="stat-card__label">{label}</span>
      <output className="stat-card__value" data-testid={testId}>
        {value}
        {suffix ?? ''}
      </output>
      <span className="stat-card__hint">{hint}</span>
    </article>
  );
}

function renderCell(cell) {
  if (!cell) {
    return (
      <div className="diff-line diff-line--empty" aria-hidden="true">
        <span className="diff-line__placeholder">no content</span>
      </div>
    );
  }

  return (
    <>
      <span className="diff-line__number">{cell.number}</span>
      <pre className="diff-line__text">{cell.text}</pre>
    </>
  );
}

export default function App() {
  const [leftText, setLeftText] = useState(SAMPLE_LEFT);
  const [rightText, setRightText] = useState(SAMPLE_RIGHT);

  const deferredLeftText = useDeferredValue(leftText);
  const deferredRightText = useDeferredValue(rightText);
  const report = useMemo(
    () => buildDiffReport(deferredLeftText, deferredRightText),
    [deferredLeftText, deferredRightText]
  );

  const isComputing = leftText !== deferredLeftText || rightText !== deferredRightText;

  const handleLoadSample = () => {
    startTransition(() => {
      setLeftText(SAMPLE_LEFT);
      setRightText(SAMPLE_RIGHT);
    });
  };

  const handleClear = () => {
    startTransition(() => {
      setLeftText('');
      setRightText('');
    });
  };

  const handleSwap = () => {
    startTransition(() => {
      setLeftText(rightText);
      setRightText(leftText);
    });
  };

  return (
    <main className="app-shell">
      <section className="hero">
        <div className="hero__content panel">
          <p className="eyebrow">React Diff Studio</p>
          <h1>Readable diff views for line-by-line text changes.</h1>
          <p className="hero__lead">
            Compare two text blocks, spot changed lines instantly, and keep the summary visible
            while editing.
          </p>
          <div className="hero__chips" aria-label="feature summary">
            <span className="hero__chip">LCS matcher</span>
            <span className="hero__chip">Responsive layout</span>
            <span className="hero__chip">Vitest covered</span>
          </div>
        </div>

        <aside className="hero__aside panel">
          <p className="hero__aside-title">Session State</p>
          <p className="hero__aside-status">{isComputing ? 'Analyzing changes...' : 'Diff ready'}</p>
          <dl className="hero__metrics">
            <div>
              <dt>Source lines</dt>
              <dd>{report.leftLineCount}</dd>
            </div>
            <div>
              <dt>Target lines</dt>
              <dd>{report.rightLineCount}</dd>
            </div>
            <div>
              <dt>Visible rows</dt>
              <dd>{report.summary.total}</dd>
            </div>
          </dl>
          <p className="hero__aside-copy">
            The viewer aligns added, removed, and paired changed blocks so the two columns stay easy
            to scan.
          </p>
        </aside>
      </section>

      <section className="panel workspace">
        <div className="workspace__header">
          <div>
            <p className="workspace__eyebrow">Input</p>
            <h2>Draft both versions</h2>
          </div>
          <div className="workspace__actions">
            <button type="button" onClick={handleLoadSample}>
              Load Sample
            </button>
            <button type="button" onClick={handleSwap}>
              Swap
            </button>
            <button type="button" className="button--ghost" onClick={handleClear}>
              Clear
            </button>
          </div>
        </div>

        <div className="editor-grid">
          <label className="editor-card" htmlFor="left-input">
            <span className="editor-card__label">Source Text</span>
            <textarea
              id="left-input"
              aria-label="Source Text"
              value={leftText}
              onChange={(event) => setLeftText(event.target.value)}
              placeholder="Paste the original version here"
            />
          </label>

          <label className="editor-card" htmlFor="right-input">
            <span className="editor-card__label">Target Text</span>
            <textarea
              id="right-input"
              aria-label="Target Text"
              value={rightText}
              onChange={(event) => setRightText(event.target.value)}
              placeholder="Paste the updated version here"
            />
          </label>
        </div>
      </section>

      <section className="stats-grid" aria-label="diff summary">
        {CARD_CONFIG.map((card) => (
          <StatCard
            key={card.key}
            label={card.label}
            hint={card.hint}
            tone={card.tone}
            suffix={card.suffix}
            value={report.summary[card.key]}
            testId={`stat-${card.key}`}
          />
        ))}
      </section>

      <section className="panel diff-panel">
        <div className="diff-panel__header">
          <div>
            <p className="workspace__eyebrow">Output</p>
            <h2>Side-by-side diff</h2>
          </div>
          <div className="legend" aria-label="diff legend">
            <span className="legend__item legend__item--equal">Unchanged</span>
            <span className="legend__item legend__item--changed">Changed</span>
            <span className="legend__item legend__item--added">Added</span>
            <span className="legend__item legend__item--removed">Removed</span>
          </div>
        </div>

        {report.rows.length === 0 ? (
          <div className="empty-state">
            <h3>No diff to display</h3>
            <p>Enter content in either panel and the comparison will appear here.</p>
          </div>
        ) : (
          <div className="diff-list">
            {report.rows.map((row) => (
              <article className={`diff-row diff-row--${row.status}`} key={row.id}>
                <div className="diff-row__status">{STATUS_LABELS[row.status]}</div>
                <div className="diff-row__pair">
                  <div className="diff-cell diff-cell--left">{renderCell(row.left)}</div>
                  <div className="diff-cell diff-cell--right">{renderCell(row.right)}</div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
