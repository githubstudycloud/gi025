import type { DiffStats } from '../utils/diff';
import './DiffStats.css';

interface DiffStatsBarProps {
  stats: DiffStats;
}

export function DiffStatsBar({ stats }: DiffStatsBarProps) {
  const total = stats.additions + stats.deletions + stats.unchanged;

  return (
    <div className="diff-stats-bar" data-testid="diff-stats">
      <span className="stat stat-added" data-testid="stat-added">
        +{stats.additions}
      </span>
      <span className="stat stat-removed" data-testid="stat-removed">
        -{stats.deletions}
      </span>
      <span className="stat stat-unchanged">
        {stats.unchanged} unchanged
      </span>
      {total > 0 && (
        <div className="stats-progress">
          <div
            className="progress-added"
            style={{ width: `${(stats.additions / total) * 100}%` }}
          />
          <div
            className="progress-removed"
            style={{ width: `${(stats.deletions / total) * 100}%` }}
          />
          <div
            className="progress-unchanged"
            style={{ width: `${(stats.unchanged / total) * 100}%` }}
          />
        </div>
      )}
    </div>
  );
}
