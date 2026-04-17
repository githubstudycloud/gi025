import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import App from '../App';

describe('App Component', () => {
  it('should render the header', () => {
    render(<App />);
    expect(screen.getByText('React Diff Viewer')).toBeInTheDocument();
    expect(screen.getByText('文本差异对比工具')).toBeInTheDocument();
  });

  it('should have unified and split view buttons', () => {
    render(<App />);
    expect(screen.getByTestId('btn-unified')).toBeInTheDocument();
    expect(screen.getByTestId('btn-split')).toBeInTheDocument();
  });

  it('should have two text inputs', () => {
    render(<App />);
    expect(screen.getByTestId('input-old')).toBeInTheDocument();
    expect(screen.getByTestId('input-new')).toBeInTheDocument();
  });

  it('should not show diff output when both inputs are empty', () => {
    render(<App />);
    expect(screen.queryByTestId('diff-output')).not.toBeInTheDocument();
  });

  it('should load sample text when clicking the sample button', () => {
    render(<App />);
    fireEvent.click(screen.getByTestId('btn-sample'));

    const oldInput = screen.getByTestId('input-old') as HTMLTextAreaElement;
    const newInput = screen.getByTestId('input-new') as HTMLTextAreaElement;

    expect(oldInput.value).toContain('function greet(name)');
    expect(newInput.value).toContain('function greet(name: string)');
  });

  it('should show diff output after loading sample', () => {
    render(<App />);
    fireEvent.click(screen.getByTestId('btn-sample'));
    expect(screen.getByTestId('diff-output')).toBeInTheDocument();
    expect(screen.getByTestId('diff-stats')).toBeInTheDocument();
  });

  it('should show unified view by default', () => {
    render(<App />);
    fireEvent.click(screen.getByTestId('btn-sample'));
    expect(screen.getByTestId('unified-view')).toBeInTheDocument();
  });

  it('should switch to split view', () => {
    render(<App />);
    fireEvent.click(screen.getByTestId('btn-sample'));
    fireEvent.click(screen.getByTestId('btn-split'));
    expect(screen.getByTestId('split-view')).toBeInTheDocument();
  });

  it('should clear inputs when clicking clear button', () => {
    render(<App />);
    fireEvent.click(screen.getByTestId('btn-sample'));
    fireEvent.click(screen.getByTestId('btn-clear'));

    const oldInput = screen.getByTestId('input-old') as HTMLTextAreaElement;
    const newInput = screen.getByTestId('input-new') as HTMLTextAreaElement;

    expect(oldInput.value).toBe('');
    expect(newInput.value).toBe('');
    expect(screen.queryByTestId('diff-output')).not.toBeInTheDocument();
  });

  it('should show diff when user types in inputs', () => {
    render(<App />);
    const oldInput = screen.getByTestId('input-old');
    const newInput = screen.getByTestId('input-new');

    fireEvent.change(oldInput, { target: { value: 'hello' } });
    fireEvent.change(newInput, { target: { value: 'world' } });

    expect(screen.getByTestId('diff-output')).toBeInTheDocument();
  });

  it('should display correct stats', () => {
    render(<App />);
    const oldInput = screen.getByTestId('input-old');
    const newInput = screen.getByTestId('input-new');

    fireEvent.change(oldInput, { target: { value: 'line1\nline2\n' } });
    fireEvent.change(newInput, { target: { value: 'line1\nline3\n' } });

    expect(screen.getByTestId('stat-added')).toBeInTheDocument();
    expect(screen.getByTestId('stat-removed')).toBeInTheDocument();
  });
});
