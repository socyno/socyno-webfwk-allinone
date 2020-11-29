<template>
  <div class="home fit-container">
    <div class="head">
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect">
        <el-menu-item v-for="item in tabs" :key="item.key" :index="item.key">
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </div>
    <div class="content">
      <div v-if="activeIndex==='app'">
        <IndexAppList />
      </div>
      <div v-else-if="activeIndex==='commits'">
        <AppSourceChanges />
      </div>
      <div v-else-if="activeIndex==='sys'">
        <IndexSystemList />
      </div>
      <div v-else-if="activeIndex==='todo'">
        <IndexTodoList />
      </div>
    </div>
  </div>
</template>
<script>
import IndexTodoList from './IndexTodoList'
import IndexAppList from './IndexAppList'
import IndexSystemList from './IndexSystemList'
import AppSourceChanges from '../application/detail/AppSourceChanges'
export default {
  components: {
    IndexTodoList,
    AppSourceChanges,
    IndexAppList,
    IndexSystemList
  },
  data() {
    return {
      activeIndex: 'sys',
      tabs: [
        {
          key: 'sys',
          title: '业务系统'
        },
        {
          key: 'todo',
          title: '我的待办'
        },
        {
          key: 'app',
          title: '我的应用'
        }
        // ,
        // {
        //   key: 'commits',
        //   title: '我的变更集'
        // }

      ]
    }
  },
  watch: {
    '$route': function(to, form) {
      if (to.query.tab !== this.activeIndex) {
        this.resetTab()
      }
    }
  },
  mounted() {
    this.resetTab()
  },
  methods: {
    resetTab() {
      if (this.$route.query.tab) {
        if (this.tabs.some(e => e.key === this.$route.query.tab)) {
          this.activeIndex = this.$route.query.tab
        } else {
          this.activeIndex = this.tabs[0].key
        }
      }
    },
    handleSelect(key, keyPath) {
      this.activeIndex = key.toString()

      location.href = this.updateQueryStringParameter(location.href, 'tab', key)
    },
    updateQueryStringParameter(uri, key, value) {
      var re = new RegExp('([?&])' + key + '=.*?(&|$)', 'i')
      var separator = uri.indexOf('?') !== -1 ? '&' : '?'
      if (uri.match(re)) {
        return uri.replace(re, '$1' + key + '=' + value + '$2')
      } else {
        return uri + separator + key + '=' + value
      }
    }
  }
}
</script>
<style lang="scss" >
.home .tabbar {
      padding: 20px;
      display: flex;
      justify-content: flex-start;
    }
.home {
    background: #fff;
    position: relative;
    padding-bottom: 20px;
    .head{
        position: relative;
        overflow: hidden;
        .search{
            position: absolute;
            right: 80px;
            top: 10px;
        }
    }
    .content{
        position: relative;
        .group{
            padding: 10px 30px;
            background-color: #fafafa;
            border-bottom: 1px solid #eee;
            border-top: 1px solid #eee;
            font-size: 14px;
        }
    }
}
.pagination{
    margin: 40px 0 0 20px;
}
</style>
