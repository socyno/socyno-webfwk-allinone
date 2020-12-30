<template>
  <div class="nav">
    <el-menu
      background-color="#3C8DBC"
      text-color="#FFFFFF"
      active-text-color="#FFFFFF"
      class="el-menu-demo"
      mode="horizontal"
      :collapse-transition="false"
      @select="handleSelect"
    >
      <div class="menu">
        <div class="logo-top" @click="handleLogo">
          <img src="@/assets/logo.png" alt>
        </div>
      </div>
      <slot v-for="(item, idx) in navs">
        <!-- level 1 -->
        <el-menu-item
          v-if="!item.children"
          :ref="'submenu-'+idx"
          :index="idx + ''"
        >
          {{ item.name }}
        </el-menu-item>
        <el-submenu v-if="item.children" :ref="'submenu-'+idx" :index="idx + ''">
          <template slot="title">
            <!-- <span @click="handleSelect(idx + '')">{{ item.name }}</span> -->
            <div class="sub-btn">
              {{ item.name }}
            </div>
          </template>
          <!-- level 2 -->
          <slot v-for="(item2, idx2) in item.children">
            <slot v-if="!item2.children.length">
              <a
                v-if="item2.openType !== 'window'"
                :key="idx + '-' + idx2"
                :href="parseUrl(item2)"
                :target="item2.openType === 'external' ? '_blank' : '_self'"
              >
                <el-menu-item :index="idx + '-' + idx2">
                  {{ item2.name }}
                </el-menu-item>
              </a>
              <el-menu-item v-else :index="idx + '-' + idx2">
                <span @click="handleNavMenuItem(item2)"> {{ item2.name }} </span>
              </el-menu-item>
            </slot>
            <!-- level 2 end -->
            <el-submenu v-if="item2.children && item2.children.length" :ref="'submenu-'+idx" :hide-timeout="0" :index="idx + '-' + idx2" popper-append-to-body>
              <template slot="title">
                <div class="sub-btn">
                  {{ item2.name }}
                </div>
              </template>
              <!-- level 3 -->
              <slot v-for="(item3, idx3) in item2.children">
                <a
                  v-if="item3.openType !== 'window'"
                  :key="idx + '-' + idx2 + '-' + idx3"
                  :href="parseUrl(item3)"
                  :target="item3.openType === 'external' ? '_blank' : '_self'"
                >
                  <el-menu-item :index="idx + '-' + idx2 + '-' + idx3">{{ item3.name }}  </el-menu-item>
                </a>
                <el-menu-item v-else :index="idx + '-' + idx2 + '-' + idx3">
                  <span @click="handleNavMenuItem(item3)"> {{ item3.name }} </span>
                </el-menu-item>
              </slot>
            </el-submenu>
          </slot>
        </el-submenu>
      </slot>
      <div class="right-panel">
        <div class="btns">
          <el-button
            class="menu-btn btn searchbutton"
            type="text"
            icon="el-icon-search"
            @click="handleSearchBtn"
          />
          <el-autocomplete
            ref="searchbar"
            v-model="inputValue"
            :value="inputValue"
            :style="isSearch ? 'margin-left: 5px;width: 200px;' : 'width: 0px;'"
            :fetch-suggestions="querySearch"
            :trigger-on-focus="true"
            :debounce="0"
            class="searchbar"
            size="mini"
            placeholder="搜索菜单"
            value-key="name"
            name="searchbar"
            @blur="handleSearchbarBlur"
            @select="handleSearchSelect"
          >
            <template slot-scope="{ item }">
              <div class="name">
                <a :href="parseUrl(item)">{{ item.name }}</a>
              </div>
            </template>
          </el-autocomplete>

          <el-button class="menu-btn btn" type="text" icon="el-icon-s-home" @click="handleHome" />

          <!-- <router-link to="/user/setting">
            <el-button class="menu-btn btn" type="text" icon="el-icon-s-tools" @click="$store.commit('uiControl/setCurrentMenuInit')" />
          </router-link> -->

          <el-badge :value="num" :hidden="!num" class="nav-bell">
            <el-button
              class="menu-btn btn"
              type="text"
              icon="el-icon-message-solid"
              @click="handleBell"
            />
          </el-badge>
        </div>
        <div class="profile">
          <el-dropdown class="item" @command="handleSetting">
            <el-button class="menu-btn btn" type="text" icon="el-icon-user-solid" />
            <el-dropdown-menu slot="dropdown">
              <div class="info">
                <div class="name">
                  {{ $store.state.user.info.display }}
                </div>
                <div class="role">
                  {{ $store.state.user.info.username }}
                </div>
                <div v-if="$store.state.user.info.proxyDisplay" class="role">
                  <br>
                  <strong>
                    正使用代理模式登录
                  </strong>
                  <br>
                  代理人姓名:{{ $store.state.user.info.proxyDisplay }}
                </div>
                <div v-if="$store.state.user.info.proxyDisplay" class="role">
                  代理人账号:{{ $store.state.user.info.proxyUsername }}
                </div>
              </div>
              <el-dropdown-item v-if="$store.state.user.info.isAdmin && !$store.state.user.info.proxyDisplay" command="proxy">
                代理用户
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </el-menu>
    <el-dialog
      title="代理登录"
      :visible.sync="proxyDialogVisble"
      width="30%"
      append-to-body
    >
      <el-form :model="proxyform">
        <el-form-item label="登录密码">
          <el-input v-model="proxyform.pwd" type="password" placeholder="请输入当前账号的登录密码" />
        </el-form-item>
        <el-form-item label="代理用户名">
          <el-input v-model="proxyform.proxyId" type="text" placeholder="请输入被代理用户名" />
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="proxyDialogVisble = false">取 消</el-button>
        <el-button type="primary" @click="handleSwitchProxyUser">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { getMenuList } from '@/apis/systemSettings/menu'
import { getTodoCount, login, menuUrlFormat } from '@/apis/common'
import Fuse from 'fuse.js'

export default {
  data() {
    return {
      navs: [],
      num: 0,
      currentSelect: '0',
      prevSelectSub: null,
      timer: -1,
      initSidebarIdx: null,
      inputValue: '',
      isSearch: false,
      proxyform: {},
      proxyDialogVisble: false
    }
  },
  created() {
    getMenuList().then(res => {
      this.navs = res.data.children
      this.$nextTick(() => {
        var ret = this.findIdxByName()
        if (!ret) {
          this.$store.dispatch('uiControl/sidebarInit', this.navs[0])
        } else {
          const navItem = localStorage.getItem('nav-item')
          if (navItem) {
            this.currentSelect = navItem
            var sidebar = this.navs[Number(navItem[0])]
            sidebar.index = this.initSidebarIdx
            this.$store.dispatch('uiControl/sidebar', sidebar)
          }
        }
        this.initSearchBar(res)
      })
    })

    var getNotification = () => {
      getTodoCount().then(res => {
        this.num = res.data
      })
    }
    getNotification()
    var timer = setInterval(() => {
      getNotification()
    }, 60000 * 3)
    this.timer = timer
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    parseUrl(e) {
      return menuUrlFormat(e.path, e.openType, e.name)
    },
    handleBell(e) {
      this.$router.push({
        path: '/dashboard?tab=todo'
      })
      this.$store.commit('uiControl/setCurrentMenuInit')
    },
    initSearchBar(res) {
      // init search engine
      var options = {
        keys: ['name', 'pyNames'],
        shouldSort: true,
        threshold: 0.6,
        location: 0,
        distance: 100,
        maxPatternLength: 32,
        minMatchCharLength: 1
      }
      var fuseList = []
      var pinyin = require('pinyin')
      for (const i in res.data.children) {
        for (const n in res.data.children[i].children) {
          for (const x in res.data.children[i].children[n].children) {
            var item = res.data.children[i].children[n].children[x]
            item.index = i + '-' + n + '-' + x
            var pyNames = pinyin(item.name, {
              style: pinyin.STYLE_NORMAL,
              segment: false
            })
            pyNames = [].concat.apply([], pyNames)
            item.pyNames = pyNames.join()
            fuseList.push(item)
          }
        }
      }
      var fuse = new Fuse(fuseList, options)
      this.fuse = fuse

      var that = this
      document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
          if (that.$refs.searchbar !== undefined) {
            that.$refs.searchbar.handleClear()
          }

          // console.log('Browser tab is hidden')
        } else {
          // console.log('Browser tab is visible')
        }
      })
    },
    findIdxByName() {
      var navIdx = null
      var sidebarIdx = null
      for (const i in this.navs) {
        const navItem = this.navs[i]
        for (const n in navItem.children) {
          const sidebarDir = navItem.children[n]
          for (const x in sidebarDir.children) {
            const sidebarItem = sidebarDir.children[x]

            if (sidebarItem.path === this.$route.path) {
              navIdx = i + (navItem.systemMenuLinks && navItem.systemMenuLinks.length ? '-0' : '0')
              sidebarIdx = n + '-' + x
              break
            }
          }
        }
      }

      this.initSidebarIdx = sidebarIdx
      localStorage.setItem('nav-item', navIdx)

      if (navIdx !== null) {
        return true
      } else {
        return false
      }
    },
    handleNavMenuItem(e) {
      // window 暂时只有Window中打开一种选项

      window.$openIframeDialog({
        'title': e.name,
        'src': e.path
      })
    },
    handleLogo(e) {
      localStorage.setItem('nav-item', '0-0')
      this.$store.commit('uiControl/setCurrentMenuInit')
      if (this.$route.name === 'Dashboard') return
      this.$router.replace({ name: 'Dashboard' })
    },
    handleSelect(e) {
      // 0 为 点击了第0个NavItem
      // 0-1 为 点击了第0个NavItem的第1个快捷按钮

      const navItem = localStorage.getItem('nav-item')
      if (e.length === 1 && this.currentSelect[0] === e && navItem !== 'null') {
        return
      } // 如果上一个选中的和这个相同则不做选中

      var prevE = this.$refs['submenu-' + this.currentSelect[0]][0].$el
      if (prevE.children.length) {
        prevE.children[0].style['border-bottom-color'] = 'transparent'
      } else {
        prevE.style['border-bottom-color'] = 'transparent'
      }

      var thisE = this.$refs['submenu-' + e[0]][0].$el
      if (thisE.children.length) {
        thisE.children[0].style['border-bottom-color'] = '#fff'
      } else {
        thisE.style['border-bottom-color'] = '#fff'
      }
      localStorage.setItem('nav-item', e.length === 1 ? e + '-0' : e)
      var indexs = e.split('-').map(Number)
      var idx1 = indexs[0]

      var sidebar = this.navs[idx1]

      if (indexs.length === 3) {
        // 点击了快捷菜单
        var sidebarIdx1 = indexs[1]
        var sidebarIdx2 = indexs[2]
        // var selectedShortcut = sidebar.children[idx2].children[idx3]
        sidebar.index = sidebarIdx1 + '-' + sidebarIdx2
        this.$store.dispatch('uiControl/sidebar', sidebar)
      }
    },
    handleSetting(e) {
      switch (e) {
        case 'logout':
          this.$store.dispatch('user/logout')
          break
        case 'proxy':
          this.proxyDialogVisble = true
          break
      }
    },
    handleSwitchProxyUser() {
      login(this.$store.state.user.info.username, this.proxyform.pwd, this.proxyform.proxyId).then(res => {
        this.$store.dispatch('user/setUserByToken', res.data.tokenContent)
        this.$store.dispatch('user/setTokenHeader', res.data.tokenHeader)
        this.proxyDialogVisble = false
        this.$message({
          type: 'success',
          message: '登录成功'
        })
        window.location.reload()
      })
    },
    handleHome(e) {
      this.$router.replace({ name: 'Dashboard' })
      this.$store.commit('uiControl/setCurrentMenuInit')
    },
    querySearch(queryString, cb) {
      var results = []
      if (this.fuse) {
        results = this.fuse.search(queryString)
      }
      cb(results)
    },
    handleSearchSelect(e) {
      this.inputValue = ''
      this.handleSelect(e.index)
    },
    handleSearchBtn() {
      this.isSearch = true
      this.$refs.searchbar.focus()
    },
    handleSearchbarBlur(e) {
      this.isSearch = false
      this.inputValue = ''
      if (e.sourceCapabilities === null) {
        this.$refs.searchbar.handleClear()
      }
    }
  }
}
</script>
<style lang="scss">
.el-menu--horizontal .el-submenu__icon-arrow.el-icon-arrow-right {
  color: #ffffff;
}
.nav {
  overflow: hidden;

  .el-menu--horizontal > .el-submenu .el-submenu__title,
  .el-menu--horizontal > .el-menu-item {
    height: 50px;
    line-height: 50px;
    margin: 0 10px;
    padding: 0 10px;
    margin-top: 2.5px;
  }

  .el-menu--horizontal > .el-submenu .el-submenu__icon-arrow {
    position: relative;
    vertical-align: middle;
    margin-left: 0;
    margin-top: 0;
    bottom: 51px;
    left: 60px;
    right: 0;
    top: unset;
  }
  .sub-btn {
    padding-right: 10px;
    position: relative;
    right: 5px;
  }

  .logo-top {
    height: 60px;
    line-height: 60px;
    background-color: unset;
    display: flex;
    padding-left: 25px;
    margin-right: 10px;
    align-items: center;
    cursor: pointer;
    // width: 185px;

    img {
      width: 53px;
      height: auto;
    }
    .title {
      color: #fff;
      font-size: 14px;
      line-height: unset;
      height: 60px;
      text-align: center;
      padding-bottom: 20px;
      margin-left: 10px;

      div {
        height: 20px;
      }
    }
  }
  .menu {
    float: left;
    height: 60px;
    line-height: 60px;
    font-size: 22px;
  }

  .menu-btn *:before {
    color: #ffffff;
    font-size: 22px;
  }

  .right-panel {
    color: #ffffff;
    height: 60px;
    line-height: 60px;
    vertical-align: middle;
    float: right;
    margin-right: 20px;
  }

  .right-panel .btns {
    float: left;
    .el-badge__content.is-fixed {
      top: 11px;
      right: 11px;
      border: none;
    }
    .btn {
      margin-left: 20px;
    }

    .searchbutton {
      position: relative;
      top: 1px;
    }
    .searchbar {
      position: relative;
      bottom: 5px;
      transition: 0.15s;

      .el-input__inner {
        background: none;
        border: none;
        border-radius: 0;
        padding: 0;
        border-bottom: 1px solid #fff;
        color: #fff;
        font-size: 14px;
      }
    }
  }

  .el-icon-arrow-down:before {
    color: #ffffff;
  }

  .profile {
    float: right;
    margin-left: 15px;
    overflow: hidden;
  }

}
.info {
      font-size: 14px;
      border-bottom: 1px #f6f6f6 solid;
      padding: 0 10px 10px 10px;
      margin-bottom: 10px;
       .name {
         font-size: 15px;
         font-weight: bold;
       }
       .role {
         color: #949494;
       }
    }
</style>
