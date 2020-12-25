import docCookies from '@/utils/cookies'
import { logout } from '@/apis/common'
import router from '@/router'
import tool from '@/utils/tools'

const tokenKey = 'weimob_webfwk_token_content'
const tokenHeaderKey = 'weimob_webfwk_token_header'
const baseApiUrlKey = 'api_url'
const state = {
  info: {}
}
const getters = {
  getToken: (state) => () => {
    return localStorage.getItem(tokenKey)
  },
  getTokenHeader: (state) => () => {
    return localStorage.getItem(tokenHeaderKey)
  },
  getApiUrl: (state) => () => {
    return tool.remove(/[\s\/]+$/, sessionStorage.getItem(baseApiUrlKey)) +
                '/' + tool.remove(/(^[\s\/]+|[\s\/]+$)/, process.env.BASE_API) + '/'
  }
}

const actions = {
  logout({ dispatch }) {
    logout().then(res => {
      dispatch('staticLogout')
    }).catch(res => {
      dispatch('staticLogout')
    })
  },
  staticLogout({ commit }) {
    docCookies.clear()
    localStorage.clear()
    commit('setUserInfo', {})
    router.push('/login')
  },
  initUserInfo({ commit }) {
    var tokenStr = localStorage.getItem(tokenKey)
    if (tokenStr) {
      var jwt = require('jsonwebtoken')
      var decodedToken = jwt.decode(tokenStr)
      commit('setUserInfo', decodedToken)
    }
  },
  setUserByToken({ commit }, jmtoken) {
    var jwt = require('jsonwebtoken')
    localStorage.setItem(tokenKey, jmtoken)
    docCookies.setItem(tokenKey, jmtoken, 3 * 24 * 3600)
    var decodedToken = jwt.decode(jmtoken)
    commit('setUserInfo', decodedToken)
  },
  setTokenHeader({ commit }, tokenHeader) {
    localStorage.setItem(tokenHeaderKey, tokenHeader)
    commit('setTokenHeader', tokenHeader)
  }
}
const mutations = {
  setUserInfo(state, newValue) {
    state.info = newValue
  },
  setTokenHeader(state, newValue) {
    state.tokenHeader = newValue
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
