// import router from '@/router/index.js'
// import tool from '@/utils/tools'
const state = {
  isFold: true,
  sidebars: [],
  currentMenu: {
    obj: null,
    index: null
  }
}
const getters = {
}

const actions = {
  fold({ commit }, foldFlag) {
    if (foldFlag !== undefined) {
      commit('setFoldState', foldFlag)
    } else {
      commit('setFoldState', !state.isFold)
    }
  },
  sidebar({ commit, dispatch }, data) {
    commit('setSidebarStates', data)

    if (data.index) {
      dispatch('currentMenu', data.index)
    } else {
      dispatch('currentMenu', '0-0')
    }
  },
  sidebarInit({ commit, dispatch }, data) {
    commit('setSidebarStates', data)
    commit('setCurrentMenuInit')
  },
  currentMenu({ commit }, e) {
    var indexs = e.split('-').map(Number)
    var idx1 = indexs[0]
    var select = state.sidebars[idx1]
    var subSelect = select.children[0]
    if (indexs.length >= 2 && select.children.length >= 2) {
      var idx2 = indexs[1]
      subSelect = select.children[idx2]
    }
    commit('setCurrentMenu', {
      index: e,
      obj: subSelect
    })
  }
}
const mutations = {
  setFoldState(state, newValue) {
    state.isFold = newValue
  },
  setSidebarStates(state, newValue) {
    if (newValue) {
      state.sidebars = newValue.children
    }
  },
  setCurrentMenuInit(state) {
    try {
      // eslint-disable-next-line
      $('.nav .el-submenu__title').css('border-bottom-color', 'transparent')
    } catch (error) {
      // console.error(error)
    }

    state.currentMenu = {}
  },
  setCurrentMenu(state, e) {
    state.currentMenu = e
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
