import request from '../utils/request'

export function logout() {
  return request({
    url: '/user/logout',
    method: 'get'
  })
}

export function login(username, password, proxied, token) {
  return request({
    url: '/user/login',
    method: 'post',
    data: {
      username,
      password,
      proxied,
      token
    }
  })
}

/**
 * 通过 sso ticket 获取用户的会话数据
 *
 * @param {String} ticket
 */
export function loginWithTicket(ssoTicket, ssoService) {
  return request({
    url: '/user/login',
    method: 'post',
    data: {
      ticket: ssoTicket,
      service: ssoService
    }
  })
}

export function getTodoListClosed(data) {
  return request({
    url: 'user/mytodo/closed',
    method: 'get',
    params: data
  })
}

export function getTodoListApplied(data) {
  return request({
    url: 'user/mytodo/applied',
    method: 'get',
    params: data
  })
}

export function getTodoList() {
  return request({
    url: 'user/mytodo/list',
    method: 'get'
  })
}

export function getTodoCount(data) {
  return request({
    url: 'user/mytodo/total',
    method: 'get'
  })
}

export function getPackageTypeList() {
  return request({
    url: 'api/application/getPackageTypeList',
    method: 'get'
  })
}

export function menuUrlFormat(path, openType, name) {
  var url = path
  var ret = url

  switch (openType) {
    case 'external': // 外链新窗口
      ret = url
      break
    case 'window': // 外链弹窗打开
      ret = url
      break
    default:
      if (/^(http|https):\/\//.test(url)) {
        ret = url
      } else {
        ret = '#' + url
      }
      break
  }
  return ret
}
