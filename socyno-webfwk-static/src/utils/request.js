import axios from 'axios'
import { Notification } from 'element-ui'
import store from '@/store'
import tool from './tools'

// 创建axios实例
const service = axios.create({
  withCredentials: true, // 设置axios跨域
  timeout: 120000 // 请求超时时间
})

// request 拦截器
service.interceptors.request.use((config) => {
  config.baseURL = store.getters['user/getApiUrl']()
  if (config.url.indexOf('kq_upload/') > -1) {
    config.url = config.url.replace('kq_upload/', '')
    config.headers['Content-Type'] = 'multipart/form-data'
  }

  var tokenStr = store.getters['user/getToken']()
  var tokenHeaderStr = store.getters['user/getTokenHeader']()
  if (tokenStr && tokenHeaderStr) {
    config.headers[tokenHeaderStr] = tokenStr
  }

  return config
}, error => {
  if (error.message) {
    // 登录超时
    if (error.message === 'Network Error') {
      Notification.error('网络错误，请检查你的网络')
    }
  }
  return Promise.reject(error)
})

// respone拦截器
service.interceptors.response.use((response) => {
  // 兼容老系统的接口响应数据格式
  if (!tool.isPlainObject(response.data)) {
    Notification.error('未知错误: HTTP Status Code: ' + response.status +
                      ' HTTP Status Text: ' + response.statusText)
    return Promise.reject(response.data)
  }
  if (tool.isBlank(response.data.status) && !tool.isBlank(response.data.code)) {
    response.data.status = response.data.code
    response.data.message = response.data.msg
    response.data.data = response.data
  }
  if (response.data.status === 0) {
    return Promise.resolve(response.data)
  } else if (response.data.status === 401) {
    Notification.error('未知用户或会话过期，请（重新）登录')
    store.dispatch('user/staticLogout')
  } else {
    Notification.error('访问或操作失败（' + (response.data.status || response.status) + '）- ' +
                        (response.data.message || response.statusText))
  }
  return Promise.reject(response.data)
}, (error) => {
  Notification.error(error.message || '请求失败')
  return Promise.reject(error)
})

export default service
