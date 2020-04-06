const express = require('express');
const path = require('path');
const { join } = require('path');
const { readdir } = require('fs').promises;
const PORT = process.env.PORT || 5000;

let app = express();

app.set('view engine', 'pug');

app.set('views', path.join(__dirname, 'views'));

app.get('/', (req, res) => {
    res.render('homepage');
});

async function getFiles(dir, printdir) {
  const dirents = await readdir(dir, { withFileTypes: true });
  const files = await Promise.all(dirents.map((dirent) => {
    const res = join(dir, dirent.name);
    const printres = join(printdir, dirent.name);
    return dirent.isDirectory() ? getFiles(res, printres) : dirent.name.split('.').pop() === 'webm' ? printres : null;
  }));
  return Array.prototype.concat(...files).filter(n => n);
}

app.get('/videolist', (req, res) => {
    getFiles('client/public/video/', '')
        .then(files => res.json(files))
        .catch(e => console.error(e));
});

app.use(express.static(path.join(__dirname, 'public')));

app.listen(PORT, () => console.log(`Listening on ${ PORT }`));
