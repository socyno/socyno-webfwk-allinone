module.exports = {
  oldLoginPath: process.env.NODE_ENV === 'production'
    ? 'http://devops.weimob.com'
    : 'http://localhost.weimob.com'
}
