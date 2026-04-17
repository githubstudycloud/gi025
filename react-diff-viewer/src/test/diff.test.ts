import { describe, it, expect } from 'vitest';
import {
  computeDiff,
  computeLineDiff,
  computeSplitDiff,
  computeStats,
} from '../utils/diff';

describe('computeDiff', () => {
  it('should return no changes for identical text', () => {
    const result = computeDiff('hello', 'hello');
    expect(result).toHaveLength(1);
    expect(result[0].added).toBeFalsy();
    expect(result[0].removed).toBeFalsy();
  });

  it('should detect line additions', () => {
    const result = computeDiff('line1\n', 'line1\nline2\n', 'lines');
    const added = result.filter((c) => c.added);
    expect(added.length).toBeGreaterThan(0);
  });

  it('should detect line removals', () => {
    const result = computeDiff('line1\nline2\n', 'line1\n', 'lines');
    const removed = result.filter((c) => c.removed);
    expect(removed.length).toBeGreaterThan(0);
  });

  it('should support word-level diff', () => {
    const result = computeDiff('hello world', 'hello earth', 'words');
    expect(result.length).toBeGreaterThan(1);
  });

  it('should support char-level diff', () => {
    const result = computeDiff('abc', 'axc', 'chars');
    expect(result.length).toBeGreaterThan(1);
  });
});

describe('computeLineDiff', () => {
  it('should return empty array for two empty strings', () => {
    const result = computeLineDiff('', '');
    expect(result).toHaveLength(0);
  });

  it('should assign correct line numbers', () => {
    const result = computeLineDiff('a\nb\n', 'a\nc\n');
    const unchanged = result.filter((l) => l.type === 'unchanged');
    const added = result.filter((l) => l.type === 'added');
    const removed = result.filter((l) => l.type === 'removed');

    expect(unchanged.length).toBeGreaterThan(0);
    expect(added.length).toBeGreaterThan(0);
    expect(removed.length).toBeGreaterThan(0);

    // All unchanged lines should have both line numbers
    for (const line of unchanged) {
      expect(line.lineNumberOld).toBeDefined();
      expect(line.lineNumberNew).toBeDefined();
    }

    // Added lines should only have new line number
    for (const line of added) {
      expect(line.lineNumberNew).toBeDefined();
      expect(line.lineNumberOld).toBeUndefined();
    }

    // Removed lines should only have old line number
    for (const line of removed) {
      expect(line.lineNumberOld).toBeDefined();
      expect(line.lineNumberNew).toBeUndefined();
    }
  });

  it('should handle multiline additions', () => {
    const result = computeLineDiff('a\n', 'a\nb\nc\n');
    const added = result.filter((l) => l.type === 'added');
    expect(added.length).toBe(2);
  });
});

describe('computeSplitDiff', () => {
  it('should produce paired lines for unchanged content', () => {
    const result = computeSplitDiff('line1\nline2\n', 'line1\nline2\n');
    expect(result.length).toBe(2);
    for (const row of result) {
      expect(row.left.type).toBe('unchanged');
      expect(row.right.type).toBe('unchanged');
      expect(row.left.value).toBe(row.right.value);
    }
  });

  it('should align modifications side by side', () => {
    const result = computeSplitDiff('old line\n', 'new line\n');
    expect(result.length).toBe(1);
    expect(result[0].left.type).toBe('removed');
    expect(result[0].right.type).toBe('added');
  });

  it('should pad with empty cells for unmatched lines', () => {
    const result = computeSplitDiff('a\nb\nc\n', 'a\n');
    const emptyRight = result.filter((r) => r.right.type === 'empty');
    expect(emptyRight.length).toBeGreaterThan(0);
  });
});

describe('computeStats', () => {
  it('should count additions and deletions correctly', () => {
    const stats = computeStats('a\nb\n', 'a\nc\nd\n');
    expect(stats.deletions).toBe(1);
    expect(stats.additions).toBe(2);
    expect(stats.unchanged).toBe(1);
  });

  it('should return all zeros for identical text', () => {
    const stats = computeStats('same\n', 'same\n');
    expect(stats.additions).toBe(0);
    expect(stats.deletions).toBe(0);
    expect(stats.unchanged).toBe(1);
  });
});
