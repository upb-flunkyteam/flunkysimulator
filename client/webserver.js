const express = require('express');
const path = require('path');
const PORT = process.env.PORT || 5000;
const BACKEND_URL = process.env.BACKEND_URL;

let app = express();

app.set('view engine', 'pug');

app.set('views', path.join(__dirname, 'views'));

// Reroute HTTP to HTTPS
app.use((req, res, next) => {
    if (req.header('x-forwarded-proto') !== 'https')
      res.redirect(`https://${req.header('host')}${req.url}`)
    else
      next()
  })

app.get('/', (req, res) => {
    res.render('homepage');
});

app.use(express.static(path.join(__dirname, 'public')));

app.listen(PORT, () => console.log(`Listening on ${ PORT }`));
