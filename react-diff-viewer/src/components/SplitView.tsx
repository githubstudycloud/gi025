import type { SplitLine } from '../utils/diff';
import './SplitView.css';

interface SplitViewProps {
  lines: SplitLine[];
}

export function SplitView({ lines }: SplitViewProps) {
  if (lines.length === 0) {
    return <div className="diff-empty" data-testid="diff-empty">输入文本以查看差异</div>;
  }

  return (
    <div className="split-view" data-testid="split-view">
      <table className="diff-table split">
        <thead>
          <tr>
            <th className="line-num-header">行号</th>
            <th className="split-content-header">原始文本</th>
            <th className="line-num-header">行号</th>
            <th className="split-content-header">新文本</th>
          </tr>
        </thead>
        <tbody>
          {lines.map((line, index) => (
            <tr key={index}>
              <td className={`line-num split-cell-${line.left.type}`}>
                {line.left.lineNumber ?? ''}
              </td>
              <td className={`split-content split-cell-${line.left.type}`}>
                {line.left.type !== 'empty' && (
                  <>
                    <span className="line-prefix">
                      {line.left.type === 'removed' ? '-' : ' '}
                    </span>
                    <span className="line-text">{line.left.value}</span>
                  </>
                )}
              </td>
              <td className={`line-num split-cell-${line.right.type}`}>
                {line.right.lineNumber ?? ''}
              </td>
              <td className={`split-content split-cell-${line.right.type}`}>
                {line.right.type !== 'empty' && (
                  <>
                    <span className="line-prefix">
                      {line.right.type === 'added' ? '+' : ' '}
                    </span>
                    <span className="line-text">{line.right.value}</span>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
