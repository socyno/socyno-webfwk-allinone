import 'babel-polyfill'
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/index'
import tool from './utils/tools'
import enums from './utils/enums'
import ElementUI from 'element-ui'
import { Base64 } from 'js-base64'
import 'element-ui/lib/theme-chalk/index.css'
import echarts from 'echarts'
Vue.prototype.$tool = tool
Vue.prototype.$enums = enums
Vue.config.productionTip = false
Vue.prototype.$echarts = echarts

/**
 * 流程事件结果数据传递编码
 * @param {object} eventResultData 事件的响应数据
 * @return {string} 生成的数据在会话存储中的名称
 */
window.$encodeResultPageArg = function(eventResultData) {
  var data = Base64.encode(JSON.stringify(eventResultData))
  var storageUuid = tool.genUuid()
  sessionStorage.setItem(storageUuid, data)
  return 'ls:' + storageUuid
}

Vue.use(ElementUI)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
