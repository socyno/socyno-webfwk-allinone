import Vue from 'vue'
import Vuex from 'vuex'
import uiControl from './modules/uicontrol'
import user from './modules/user'
Vue.use(Vuex)

const debug = process.env.NODE_ENV !== 'production'

export default new Vuex.Store({
  modules: {
    uiControl,
    user
  },
  strict: debug
})
