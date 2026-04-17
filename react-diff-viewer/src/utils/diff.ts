import { diffLines, diffWords, diffChars } from 'diff';
import type { Change } from 'diff';

export type DiffType = 'lines' | 'words' | 'chars';

export interface DiffLine {
  type: 'added' | 'removed' | 'unchanged';
  value: string;
  lineNumberOld?: number;
  lineNumberNew?: number;
}

/**
 * 计算两段文本的差异
 */
export function computeDiff(
  oldText: string,
  newText: string,
  diffType: DiffType = 'lines'
): Change[] {
  switch (diffType) {
    case 'words':
      return diffWords(oldText, newText);
    case 'chars':
      return diffChars(oldText, newText);
    case 'lines':
    default:
      return diffLines(oldText, newText);
  }
}

/**
 * 将 diff 结果转换为带行号的行数据（用于 unified 视图）
 */
export function computeLineDiff(
  oldText: string,
  newText: string
): DiffLine[] {
  const changes = diffLines(oldText, newText);
  const result: DiffLine[] = [];
  let oldLineNum = 1;
  let newLineNum = 1;

  for (const change of changes) {
    const lines = change.value.replace(/\n$/, '').split('\n');

    for (const line of lines) {
      if (change.added) {
        result.push({
          type: 'added',
          value: line,
          lineNumberNew: newLineNum++,
        });
      } else if (change.removed) {
        result.push({
          type: 'removed',
          value: line,
          lineNumberOld: oldLineNum++,
        });
      } else {
        result.push({
          type: 'unchanged',
          value: line,
          lineNumberOld: oldLineNum++,
          lineNumberNew: newLineNum++,
        });
      }
    }
  }

  return result;
}

/**
 * 将 diff 结果转换为左右对照数据（用于 split 视图）
 */
export interface SplitLine {
  left: { type: 'removed' | 'unchanged' | 'empty'; value: string; lineNumber?: number };
  right: { type: 'added' | 'unchanged' | 'empty'; value: string; lineNumber?: number };
}

export function computeSplitDiff(
  oldText: string,
  newText: string
): SplitLine[] {
  const changes = diffLines(oldText, newText);
  const result: SplitLine[] = [];
  let oldLineNum = 1;
  let newLineNum = 1;

  let i = 0;
  while (i < changes.length) {
    const change = changes[i];

    if (!change.added && !change.removed) {
      // 不变的行
      const lines = change.value.replace(/\n$/, '').split('\n');
      for (const line of lines) {
        result.push({
          left: { type: 'unchanged', value: line, lineNumber: oldLineNum++ },
          right: { type: 'unchanged', value: line, lineNumber: newLineNum++ },
        });
      }
      i++;
    } else if (change.removed && i + 1 < changes.length && changes[i + 1].added) {
      // 删除 + 添加 = 修改
      const removedLines = change.value.replace(/\n$/, '').split('\n');
      const addedLines = changes[i + 1].value.replace(/\n$/, '').split('\n');
      const maxLen = Math.max(removedLines.length, addedLines.length);

      for (let j = 0; j < maxLen; j++) {
        result.push({
          left: j < removedLines.length
            ? { type: 'removed', value: removedLines[j], lineNumber: oldLineNum++ }
            : { type: 'empty', value: '' },
          right: j < addedLines.length
            ? { type: 'added', value: addedLines[j], lineNumber: newLineNum++ }
            : { type: 'empty', value: '' },
        });
      }
      i += 2;
    } else if (change.removed) {
      const lines = change.value.replace(/\n$/, '').split('\n');
      for (const line of lines) {
        result.push({
          left: { type: 'removed', value: line, lineNumber: oldLineNum++ },
          right: { type: 'empty', value: '' },
        });
      }
      i++;
    } else if (change.added) {
      const lines = change.value.replace(/\n$/, '').split('\n');
      for (const line of lines) {
        result.push({
          left: { type: 'empty', value: '' },
          right: { type: 'added', value: line, lineNumber: newLineNum++ },
        });
      }
      i++;
    } else {
      i++;
    }
  }

  return result;
}

/**
 * 统计差异信息
 */
export interface DiffStats {
  additions: number;
  deletions: number;
  unchanged: number;
}

export function computeStats(oldText: string, newText: string): DiffStats {
  const changes = diffLines(oldText, newText);
  let additions = 0;
  let deletions = 0;
  let unchanged = 0;

  for (const change of changes) {
    const lineCount = change.value.replace(/\n$/, '').split('\n').length;
    if (change.added) {
      additions += lineCount;
    } else if (change.removed) {
      deletions += lineCount;
    } else {
      unchanged += lineCount;
    }
  }

  return { additions, deletions, unchanged };
}
