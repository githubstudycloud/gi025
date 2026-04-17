import { useDiff } from './hooks/useDiff';
import { UnifiedView } from './components/UnifiedView';
import { SplitView } from './components/SplitView';
import { DiffStatsBar } from './components/DiffStats';
import './App.css';

const SAMPLE_OLD = `function greet(name) {
  console.log("Hello, " + name);
  return true;
}

const users = ["Alice", "Bob"];
for (let i = 0; i < users.length; i++) {
  greet(users[i]);
}`;

const SAMPLE_NEW = `function greet(name: string): boolean {
  console.log(\`Hello, \${name}!\`);
  return true;
}

const users: string[] = ["Alice", "Bob", "Charlie"];
for (const user of users) {
  greet(user);
}`;

function App() {
  const {
    oldText,
    newText,
    setOldText,
    setNewText,
    viewMode,
    setViewMode,
    lineDiff,
    splitDiff,
    stats,
    handleFileUpload,
  } = useDiff();

  const loadSample = () => {
    setOldText(SAMPLE_OLD);
    setNewText(SAMPLE_NEW);
  };

  const clearAll = () => {
    setOldText('');
    setNewText('');
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>React Diff Viewer</h1>
        <p className="subtitle">文本差异对比工具</p>
      </header>

      <div className="toolbar">
        <div className="view-toggle">
          <button
            className={viewMode === 'unified' ? 'active' : ''}
            onClick={() => setViewMode('unified')}
            data-testid="btn-unified"
          >
            Unified
          </button>
          <button
            className={viewMode === 'split' ? 'active' : ''}
            onClick={() => setViewMode('split')}
            data-testid="btn-split"
          >
            Split
          </button>
        </div>
        <div className="actions">
          <button onClick={loadSample} data-testid="btn-sample">
            加载示例
          </button>
          <button onClick={clearAll} data-testid="btn-clear">
            清空
          </button>
        </div>
      </div>

      <div className="input-area">
        <div className="input-panel">
          <div className="panel-header">
            <label>原始文本</label>
            <input
              type="file"
              accept=".txt,.md,.json,.js,.ts,.java,.py,.xml,.yml,.yaml,.css,.html"
              onChange={handleFileUpload('old')}
              data-testid="file-old"
            />
          </div>
          <textarea
            value={oldText}
            onChange={(e) => setOldText(e.target.value)}
            placeholder="输入或粘贴原始文本..."
            data-testid="input-old"
          />
        </div>
        <div className="input-panel">
          <div className="panel-header">
            <label>新文本</label>
            <input
              type="file"
              accept=".txt,.md,.json,.js,.ts,.java,.py,.xml,.yml,.yaml,.css,.html"
              onChange={handleFileUpload('new')}
              data-testid="file-new"
            />
          </div>
          <textarea
            value={newText}
            onChange={(e) => setNewText(e.target.value)}
            placeholder="输入或粘贴新文本..."
            data-testid="input-new"
          />
        </div>
      </div>

      {(oldText || newText) && (
        <>
          <DiffStatsBar stats={stats} />
          <div className="diff-output" data-testid="diff-output">
            {viewMode === 'unified' ? (
              <UnifiedView lines={lineDiff} />
            ) : (
              <SplitView lines={splitDiff} />
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default App;
