# React Diff Studio

`react-diff-studio` is a standalone React app inside this repository. It provides a side-by-side text diff viewer that compares two text blocks line by line and highlights unchanged, changed, added, and removed rows.

## Features

- Side-by-side comparison for source text and target text
- Line-level diff powered by a longest common subsequence matcher
- Summary cards for unchanged, changed, added, removed, total rows, and similarity
- Responsive layout that stacks cleanly on narrow screens
- Vitest and Testing Library coverage for the diff engine and key UI flow

## Commands

```bash
npm install
npm run dev
npm run test
npm run build
```

## Verification

The app was verified with:

```bash
npm run test
npm run build
```

The project is isolated from the Spring Boot backend and can be developed independently from the Maven build.
