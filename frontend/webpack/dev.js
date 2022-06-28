// development config
const merge = require('webpack-merge');
const commonConfig = require('./common');
const {resolve} = require('path');

module.exports = merge.merge(commonConfig, {
  mode: 'development',
  entry: [
    'react-hot-loader/patch',
    'webpack/hot/only-dev-server',
    './index.tsx'
  ],
  output: {
    filename: 'bundle.[fullhash].js',
    path: resolve(__dirname, '../build/dist-dev/'),
  },
  devServer: {
    hot: true,
    port: process.env.FRONTEND_DEV_PORT,
    historyApiFallback: true,
  },
  devtool: 'eval-cheap-module-source-map',
  plugins: [],
  optimization: {
    moduleIds: 'named',
  }
});
