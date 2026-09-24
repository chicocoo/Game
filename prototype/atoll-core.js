/*
 * ATOLL — logique de jeu pure (sans rendu).
 * Fonctionne dans le navigateur (window.AtollCore) et sous Node (require).
 *
 * Règles :
 *  - Grille 9x9, main de 3 pièces à poser librement (pas de rotation).
 *  - Ligne ou colonne pleine -> elle s'efface.
 *  - Règle « lagon » : toute zone de cases vides entièrement encerclée
 *    (non reliée au bord de la grille, la « mer ») se remplit de perles.
 *    Une perle compte comme une case pleine et multiplie le score de la
 *    ligne qui l'efface (x2 pour 1 perle, x3 pour 2 perles, etc.).
 */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory();
  else root.AtollCore = factory();
})(typeof self !== 'undefined' ? self : this, function () {
  'use strict';

  var SIZE = 9;
  var EMPTY = 0, BLOCK = 1, PEARL = 2;
  var DAILY_PIECES = 45;
  var COMBO_WINDOW = 3;
  var MIN_LAGOON = 2; // 1 = bonus « Plongeur » du Tour du monde

  // ---------- Hasard déterministe (identique sur tous les appareils) ----------
  function mulberry32(seed) {
    var a = seed >>> 0;
    return function () {
      a = (a + 0x6D2B79F5) >>> 0;
      var t = a;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  function hashString(s) { // FNV-1a 32 bits
    var h = 0x811c9dc5;
    for (var i = 0; i < s.length; i++) {
      h ^= s.charCodeAt(i);
      h = Math.imul(h, 0x01000193);
    }
    return h >>> 0;
  }

  // Jour du défi = jour du classement quotidien Play Games (reset à UTC-7).
  function dailyKey(date) {
    var d = new Date((date || new Date()).getTime() - 7 * 3600 * 1000);
    var y = d.getUTCFullYear(), m = d.getUTCMonth() + 1, day = d.getUTCDate();
    return y + '-' + (m < 10 ? '0' : '') + m + '-' + (day < 10 ? '0' : '') + day;
  }

  // ---------- Pièces ----------
  function normalize(cells) {
    var minR = Infinity, minC = Infinity;
    cells.forEach(function (p) { minR = Math.min(minR, p[0]); minC = Math.min(minC, p[1]); });
    var out = cells.map(function (p) { return [p[0] - minR, p[1] - minC]; });
    out.sort(function (a, b) { return a[0] - b[0] || a[1] - b[1]; });
    return out;
  }
  function rotate(cells) { return normalize(cells.map(function (p) { return [p[1], -p[0]]; })); }
  function mirror(cells) { return normalize(cells.map(function (p) { return [p[0], -p[1]]; })); }
  function keyOf(cells) { return cells.map(function (p) { return p.join(','); }).join(';'); }

  // [nom, poids total réparti entre les variantes, cellules, rotations?, miroir?]
  var BASE = [
    ['dot', 2, [[0, 0]], false, false],
    ['i2', 4, [[0, 0], [0, 1]], true, false],
    ['i3', 6, [[0, 0], [0, 1], [0, 2]], true, false],
    ['i4', 4, [[0, 0], [0, 1], [0, 2], [0, 3]], true, false],
    ['i5', 2, [[0, 0], [0, 1], [0, 2], [0, 3], [0, 4]], true, false],
    ['o2', 3, [[0, 0], [0, 1], [1, 0], [1, 1]], false, false],
    ['o3', 1, [[0, 0], [0, 1], [0, 2], [1, 0], [1, 1], [1, 2], [2, 0], [2, 1], [2, 2]], false, false],
    ['r23', 2, [[0, 0], [0, 1], [0, 2], [1, 0], [1, 1], [1, 2]], true, false],
    ['v3', 6, [[0, 0], [1, 0], [1, 1]], true, false],
    ['l4', 6, [[0, 0], [1, 0], [2, 0], [2, 1]], true, true],
    ['t4', 3, [[0, 0], [0, 1], [0, 2], [1, 1]], true, false],
    ['s4', 3, [[0, 1], [0, 2], [1, 0], [1, 1]], true, true],
    ['v5', 3, [[0, 0], [1, 0], [2, 0], [2, 1], [2, 2]], true, false]
  ];

  var PIECES = [];
  BASE.forEach(function (b) {
    var variants = {};
    var cur = normalize(b[2]);
    var forms = [cur];
    if (b[3]) { for (var i = 0; i < 3; i++) { cur = rotate(cur); forms.push(cur); } }
    if (b[4]) { forms = forms.concat(forms.map(mirror)); }
    forms.forEach(function (f) { variants[keyOf(f)] = f; });
    var list = Object.keys(variants).map(function (k) { return variants[k]; });
    list.forEach(function (cells, i) {
      var h = 0, w = 0;
      cells.forEach(function (p) { h = Math.max(h, p[0] + 1); w = Math.max(w, p[1] + 1); });
      PIECES.push({ id: b[0] + '_' + i, family: b[0], weight: b[1] / list.length, cells: cells, h: h, w: w });
    });
  });
  var TOTAL_WEIGHT = PIECES.reduce(function (s, p) { return s + p.weight; }, 0);

  function drawPiece(rng) {
    var x = rng() * TOTAL_WEIGHT;
    for (var i = 0; i < PIECES.length; i++) {
      x -= PIECES[i].weight;
      if (x < 0) return PIECES[i];
    }
    return PIECES[PIECES.length - 1];
  }

  // ---------- Grille ----------
  function canPlaceOn(grid, piece, r0, c0) {
    for (var i = 0; i < piece.cells.length; i++) {
      var r = r0 + piece.cells[i][0], c = c0 + piece.cells[i][1];
      if (r < 0 || c < 0 || r >= SIZE || c >= SIZE) return false;
      if (grid[r * SIZE + c] !== EMPTY) return false;
    }
    return true;
  }

  function pieceFits(grid, piece) {
    for (var r = 0; r <= SIZE - piece.h; r++)
      for (var c = 0; c <= SIZE - piece.w; c++)
        if (canPlaceOn(grid, piece, r, c)) return true;
    return false;
  }

  // Cases vides reliées au bord = la mer. Les autres zones vides = lagons.
  function findLagoons(grid) {
    var sea = new Uint8Array(SIZE * SIZE);
    var stack = [];
    function seed(r, c) {
      var i = r * SIZE + c;
      if (grid[i] === EMPTY && !sea[i]) { sea[i] = 1; stack.push(i); }
    }
    for (var k = 0; k < SIZE; k++) { seed(0, k); seed(SIZE - 1, k); seed(k, 0); seed(k, SIZE - 1); }
    while (stack.length) {
      var i = stack.pop(), r = (i / SIZE) | 0, c = i % SIZE;
      if (r > 0) seed(r - 1, c);
      if (r < SIZE - 1) seed(r + 1, c);
      if (c > 0) seed(r, c - 1);
      if (c < SIZE - 1) seed(r, c + 1);
    }
    var seen = new Uint8Array(SIZE * SIZE);
    var lagoons = [];
    for (var j = 0; j < SIZE * SIZE; j++) {
      if (grid[j] !== EMPTY || sea[j] || seen[j]) continue;
      var comp = [], q = [j];
      seen[j] = 1;
      while (q.length) {
        var x = q.pop(), xr = (x / SIZE) | 0, xc = x % SIZE;
        comp.push(x);
        var nb = [];
        if (xr > 0) nb.push(x - SIZE);
        if (xr < SIZE - 1) nb.push(x + SIZE);
        if (xc > 0) nb.push(x - 1);
        if (xc < SIZE - 1) nb.push(x + 1);
        for (var n = 0; n < nb.length; n++) {
          if (grid[nb[n]] === EMPTY && !seen[nb[n]]) { seen[nb[n]] = 1; q.push(nb[n]); }
        }
      }
      comp.sort(function (a, b) { return a - b; });
      lagoons.push(comp);
    }
    return lagoons;
  }

  function lagoonPoints(area) { return 20 * area + (area >= 6 ? 100 : 0); }

  function fullLines(grid) {
    var lines = [];
    for (var r = 0; r < SIZE; r++) {
      var ok = true, cells = [];
      for (var c = 0; c < SIZE; c++) { var i = r * SIZE + c; cells.push(i); if (grid[i] === EMPTY) { ok = false; break; } }
      if (ok) lines.push({ type: 'row', index: r, cells: cells });
    }
    for (var c2 = 0; c2 < SIZE; c2++) {
      var ok2 = true, cells2 = [];
      for (var r2 = 0; r2 < SIZE; r2++) { var i2 = r2 * SIZE + c2; cells2.push(i2); if (grid[i2] === EMPTY) { ok2 = false; break; } }
      if (ok2) lines.push({ type: 'col', index: c2, cells: cells2 });
    }
    return lines;
  }

  // Aperçu pendant le glisser : ce qui deviendrait perle et les lignes qui sauteraient.
  function preview(grid, piece, r0, c0, minLagoon) {
    if (!canPlaceOn(grid, piece, r0, c0)) return null;
    var g = grid.slice();
    piece.cells.forEach(function (p) { g[(r0 + p[0]) * SIZE + c0 + p[1]] = BLOCK; });
    var pearls = [];
    if (minLagoon !== 0) {
      findLagoons(g).forEach(function (comp) {
        if (comp.length < (minLagoon || MIN_LAGOON)) return;
        comp.forEach(function (i) { g[i] = PEARL; pearls.push(i); });
      });
    }
    return { pearls: pearls, lines: fullLines(g) };
  }

  // ---------- Partie ----------
  function createGame(opts) {
    opts = opts || {};
    var mode = opts.mode || 'classic';
    var seed = opts.seed != null ? opts.seed >>> 0 : (Math.random() * 4294967296) >>> 0;
    var g = {
      mode: mode,
      seed: seed,
      rng: mulberry32(seed),
      grid: new Uint8Array(SIZE * SIZE),
      hand: [null, null, null],
      score: 0,
      combo: 0,
      sinceClear: COMBO_WINDOW,
      bestCombo: 0,
      piecesPlaced: 0,
      linesCleared: 0,
      lagoons: 0,
      pearlsCreated: 0,
      pearlsCashed: 0,
      biggestLagoon: 0,
      revivesLeft: mode === 'classic' ? 1 : 0,
      lagoonsEnabled: opts.lagoons !== false,
      minLagoon: opts.minLagoon || MIN_LAGOON,
      over: false,
      sequence: null,
      cursor: 0
    };
    if (mode === 'daily') {
      // Même suite de pièces pour tout le monde, indépendante des coups joués.
      g.sequence = [];
      for (var i = 0; i < (opts.pieces || DAILY_PIECES); i++) g.sequence.push(drawPiece(g.rng));
    }
    dealHand(g);
    return g;
  }

  function piecesLeft(g) {
    var inHand = g.hand.filter(Boolean).length;
    return g.sequence ? inHand + (g.sequence.length - g.cursor) : Infinity;
  }

  function dealHand(g) {
    if (g.sequence) {
      for (var k = 0; k < 3; k++) g.hand[k] = g.cursor < g.sequence.length ? g.sequence[g.cursor++] : null;
      return;
    }
    // Mode classique : on retire une main entière injouable (jusqu'à 8 essais).
    for (var attempt = 0; attempt < 8; attempt++) {
      var hand = [drawPiece(g.rng), drawPiece(g.rng), drawPiece(g.rng)];
      var playable = hand.some(function (p) { return pieceFits(g.grid, p); });
      g.hand = hand;
      if (playable) return;
    }
  }

  function hasMove(g) {
    for (var k = 0; k < 3; k++) if (g.hand[k] && pieceFits(g.grid, g.hand[k])) return true;
    return false;
  }

  function place(g, handIndex, r0, c0) {
    var piece = g.hand[handIndex];
    if (g.over || !piece || !canPlaceOn(g.grid, piece, r0, c0)) return null;
    var ev = { placed: [], lagoons: [], lines: [], cleared: [], points: { place: 0, lagoon: 0, lines: 0 },
      multi: 0, combo: 0, total: 0, over: false };

    piece.cells.forEach(function (p) {
      var i = (r0 + p[0]) * SIZE + c0 + p[1];
      g.grid[i] = BLOCK;
      ev.placed.push(i);
    });
    ev.points.place = piece.cells.length;

    if (g.lagoonsEnabled) {
      findLagoons(g.grid).forEach(function (comp) {
        if (comp.length < g.minLagoon) return;
        comp.forEach(function (i) { g.grid[i] = PEARL; });
        var pts = lagoonPoints(comp.length);
        ev.lagoons.push({ cells: comp, points: pts });
        ev.points.lagoon += pts;
        g.lagoons++;
        g.pearlsCreated += comp.length;
        g.biggestLagoon = Math.max(g.biggestLagoon, comp.length);
      });
    }

    var lines = fullLines(g.grid);
    if (lines.length) {
      g.combo = (g.combo > 0 && g.sinceClear < COMBO_WINDOW) ? g.combo + 1 : 1;
      g.sinceClear = 0;
      g.bestCombo = Math.max(g.bestCombo, g.combo);
      var base = 0, toClear = {};
      lines.forEach(function (L) {
        var pearls = 0;
        L.cells.forEach(function (i) { if (g.grid[i] === PEARL) pearls++; toClear[i] = true; });
        L.pearls = pearls;
        L.points = 10 * SIZE * (1 + pearls);
        base += L.points;
        ev.lines.push(L);
      });
      ev.multi = lines.length;
      ev.combo = g.combo;
      ev.points.lines = Math.round(base * lines.length * (1 + 0.5 * (g.combo - 1)));
      Object.keys(toClear).forEach(function (key) {
        var i = +key;
        if (g.grid[i] === PEARL) g.pearlsCashed++;
        ev.cleared.push({ index: i, kind: g.grid[i] });
        g.grid[i] = EMPTY;
      });
      g.linesCleared += lines.length;
    } else {
      g.sinceClear++;
      if (g.sinceClear >= COMBO_WINDOW) g.combo = 0;
    }

    ev.total = ev.points.place + ev.points.lagoon + ev.points.lines;
    g.score += ev.total;
    g.hand[handIndex] = null;
    g.piecesPlaced++;

    if (!g.hand.some(Boolean)) dealHand(g);
    g.over = !g.hand.some(Boolean) || !hasMove(g);
    ev.over = g.over;
    return ev;
  }

  // « Continuer » (en vrai : pub récompensée) — nouvelle main garantie jouable.
  function revive(g) {
    if (!g.over || g.revivesLeft <= 0 || g.sequence) return false;
    var fitting = PIECES.filter(function (p) { return pieceFits(g.grid, p); });
    if (!fitting.length) return false;
    fitting.sort(function (a, b) { return a.cells.length - b.cells.length; });
    var pool = fitting.slice(0, Math.max(3, Math.ceil(fitting.length / 2)));
    for (var k = 0; k < 3; k++) g.hand[k] = pool[(g.rng() * pool.length) | 0];
    g.revivesLeft--;
    g.over = false;
    return true;
  }

  return {
    SIZE: SIZE, EMPTY: EMPTY, BLOCK: BLOCK, PEARL: PEARL, DAILY_PIECES: DAILY_PIECES, MIN_LAGOON: MIN_LAGOON,
    PIECES: PIECES,
    mulberry32: mulberry32, hashString: hashString, dailyKey: dailyKey,
    canPlaceOn: canPlaceOn, pieceFits: pieceFits, findLagoons: findLagoons, fullLines: fullLines,
    lagoonPoints: lagoonPoints, preview: preview,
    createGame: createGame, place: place, hasMove: hasMove, revive: revive, piecesLeft: piecesLeft
  };
});
