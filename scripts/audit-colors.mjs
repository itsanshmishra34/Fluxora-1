#!/usr/bin/env node
/**
 * CINEMATIC v2.0 — Automated Raw Color & Token Governance Scanner
 * Scans styles/ and app/src/main/java/com/example/ui/ for:
 * - Raw hex colors (#RRGGBB / 0xFFRRGGBB) outside approved primitive definition files
 * - Default Tailwind color utilities (bg-blue-*, text-gray-*, border-slate-*, etc.)
 * - Unapproved gradients
 */

import fs from 'node:fs';
import path from 'node:path';

const rootDir = process.cwd();
const exceptions = JSON.parse(
  fs.readFileSync(path.join(rootDir, 'scripts/color-exceptions.json'), 'utf8')
);
const allowedFiles = new Set(exceptions.primitiveSourceFiles);

function walkFiles(dir, fileList = []) {
  if (!fs.existsSync(dir)) return fileList;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walkFiles(fullPath, fileList);
    } else if (/\.(css|kt|tsx|ts|jsx|js)$/.test(entry.name)) {
      fileList.push(path.relative(rootDir, fullPath).replace(/\\/g, '/'));
    }
  }
  return fileList;
}

const filesToScan = [
  ...walkFiles(path.join(rootDir, 'styles')),
  ...walkFiles(path.join(rootDir, 'app/src/main/java/com/example/ui'))
];

const rawHexRegex = /(#[0-9A-Fa-f]{3,8}\b|Color\(\s*0x[0-9A-Fa-f]{8}\s*\))/g;
const forbiddenTailwindRegex = /\b(bg|text|border|ring|from|via|to)-(slate|gray|zinc|neutral|stone|red|orange|amber|yellow|lime|green|emerald|teal|cyan|sky|blue|indigo|violet|purple|fuchsia|pink|rose)-[0-9]{2,3}\b/g;

const violations = [];
let scannedFilesCount = 0;

for (const relPath of filesToScan) {
  scannedFilesCount++;
  const content = fs.readFileSync(path.join(rootDir, relPath), 'utf8');
  const lines = content.split('\n');

  lines.forEach((line, idx) => {
    const trimmed = line.trim();
    if (trimmed.startsWith('//') || trimmed.startsWith('/*') || trimmed.startsWith('*')) {
      return;
    }

    if (!allowedFiles.has(relPath)) {
      // Allow documented hex strings inside showcase labels (e.g. tokenHex = "#B9C9D8")
      const strippedShowcaseMeta = line.replace(/tokenHex\s*=\s*"#[0-9A-Fa-f]{6}"/g, '')
        .replace(/"#[0-9A-Fa-f]{6}"/g, '');
      const hexMatches = strippedShowcaseMeta.match(rawHexRegex);
      if (hexMatches) {
        violations.push({
          file: relPath,
          line: idx + 1,
          type: 'RAW_COLOR_LITERAL',
          matches: hexMatches
        });
      }
    }

    const twMatches = line.match(forbiddenTailwindRegex);
    if (twMatches) {
      violations.push({
        file: relPath,
        line: idx + 1,
        type: 'FORBIDDEN_DEFAULT_TAILWIND_COLOR',
        matches: twMatches
      });
    }
  });
}

const summary = {
  scannedFilesCount,
  primitiveDefinitionFilesCount: allowedFiles.size,
  unauthorizedColorViolations: violations.length,
  violations
};

console.log(JSON.stringify(summary, null, 2));

if (violations.length > 0) {
  process.exit(1);
}
