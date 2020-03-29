const express = require('express')
const path = require('path')
const PORT = process.env.PORT || 5000

let app = express()

app.set('view engine', 'pug')

app.set('views', path.join(__dirname, 'views'))

app.get('/', (req, res) => {
    res.render('homepage');
});

app.use(express.static(path.join(__dirname, 'public')))

app.listen(PORT, () => console.log(`Listening on ${ PORT }`))
