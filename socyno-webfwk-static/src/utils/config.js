module.exports = {
  oldLoginPath: process.env.NODE_ENV === 'production'
    ? 'http://webfwk.socyno.org'
    : 'http://localhost.socyno.org'
}
