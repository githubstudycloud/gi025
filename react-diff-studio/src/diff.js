function toLines(text) {
  if (!text) {
    return [];
  }

  return text.replace(/\r\n/g, '\n').split('\n');
}

function buildLcsMatrix(leftLines, rightLines) {
  const matrix = Array.from({ length: leftLines.length + 1 }, () =>
    Array(rightLines.length + 1).fill(0)
  );

  for (let leftIndex = leftLines.length - 1; leftIndex >= 0; leftIndex -= 1) {
    for (let rightIndex = rightLines.length - 1; rightIndex >= 0; rightIndex -= 1) {
      if (leftLines[leftIndex] === rightLines[rightIndex]) {
        matrix[leftIndex][rightIndex] = matrix[leftIndex + 1][rightIndex + 1] + 1;
      } else {
        matrix[leftIndex][rightIndex] = Math.max(
          matrix[leftIndex + 1][rightIndex],
          matrix[leftIndex][rightIndex + 1]
        );
      }
    }
  }

  return matrix;
}

function collectOperations(leftLines, rightLines, matrix) {
  const operations = [];
  let leftIndex = 0;
  let rightIndex = 0;

  while (leftIndex < leftLines.length && rightIndex < rightLines.length) {
    if (leftLines[leftIndex] === rightLines[rightIndex]) {
      operations.push({
        type: 'equal',
        leftText: leftLines[leftIndex],
        rightText: rightLines[rightIndex]
      });
      leftIndex += 1;
      rightIndex += 1;
      continue;
    }

    if (matrix[leftIndex + 1][rightIndex] >= matrix[leftIndex][rightIndex + 1]) {
      operations.push({
        type: 'removed',
        leftText: leftLines[leftIndex]
      });
      leftIndex += 1;
      continue;
    }

    operations.push({
      type: 'added',
      rightText: rightLines[rightIndex]
    });
    rightIndex += 1;
  }

  while (leftIndex < leftLines.length) {
    operations.push({
      type: 'removed',
      leftText: leftLines[leftIndex]
    });
    leftIndex += 1;
  }

  while (rightIndex < rightLines.length) {
    operations.push({
      type: 'added',
      rightText: rightLines[rightIndex]
    });
    rightIndex += 1;
  }

  return operations;
}

function createCell(number, text) {
  if (!text && number === null) {
    return null;
  }

  return {
    number,
    text
  };
}

function alignOperations(operations) {
  const rows = [];
  let leftNumber = 1;
  let rightNumber = 1;
  let rowId = 1;

  for (let index = 0; index < operations.length; index += 1) {
    const current = operations[index];

    if (current.type === 'equal') {
      rows.push({
        id: `row-${rowId}`,
        status: 'equal',
        left: createCell(leftNumber, current.leftText),
        right: createCell(rightNumber, current.rightText)
      });
      rowId += 1;
      leftNumber += 1;
      rightNumber += 1;
      continue;
    }

    const removedBlock = [];
    const addedBlock = [];

    while (index < operations.length && operations[index].type !== 'equal') {
      if (operations[index].type === 'removed') {
        removedBlock.push(operations[index].leftText);
      } else {
        addedBlock.push(operations[index].rightText);
      }
      index += 1;
    }

    index -= 1;
    const blockSize = Math.max(removedBlock.length, addedBlock.length);

    for (let blockIndex = 0; blockIndex < blockSize; blockIndex += 1) {
      const leftText = removedBlock[blockIndex];
      const rightText = addedBlock[blockIndex];
      const isChanged = typeof leftText === 'string' && typeof rightText === 'string';
      const status = isChanged ? 'changed' : leftText ? 'removed' : 'added';

      rows.push({
        id: `row-${rowId}`,
        status,
        left: typeof leftText === 'string' ? createCell(leftNumber, leftText) : null,
        right: typeof rightText === 'string' ? createCell(rightNumber, rightText) : null
      });

      rowId += 1;

      if (typeof leftText === 'string') {
        leftNumber += 1;
      }

      if (typeof rightText === 'string') {
        rightNumber += 1;
      }
    }
  }

  return rows;
}

function summarizeRows(rows, leftCount, rightCount) {
  const summary = {
    unchanged: 0,
    changed: 0,
    added: 0,
    removed: 0,
    total: rows.length,
    similarity: 100
  };

  rows.forEach((row) => {
    if (row.status === 'equal') {
      summary.unchanged += 1;
      return;
    }

    if (row.status === 'changed') {
      summary.changed += 1;
      return;
    }

    if (row.status === 'added') {
      summary.added += 1;
      return;
    }

    summary.removed += 1;
  });

  const baseline = Math.max(leftCount, rightCount);
  summary.similarity = baseline === 0 ? 100 : Math.round((summary.unchanged / baseline) * 100);

  return summary;
}

function buildDiffReport(leftText, rightText) {
  const leftLines = toLines(leftText);
  const rightLines = toLines(rightText);
  const matrix = buildLcsMatrix(leftLines, rightLines);
  const operations = collectOperations(leftLines, rightLines, matrix);
  const rows = alignOperations(operations);

  return {
    leftLineCount: leftLines.length,
    rightLineCount: rightLines.length,
    rows,
    summary: summarizeRows(rows, leftLines.length, rightLines.length)
  };
}

export { buildDiffReport, toLines };
