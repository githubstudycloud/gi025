import { describe, expect, it } from 'vitest';
import { buildDiffReport, toLines } from './diff';

describe('toLines', () => {
  it('returns an empty array for empty input', () => {
    expect(toLines('')).toEqual([]);
  });

  it('normalizes windows line endings', () => {
    expect(toLines('a\r\nb\r\nc')).toEqual(['a', 'b', 'c']);
  });
});

describe('buildDiffReport', () => {
  it('keeps matching lines as unchanged rows', () => {
    const report = buildDiffReport('alpha\nbeta', 'alpha\nbeta');

    expect(report.summary).toMatchObject({
      unchanged: 2,
      changed: 0,
      added: 0,
      removed: 0,
      total: 2,
      similarity: 100
    });
    expect(report.rows.map((row) => row.status)).toEqual(['equal', 'equal']);
  });

  it('pairs adjacent removed and added lines as changed rows', () => {
    const report = buildDiffReport('alpha\nbeta\ngamma', 'alpha\nbeta revised\ngamma');

    expect(report.summary).toMatchObject({
      unchanged: 2,
      changed: 1,
      added: 0,
      removed: 0
    });
    expect(report.rows.map((row) => row.status)).toEqual(['equal', 'changed', 'equal']);
    expect(report.rows[1].left?.text).toBe('beta');
    expect(report.rows[1].right?.text).toBe('beta revised');
  });

  it('tracks additions and removals that cannot be paired', () => {
    const report = buildDiffReport('alpha\ngamma', 'alpha\nbeta\ngamma\ndelta');

    expect(report.summary).toMatchObject({
      unchanged: 2,
      changed: 0,
      added: 2,
      removed: 0,
      total: 4,
      similarity: 50
    });
    expect(report.rows.map((row) => row.status)).toEqual(['equal', 'added', 'equal', 'added']);
  });
});
