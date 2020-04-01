module.exports = {
  entry: [
    './client/public/js/client.js'
  ],
  output: {
    path: __dirname,
    publicPath: '/client/public/js',
    filename: 'main.js'
  }
} 
