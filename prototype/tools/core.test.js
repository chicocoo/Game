// Tests de la logique ATOLL : node --test prototype/tools/core.test.js
'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const A = require('../atoll-core.js');

const N = A.SIZE;
const at = (r, c) => r * N + c;

function gridFrom(rows) {
  const g = new Uint8Array(N * N);
  rows.forEach((line, r) => {
    [...line].forEach((ch, c) => {
      g[at(r, c)] = ch === '#' ? A.BLOCK : ch === 'o' ? A.PEARL : A.EMPTY;
    });
  });
  return g;
}

const dot = A.PIECES.find((p) => p.family === 'dot');
const i2h = A.PIECES.find((p) => p.family === 'i2' && p.w === 2);

function gameWith(grid, hand, opts) {
  const g = A.createGame(Object.assign({ seed: 1 }, opts));
  g.grid = grid;
  g.hand = hand.slice();
  return g;
}

test('deux cases vides encerclées deviennent un lagon (perles)', () => {
  const grid = gridFrom([
    '.........',
    '...####..',
    '...#..#..',
    '.........',
  ]);
  const g = gameWith(grid, [i2h, dot, dot]);
  const ev = A.place(g, 0, 3, 4);
  assert.ok(ev);
  assert.equal(ev.lagoons.length, 1);
  assert.deepEqual(ev.lagoons[0].cells, [at(2, 4), at(2, 5)]);
  assert.equal(g.grid[at(2, 4)], A.PEARL);
  assert.equal(ev.points.lagoon, A.lagoonPoints(2));
});

test("par défaut, un trou d'une seule case reste un trou", () => {
  const grid = gridFrom([
    '.........',
    '...###...',
    '...#.#...',
    '.........',
  ]);
  const g = gameWith(grid, [dot, dot, dot]);
  const ev = A.place(g, 0, 3, 4);
  assert.equal(ev.lagoons.length, 0);
  assert.equal(g.grid[at(2, 4)], A.EMPTY);
});

test('bonus « Plongeur » (taille min 1) : une seule case suffit', () => {
  const grid = gridFrom([
    '.........',
    '...###...',
    '...#.#...',
    '.........',
  ]);
  const g = gameWith(grid, [dot, dot, dot], { minLagoon: 1 });
  const ev = A.place(g, 0, 3, 4);
  assert.equal(ev.lagoons.length, 1);
  assert.equal(g.grid[at(2, 4)], A.PEARL);
});

test("l'aperçu annonce les perles et les lignes avant la pose", () => {
  const grid = gridFrom([
    '.........',
    '...####..',
    '...#..#..',
    '.........',
  ]);
  const pv = A.preview(grid, i2h, 3, 4);
  assert.deepEqual(pv.pearls, [at(2, 4), at(2, 5)]);
  assert.equal(pv.lines.length, 0);
  assert.equal(A.preview(grid, i2h, 1, 3), null, 'case occupée');
});

test("une zone reliée au bord (la mer) ne devient pas un lagon", () => {
  const grid = gridFrom([
    '#.#......',
    '###......',
  ]);
  // La case (0,1) touche le bord : elle reste de la mer.
  assert.equal(A.findLagoons(grid).length, 0);
});

test('deux lagons distincts sont détectés séparément', () => {
  const grid = gridFrom([
    '.........',
    '.###.###.',
    '.#.#.#.#.',
    '.###.###.',
  ]);
  const lag = A.findLagoons(grid);
  assert.equal(lag.length, 2);
  assert.deepEqual(lag.map((l) => l.length), [1, 1]);
});

test('un grand lagon donne le bonus', () => {
  assert.equal(A.lagoonPoints(6), 20 * 6 + 100);
  assert.equal(A.lagoonPoints(5), 100);
});

test('les perles comptent comme pleines et multiplient la ligne', () => {
  // Ligne 4 : 7 blocs + 1 trou encerclé par la ligne 3 et 5 + 1 case à poser.
  const grid = gridFrom([
    '.........',
    '.........',
    '.........',
    '...#.....',
    '###.####.',
    '...#.....',
  ]);
  const g = gameWith(grid, [dot, dot, dot], { minLagoon: 1 });
  // Poser en (4,8) ferme la ligne 4... sauf le trou (4,3) : il est encerclé
  // (haut, bas, gauche, droite occupés) -> perle -> la ligne se complète.
  const ev = A.place(g, 0, 4, 8);
  assert.equal(ev.lagoons.length, 1);
  assert.equal(ev.lines.length, 1);
  assert.equal(ev.lines[0].type, 'row');
  assert.equal(ev.lines[0].pearls, 1);
  assert.equal(ev.lines[0].points, 10 * N * 2);
  assert.equal(g.grid[at(4, 3)], A.EMPTY, 'la ligne est effacée, perle comprise');
  assert.equal(g.pearlsCashed, 1);
});

test('lignes multiples : multiplicateur par nombre de lignes', () => {
  const grid = gridFrom([
    '########.',
    '########.',
  ]);
  const i2v = A.PIECES.find((p) => p.family === 'i2' && p.h === 2);
  const g = gameWith(grid, [i2v, dot, dot]);
  const ev = A.place(g, 0, 0, 8);
  assert.equal(ev.lines.length, 2);
  assert.equal(ev.points.lines, (90 + 90) * 2);
});

test('combo : se prolonge si on efface dans la fenêtre de 3 poses', () => {
  const grid = gridFrom([
    '########.',
    '.........',
    '########.',
  ]);
  const g = gameWith(grid, [dot, dot, dot]);
  A.place(g, 0, 0, 8);
  assert.equal(g.combo, 1);
  A.place(g, 1, 5, 5); // pas d'effacement
  const ev = A.place(g, 2, 2, 8);
  assert.equal(ev.combo, 2);
  assert.equal(ev.points.lines, Math.round(90 * 1 * 1.5));
});

test('pose impossible hors grille ou sur une case occupée', () => {
  const grid = gridFrom(['#........']);
  const g = gameWith(grid, [i2h, dot, dot]);
  assert.equal(A.place(g, 0, 0, 8), null);
  assert.equal(A.place(g, 1, 0, 0), null);
});

test('le défi du jour donne la même suite de pièces à tout le monde', () => {
  const seed = A.hashString('atoll-daily-2026-09-24');
  const a = A.createGame({ mode: 'daily', seed });
  const b = A.createGame({ mode: 'daily', seed });
  assert.deepEqual(a.sequence.map((p) => p.id), b.sequence.map((p) => p.id));
  assert.equal(a.sequence.length, A.DAILY_PIECES);
  const c = A.createGame({ mode: 'daily', seed: A.hashString('atoll-daily-2026-09-25') });
  assert.notDeepEqual(a.sequence.map((p) => p.id), c.sequence.map((p) => p.id));
});

test('le jour du défi suit le reset des classements Play Games (UTC-7)', () => {
  assert.equal(A.dailyKey(new Date('2026-09-25T06:59:00Z')), '2026-09-24');
  assert.equal(A.dailyKey(new Date('2026-09-25T07:00:00Z')), '2026-09-25');
});

test('fin de partie quand plus aucune pièce ne rentre, puis « continuer »', () => {
  const rows = [];
  for (let r = 0; r < N; r++) rows.push(r % 2 ? '#.#.#.#.#' : '.#.#.#.#.');
  const o3 = A.PIECES.find((p) => p.family === 'o3');
  const g = gameWith(gridFrom(rows), [o3, o3, o3]);
  assert.equal(A.hasMove(g), false);
  g.over = true;
  assert.equal(A.revive(g), true);
  assert.equal(g.over, false);
  assert.ok(A.hasMove(g));
  g.over = true;
  assert.equal(A.revive(g), false, 'un seul « continuer » par partie');
});

test('toutes les pièces sont normalisées et uniques', () => {
  const keys = new Set();
  for (const p of A.PIECES) {
    const k = p.cells.map((c) => c.join(',')).join(';');
    assert.ok(!keys.has(k), 'doublon ' + p.id);
    keys.add(k);
    assert.ok(p.cells.every(([r, c]) => r >= 0 && c >= 0 && r < p.h && c < p.w));
  }
});
