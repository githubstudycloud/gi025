import type { DiffLine } from '../utils/diff';
import './UnifiedView.css';

interface UnifiedViewProps {
  lines: DiffLine[];
}

export function UnifiedView({ lines }: UnifiedViewProps) {
  if (lines.length === 0) {
    return <div className="diff-empty" data-testid="diff-empty">输入文本以查看差异</div>;
  }

  return (
    <table className="diff-table unified" data-testid="unified-view">
      <thead>
        <tr>
          <th className="line-num-header">旧</th>
          <th className="line-num-header">新</th>
          <th className="content-header">内容</th>
        </tr>
      </thead>
      <tbody>
        {lines.map((line, index) => (
          <tr
            key={index}
            className={`diff-line diff-line-${line.type}`}
            data-testid={`diff-line-${line.type}`}
          >
            <td className="line-num">{line.lineNumberOld ?? ''}</td>
            <td className="line-num">{line.lineNumberNew ?? ''}</td>
            <td className="line-content">
              <span className="line-prefix">
                {line.type === 'added' ? '+' : line.type === 'removed' ? '-' : ' '}
              </span>
              <span className="line-text">{line.value}</span>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
