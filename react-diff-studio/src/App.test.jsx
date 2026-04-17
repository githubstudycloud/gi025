import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import App from './App';

describe('App', () => {
  it('renders the diff workspace and seeded summary', () => {
    render(<App />);

    expect(
      screen.getByRole('heading', { name: /readable diff views for line-by-line text changes/i })
    ).toBeInTheDocument();
    expect(screen.getByTestId('stat-changed')).toHaveTextContent('2');
    expect(screen.getByTestId('stat-added')).toHaveTextContent('1');
    expect(screen.getByTestId('stat-removed')).toHaveTextContent('0');
  });

  it('clears the inputs and shows the empty state', async () => {
    const user = userEvent.setup();

    render(<App />);
    await user.click(screen.getByRole('button', { name: /clear/i }));

    await waitFor(() => {
      expect(screen.getByTestId('stat-total')).toHaveTextContent('0');
      expect(screen.getByText(/no diff to display/i)).toBeInTheDocument();
    });
  });

  it('recomputes the report when both texts become identical', async () => {
    const user = userEvent.setup();

    render(<App />);

    const sourceInput = screen.getByLabelText('Source Text');
    const targetInput = screen.getByLabelText('Target Text');

    await user.clear(sourceInput);
    await user.type(sourceInput, 'same line');
    await user.clear(targetInput);
    await user.type(targetInput, 'same line');

    await waitFor(() => {
      expect(screen.getByTestId('stat-unchanged')).toHaveTextContent('1');
      expect(screen.getByTestId('stat-changed')).toHaveTextContent('0');
      expect(screen.getByTestId('stat-added')).toHaveTextContent('0');
      expect(screen.getByTestId('stat-removed')).toHaveTextContent('0');
      expect(screen.getByTestId('stat-similarity')).toHaveTextContent('100%');
    });
  });
});
