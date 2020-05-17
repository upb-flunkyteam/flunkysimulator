module.exports = {
    mode: 'development',
    entry: [
        './client/public/js/client.js'
    ],
    output: {
        path: __dirname + '/public/js',
        publicPath: '/js',
        filename: 'main.js'
    }
}