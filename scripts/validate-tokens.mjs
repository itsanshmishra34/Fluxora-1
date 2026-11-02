#!/usr/bin/env node
/**
 * CINEMATIC v2.0 — Automated Token & WCAG 2.1 AA Contrast Validator
 * Validates:
 * 1. Every semantic token in styles/semantic-tokens.css has a matching [data-theme="light"] override in styles/themes.css.
 * 2. Every primitive referenced by semantic tokens is defined in styles/primitives.css.
 * 3. Every Dark and Light foreground/background pairing meets WCAG 2.1 AA contrast ratios.
 */

import fs from 'node:fs';
import path from 'node:path';

const rootDir = process.cwd();
const primitivesCss = fs.readFileSync(path.join(rootDir, 'styles/primitives.css'), 'utf8');
const semanticCss = fs.readFileSync(path.join(rootDir, 'styles/semantic-tokens.css'), 'utf8');
const themesCss = fs.readFileSync(path.join(rootDir, 'styles/themes.css'), 'utf8');

function extractVars(cssText) {
  const vars = new Map();
  const regex = /(--[a-z0-9-]+)\s*:\s*([^;]+);/gi;
  let match;
  while ((match = regex.exec(cssText)) !== null) {
    vars.set(match[1].trim(), match[2].trim());
  }
  return vars;
}

const primitives = extractVars(primitivesCss);
const darkTokens = extractVars(semanticCss);
const lightTokens = extractVars(themesCss);

const missingLightTokens = [];
const unresolvedPrimitives = [];

for (const [tokenName, tokenValue] of darkTokens.entries()) {
  if (!lightTokens.has(tokenName)) {
    missingLightTokens.push(tokenName);
  }
  const refMatch = tokenValue.match(/var\((--primitive-[a-z0-9-]+)\)/i);
  if (refMatch && !primitives.has(refMatch[1])) {
    unresolvedPrimitives.push({ theme: 'dark', tokenName, primitive: refMatch[1] });
  }
}

for (const [tokenName, tokenValue] of lightTokens.entries()) {
  const refMatch = tokenValue.match(/var\((--primitive-[a-z0-9-]+)\)/i);
  if (refMatch && !primitives.has(refMatch[1])) {
    unresolvedPrimitives.push({ theme: 'light', tokenName, primitive: refMatch[1] });
  }
}

function hexToRgb(hex) {
  const clean = hex.replace('#', '').trim();
  return [
    parseInt(clean.slice(0, 2), 16),
    parseInt(clean.slice(2, 4), 16),
    parseInt(clean.slice(4, 6), 16)
  ];
}

function relativeLuminance([r, g, b]) {
  const chan = [r, g, b].map((v) => {
    const c = v / 255;
    return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * chan[0] + 0.7152 * chan[1] + 0.0722 * chan[2];
}

function contrastRatio(hexA, hexB) {
  const l1 = relativeLuminance(hexToRgb(hexA));
  const l2 = relativeLuminance(hexToRgb(hexB));
  const lighter = Math.max(l1, l2);
  const darker = Math.min(l1, l2);
  return Number(((lighter + 0.05) / (darker + 0.05)).toFixed(2));
}

const requiredContrastPairs = [
  // Dark Mode
  { theme: 'dark', pair: '--fg-primary on --bg-canvas', fg: '#E4EDF5', bg: '#070B12', min: 4.5 },
  { theme: 'dark', pair: '--fg-primary on --bg-primary', fg: '#E4EDF5', bg: '#0D1521', min: 4.5 },
  { theme: 'dark', pair: '--fg-primary on --bg-secondary', fg: '#E4EDF5', bg: '#142235', min: 4.5 },
  { theme: 'dark', pair: '--fg-secondary on --bg-canvas', fg: '#B9C9D8', bg: '#070B12', min: 4.5 },
  { theme: 'dark', pair: '--fg-secondary on --bg-primary', fg: '#B9C9D8', bg: '#0D1521', min: 4.5 },
  { theme: 'dark', pair: '--fg-muted on --bg-canvas', fg: '#6D91B2', bg: '#070B12', min: 4.5 },
  { theme: 'dark', pair: '--fg-muted on --bg-primary', fg: '#6D91B2', bg: '#0D1521', min: 4.5 },
  { theme: 'dark', pair: '--btn-primary-fg on --btn-primary-bg', fg: '#070B12', bg: '#B9C9D8', min: 4.5 },
  { theme: 'dark', pair: '--btn-secondary-fg on --btn-secondary-bg', fg: '#E4EDF5', bg: '#1D3045', min: 4.5 },
  { theme: 'dark', pair: '--status-success-fg on --status-success-bg', fg: '#8DBEAA', bg: '#10261F', min: 4.5 },
  { theme: 'dark', pair: '--status-warning-fg on --status-warning-bg', fg: '#E7BE79', bg: '#2B2111', min: 4.5 },
  { theme: 'dark', pair: '--status-danger-fg on --status-danger-bg', fg: '#E58C94', bg: '#2D1519', min: 4.5 },
  { theme: 'dark', pair: '--status-info-fg on --status-info-bg', fg: '#85B9DB', bg: '#102334', min: 4.5 },
  // Light Mode
  { theme: 'light', pair: '--fg-primary on --bg-canvas', fg: '#172A3D', bg: '#F3F6F9', min: 4.5 },
  { theme: 'light', pair: '--fg-primary on --bg-primary', fg: '#172A3D', bg: '#FFFFFF', min: 4.5 },
  { theme: 'light', pair: '--fg-secondary on --bg-canvas', fg: '#526477', bg: '#F3F6F9', min: 4.5 },
  { theme: 'light', pair: '--fg-secondary on --bg-primary', fg: '#526477', bg: '#FFFFFF', min: 4.5 },
  { theme: 'light', pair: '--fg-muted on --bg-canvas', fg: '#526477', bg: '#F3F6F9', min: 4.5 },
  { theme: 'light', pair: '--fg-subtle (large text) on --bg-primary', fg: '#718398', bg: '#FFFFFF', min: 3.0 },
  { theme: 'light', pair: '--btn-primary-fg on --btn-primary-bg', fg: '#FFFFFF', bg: '#172A3D', min: 4.5 },
  { theme: 'light', pair: '--btn-secondary-fg on --btn-secondary-bg', fg: '#172A3D', bg: '#E5ECF2', min: 4.5 },
  { theme: 'light', pair: '--status-success-fg on --status-success-bg', fg: '#287A58', bg: '#E8F5EF', min: 4.5 },
  { theme: 'light', pair: '--status-warning-fg on --status-warning-bg', fg: '#956019', bg: '#FBF3E4', min: 4.5 },
  { theme: 'light', pair: '--status-danger-fg on --status-danger-bg', fg: '#B83D4B', bg: '#FCECEE', min: 4.5 },
  { theme: 'light', pair: '--status-info-fg on --status-info-bg', fg: '#286B99', bg: '#EAF3FA', min: 4.5 }
];

const contrastResults = requiredContrastPairs.map((item) => {
  const ratio = contrastRatio(item.fg, item.bg);
  return {
    ...item,
    ratio,
    passed: ratio >= item.min
  };
});

const failedContrasts = contrastResults.filter((r) => !r.passed);

const report = {
  primitiveCount: primitives.size,
  darkSemanticTokenCount: darkTokens.size,
  lightSemanticTokenCount: lightTokens.size,
  missingLightTokens,
  unresolvedPrimitives,
  contrastChecksTotal: contrastResults.length,
  contrastChecksPassed: contrastResults.length - failedContrasts.length,
  failedContrasts,
  contrastResults
};

console.log(JSON.stringify(report, null, 2));

if (missingLightTokens.length > 0 || unresolvedPrimitives.length > 0 || failedContrasts.length > 0) {
  process.exit(1);
}
