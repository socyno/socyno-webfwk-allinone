import store from '@/store'
import parser from 'url-parse'
import tool from './tools'
import { MessageBox } from 'element-ui'
/**
 *  拼装当前应用的 WebSocket 请求地址。
 */
function getCurrentWebSocketUrl(url) {
  var protocol = 'ws'
  var baseUrl = parser(store.getters['user/getApiUrl']())
  if (tool.toLower(baseUrl.protocol).indexOf('https') >= 0) {
    protocol = 'wss'
  }
  var tokenStr = store.getters['user/getToken']()
  var tokenHeaderStr = store.getters['user/getTokenHeader']()
  if (tokenStr && tokenHeaderStr) {
    url = tool.setUrlParam('__' + tokenHeaderStr, tokenStr, url)
  }
  return protocol + '://' + baseUrl.host +
        '/' + tool.remove(/^\/+/, url)
}

/**
 *  通用的 websocket 请求方法。
 *
 *  接受一个消息处理函数作为入参，该函数接收三个参数：
 *       message   ：消息
 *       websocket : WebSocket 连接
 *       isOnOpen  : 标注是否为连接创建时间触发的调用（此时message为空），便于用户发起请求。
 */

export function CommonWebSocket(url, handler) {
  if (!('WebSocket' in window)) {
    alert('当前浏览器不支持 WebSocket 特性')
    return
  }

  // 标记消息是否结束
  var mesageEnd = false

  // 消息的处理程序
  var messageHandler = handler

  // 空闲超时检测定时器
  var idleCheckTimer = null

  // 最后响应消息的时间
  var latestRepsonseTime = new Date().getTime()

  // WebSocket 对象
  var websocket = new WebSocket(getCurrentWebSocketUrl(url))

  // 连接成功建立的回调
  websocket.onopen = function() {
    if (idleCheckTimer) {
      try {
        clearInterval(idleCheckTimer)
      } catch (err) {
        // eslint-disable-next-line
        console.error(err)
      }
    }
    try {
      messageHandler('', websocket, true)
    } catch (err) {
      // eslint-disable-next-line
      console.error(err)
    }
    // 添加一个定时器，
    idleCheckTimer = setInterval(function() {
      if (new Date().getTime() - latestRepsonseTime > 60000) {
        try {
          websocket.close()
        } finally {
          clearInterval(idleCheckTimer)
        }
      }
    }, 72000)
  }

  // 接收到消息的回调方法
  websocket.onmessage = function(event) {
    try {
      mesageEnd = messageHandler(event.data, websocket, false)
    } catch (err) {
      // eslint-disable-next-line
      console.error(err)
    }
    latestRepsonseTime = new Date().getTime()
  }

  // 连接关闭的回调方法
  websocket.onclose = function(e) {
    if (idleCheckTimer) {
      try {
        clearInterval(idleCheckTimer)
      } catch (e) {
        // no console
      }
    }
    // 如果未结束进行重新连接
    if (mesageEnd === false || typeof mesageEnd === 'string') {
      MessageBox.confirm(mesageEnd || '未知错误', 'socket连接被断开,是否要重连?', {
        type: 'error'
      }).then(res => {
        CommonWebSocket(url, messageHandler)
      })
    }
  }
}
