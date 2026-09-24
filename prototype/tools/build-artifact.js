// Construit une version autonome du prototype (un seul fichier, logique intégrée),
// sans les balises <html>/<head>/<body>, pour la publier comme page web.
// Usage : node prototype/tools/build-artifact.js <fichier-de-sortie.html>
'use strict';
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const html = fs.readFileSync(path.join(root, 'index.html'), 'utf8');
const core = fs.readFileSync(path.join(root, 'atoll-core.js'), 'utf8');
const out = process.argv[2];
if (!out) { console.error('Usage : node build-artifact.js <sortie.html>'); process.exit(1); }

const head = /<head>([\s\S]*?)<\/head>/i.exec(html)[1]
  .replace(/<meta[^>]*>\s*/gi, '')
  .trim();
const body = /<body>([\s\S]*?)<\/body>/i.exec(html)[1]
  .replace('<script src="atoll-core.js"></script>', () => '<script>\n' + core + '\n</script>')
  .trim();

fs.writeFileSync(out, head + '\n' + body + '\n');
console.log('Écrit : ' + out + ' (' + Math.round(fs.statSync(out).size / 1024) + ' Ko)');
