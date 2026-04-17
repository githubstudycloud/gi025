import { useState, useCallback } from 'react';
import { computeLineDiff, computeSplitDiff, computeStats, type DiffLine, type SplitLine, type DiffStats } from '../utils/diff';

export type ViewMode = 'unified' | 'split';

interface UseDiffReturn {
  oldText: string;
  newText: string;
  setOldText: (text: string) => void;
  setNewText: (text: string) => void;
  viewMode: ViewMode;
  setViewMode: (mode: ViewMode) => void;
  lineDiff: DiffLine[];
  splitDiff: SplitLine[];
  stats: DiffStats;
  handleFileUpload: (side: 'old' | 'new') => (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export function useDiff(): UseDiffReturn {
  const [oldText, setOldText] = useState('');
  const [newText, setNewText] = useState('');
  const [viewMode, setViewMode] = useState<ViewMode>('unified');

  const lineDiff = computeLineDiff(oldText, newText);
  const splitDiff = computeSplitDiff(oldText, newText);
  const stats = computeStats(oldText, newText);

  const handleFileUpload = useCallback(
    (side: 'old' | 'new') => (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (event) => {
        const text = event.target?.result as string;
        if (side === 'old') {
          setOldText(text);
        } else {
          setNewText(text);
        }
      };
      reader.readAsText(file);
    },
    []
  );

  return {
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
  };
}
