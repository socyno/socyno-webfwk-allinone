module.exports = {
  publicPath: process.env.NODE_ENV === 'production'
    ? '/'
    : '/',
  devServer: {
    host: 'localhost.weimob.com',
    port: 8090,
    proxy: {
      '/webfwk-executor/': {
        target: 'http://localhost.weimob.com:8080',
        ws: true
      },
      '/webfwk-backend/': {
        target: 'http://localhost.weimob.com:8080'
      },
      '/webfwk-gateway/': {
        target: 'http://localhost.weimob.com:8080'
      },
      '/webfwk-schedule/': {
        target: 'http://localhost.weimob.com:8080'
      }
    }
  },
  pluginOptions: {
    'style-resources-loader': {
      preProcessor: 'scss',
      patterns: ['./src/styles/common.scss']
    }
  },
  chainWebpack: config => {
    config.plugin('define').tap(definitions => {
      Object.assign(definitions[0]['process.env'], {
        BASE_API: '"/webfwk-gateway/"'
      })
      return definitions
    })
  }
}
