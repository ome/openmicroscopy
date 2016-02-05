// webpack.config.js

var path = require('path');

module.exports = {
  entry: './webclient/static/webclient/react/main.jsx',
  output: {
    path: './webclient/static/webclient/js',
    filename: 'bundle.js',
    library: 'renderCentrePanel'
  },
  module: {
    loaders: [
      {
        test: /\.jsx$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
        query: {
          presets: ['react', 'es2015']
        }
      },
      {
        test: /\.css$/, // Only .css files
        loader: 'style-loader!css-loader' // Run both loaders
      },
      { test: /\.png$/,
        loader: "url-loader?limit=100000"
      }
    ]
  },
  resolve: {
    extensions: ['', '.js', '.jsx', '.json']
  },
};