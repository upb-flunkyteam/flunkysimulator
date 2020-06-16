module.exports = {
    mode: 'production',
    entry: [
        './public/js/client.js'
    ],
    output: {
        path: __dirname + '/public/js',
        publicPath: '/js',
        filename: 'main.js'
    }
}