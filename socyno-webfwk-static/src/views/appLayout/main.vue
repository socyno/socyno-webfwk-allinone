<template>
  <div class="container">
    <el-container>
      <el-header v-if="!inIframe()" style="z-index: 100;" class="header">
        <keep-alive>
          <Nav />
        </keep-alive>
      </el-header>
      <el-container>
        <el-aside class="aside" :width="!$store.state.uiControl.isFold ? '200px' : '0px'">
          <Sidebar />
        </el-aside>
        <el-main
          class="main"
          :style="($store.state.uiControl.isFold ? 'left:0px;' : 'left:200px; ') + (inIframe() ? 'top:0px;' : '')"
        >
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    <el-button
      class="fold-btn"
      type="text"
      :style="$store.state.uiControl.isFold ? 'left:0px' : 'left:0px;'"
      :icon="$store.state.uiControl.isFold ? 'el-icon-caret-right' : 'el-icon-caret-left'"
      @click="handleFold()"
    />
  </div>
</template>
<script>
import tool from '@/utils/tools'
import { Nav, Sidebar } from '@/components/AppLayout/'

export default {
  name: 'AppMain',
  components: {
    Sidebar,
    Nav
  },
  methods: {
    handleFold() {
      this.$store.dispatch('uiControl/fold')
    },

    inIframe() {
      return tool.inIframe()
    }
  }
}
</script>
<style lang="scss">
.container .fold-btn {
  position: fixed;
  bottom: 10%;
  left: 0;
  z-index: 1;
  color: #fff;
  font-size: 20px;
  padding: 0 5px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0;
  border-bottom-right-radius: 30px;
  border-top-right-radius: 30px;
}

.container .fold-btn:active,
.container .fold-btn:focus,
.container .fold-btn:hover {
  color: #fff;
  font-size: 20px;
  padding: 0 5px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0;
  border-bottom-right-radius: 30px;
  border-top-right-radius: 30px;
}

.container .fold-btn:hover {
  transform: scale(1.1);
}

.container .el-header {
  padding: 0;
}
.header,
.aside {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  // transition: 0.2s;
  white-space: nowrap;
}
.main {
  position: fixed;
  left: 200px;
  right: 0;
  top: 60px;
  bottom: 0;
  transition: 0.1s;
  background-color: $bgcolor;
  padding: 10px !important;

  .el-breadcrumb {
    font-size: 14px;
    line-height: 1;
    padding: 10px 20px;
    background: #fff;
    margin-bottom: 10px;
  }
}

.container::-webkit-scrollbar {
  display: none;
}
</style>
