// Simulation par bot glouton pour calibrer ATOLL.
// Usage : node prototype/tools/simulate.js [nbParties]
'use strict';
const A = require('../atoll-core.js');

const N = A.SIZE;
const O3 = A.PIECES.find((p) => p.family === 'o3');

function cloneForEval(g) {
  return Object.assign({}, g, {
    rng: () => 0.5,
    grid: g.grid.slice(),
    hand: g.hand.slice(),
    score: 0, lagoons: 0, pearlsCreated: 0, pearlsCashed: 0, linesCleared: 0,
    sequence: [], cursor: 0, over: false
  });
}

function boardValue(grid) {
  let empties = 0, isolated = 0;
  for (let i = 0; i < N * N; i++) {
    if (grid[i] !== A.EMPTY) continue;
    empties++;
    const r = (i / N) | 0, c = i % N;
    const free = [[r - 1, c], [r + 1, c], [r, c - 1], [r, c + 1]]
      .filter(([y, x]) => y >= 0 && x >= 0 && y < N && x < N && grid[y * N + x] === A.EMPTY).length;
    if (free === 0) isolated++;
  }
  return 4 * empties - 8 * isolated + (A.pieceFits(grid, O3) ? 30 : 0);
}

function bestMove(g) {
  let best = null;
  g.hand.forEach((piece, k) => {
    if (!piece) return;
    for (let r = 0; r <= N - piece.h; r++) {
      for (let c = 0; c <= N - piece.w; c++) {
        if (!A.canPlaceOn(g.grid, piece, r, c)) continue;
        const sim = cloneForEval(g);
        const ev = A.place(sim, k, r, c);
        const v = ev.total + boardValue(sim.grid);
        if (!best || v > best.v) best = { k, r, c, v };
      }
    }
  });
  return best;
}

function playOne(opts) {
  const g = A.createGame(opts);
  let placements = 0, placementsWithLagoon = 0, clearsWithPearl = 0, clears = 0, lagoonPts = 0, linePts = 0;
  while (!g.over) {
    const m = bestMove(g);
    if (!m) break;
    const ev = A.place(g, m.k, m.r, m.c);
    placements++;
    lagoonPts += ev.points.lagoon;
    linePts += ev.points.lines;
    if (ev.lagoons.length) placementsWithLagoon++;
    if (ev.lines.length) {
      clears++;
      if (ev.lines.some((L) => L.pearls > 0)) clearsWithPearl++;
    }
    if (placements > 3000) break; // garde-fou
  }
  return {
    placements, score: g.score, lines: g.linesCleared, lagoons: g.lagoons,
    pearls: g.pearlsCreated, cashed: g.pearlsCashed, biggest: g.biggestLagoon, bestCombo: g.bestCombo,
    lagoonRate: placements ? placementsWithLagoon / placements : 0,
    lagoonShare: g.score ? lagoonPts / g.score : 0,
    lineShare: g.score ? linePts / g.score : 0,
    pearlClearRate: clears ? clearsWithPearl / clears : 0
  };
}

function stats(values) {
  const s = values.slice().sort((a, b) => a - b);
  const mean = s.reduce((a, b) => a + b, 0) / s.length;
  const q = (p) => s[Math.min(s.length - 1, Math.floor(p * s.length))];
  return { mean, p10: q(0.1), median: q(0.5), p90: q(0.9), max: s[s.length - 1] };
}

function run(label, n, makeOpts) {
  const res = [];
  for (let i = 0; i < n; i++) res.push(playOne(makeOpts(i)));
  const pick = (k) => stats(res.map((r) => r[k]));
  const fmt = (o, d = 0) => `moy ${o.mean.toFixed(d)} | p10 ${o.p10.toFixed(d)} | méd ${o.median.toFixed(d)} | p90 ${o.p90.toFixed(d)} | max ${o.max.toFixed(d)}`;
  console.log(`\n=== ${label} (${n} parties) ===`);
  console.log('Poses par partie      ', fmt(pick('placements')));
  console.log('Score                 ', fmt(pick('score')));
  console.log('Lignes effacées       ', fmt(pick('lines')));
  console.log('Lagons créés          ', fmt(pick('lagoons'), 1));
  console.log('Perles créées         ', fmt(pick('pearls'), 1));
  console.log('Perles encaissées     ', fmt(pick('cashed'), 1));
  console.log('Plus grand lagon      ', fmt(pick('biggest'), 1));
  console.log('Meilleur combo        ', fmt(pick('bestCombo'), 1));
  console.log('% poses avec lagon    ', (100 * pick('lagoonRate').mean).toFixed(1) + ' %');
  console.log('% effacements avec perle', (100 * pick('pearlClearRate').mean).toFixed(1) + ' %');
  console.log('Part du score : lagons', (100 * pick('lagoonShare').mean).toFixed(1) + ' % | lignes', (100 * pick('lineShare').mean).toFixed(1) + ' %');
  return res;
}

const n = +(process.argv[2] || 60);
const only = process.argv[3];
const t0 = Date.now();
if (!only || only === 'classic') {
  run('Classique SANS lagons (block puzzle standard)', n, (i) => ({ seed: 1000 + i, lagoons: false }));
  [1, 2, 3].forEach((m) => run('Classique AVEC lagons, taille min ' + m, n, (i) => ({ seed: 1000 + i, minLagoon: m })));
}
if (!only || only === 'daily') {
  run('Défi du jour (45 pièces, 30 jours)', Math.min(n, 30), (i) => ({ mode: 'daily', seed: A.hashString('atoll-daily-day-' + i) }));
}
console.log(`\n(${((Date.now() - t0) / 1000).toFixed(1)} s)`);
