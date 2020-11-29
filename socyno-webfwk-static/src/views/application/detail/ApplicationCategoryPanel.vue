<template>
  <div class="application-category-panel">
    <el-card v-if="app">
      <div class="files-top">
        <div class="files-top-branches">
          <el-input v-model="value" style="width:300px;" readonly size="small" placeholder="请输入内容">
            <el-button slot="append" size="small" icon="el-icon-more" @click="showPickBranch = true" />
          </el-input>
        </div>
        <div class="files-top-titles">
          <span class="t1" @click="handleName">{{ app.name }}</span>
          <slot v-for="(path, pathidx) in currentPath">
            <span :key="pathidx" class="t2"> / </span>
            <span :key="path" class="t3" @click="handlePath(pathidx)">{{ path }}</span>
          </slot>
        </div>
        <el-radio-group v-model="tabPaneSelected" size="small">
          <el-radio-button label="source-codetree">
            代码管理
          </el-radio-button>
          <el-radio-button label="repo-referencces">
            分支管理
          </el-radio-button>
          <el-radio-button v-if="app.type == 'application' && app.state != 'offlined'" label="runtime-status">
            运行状态
          </el-radio-button>
          <el-radio-button v-if="app.type == 'application' && app.state != 'offlined'" label="container-configs">
            容器配置
          </el-radio-button>
          <el-radio-button v-if="app.type == 'application' && app.state != 'offlined'" label="deploy-namespaces">
            部署机组
          </el-radio-button>
          <!-- <el-radio-button label="source-changes">
            变更记录
          </el-radio-button> -->
        </el-radio-group>
      </div>
      <div class="files-container">
        <AppSourceTree v-if="tabPaneSelected === 'source-codetree'" ref="appContentPane" v-model="currentPath" :app="app" :branch="value" />
        <AppSourceChanges v-if="tabPaneSelected === 'source-changes'" :branch="value" />
        <AppRepoReferences v-if="tabPaneSelected === 'repo-referencces'" :app="app" />
        <AppRuntimeStatus v-if="tabPaneSelected === 'runtime-status'" :app="app" />
        <AppContainerConfigs v-if="tabPaneSelected === 'container-configs'" />
        <AppDeployNamespaces v-if="tabPaneSelected === 'deploy-namespaces'" />
      </div>
    </el-card>
    <el-dialog :visible.sync="showPickBranch" append-to-body>
      <AppRepoReferences select :app="app" @click="handleBranchSelected" />
    </el-dialog>
  </div>
</template>
<script>
import AppSourceTree from './AppSourceTree'
import AppSourceChanges from './AppSourceChanges'
import AppRepoReferences from './AppRepoReferences'
import AppRuntimeStatus from './AppRuntimeStatus'
import AppContainerConfigs from './AppContainerConfigs'
import AppDeployNamespaces from './AppDeployNamespaces'
import { getAllBranches } from '@/apis/application/index'
export default {
  components: {
    AppSourceTree,
    AppSourceChanges,
    AppRepoReferences,
    AppRuntimeStatus,
    AppContainerConfigs,
    AppDeployNamespaces
  },
  props: {
    app: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      options: [
      ],
      value: '',
      tabPaneSelected: 'source-codetree',
      currentPath: [],
      historyData: [],
      showPickBranch: false
    }
  },
  mounted() {
    getAllBranches(this.$route.params.app_id).then(res => {
      this.options = res.data
      this.value = res.data[0].name
    })
  },
  methods: {
    handleName() {
      this.currentPath = []
      this.$refs.appContentPane.reloadDir('', this.value)
    },
    handlePath(idx) {
      this.currentPath = this.currentPath.slice(0, idx + 1)
      this.$refs.appContentPane.reloadDir(this.currentPath.join('/'), this.value)
    },
    handleBranchSelected(branch) {
      this.currentPath = []
      this.value = branch
      this.tabPaneSelected = 'source-codetree'
      this.$forceUpdate()
      this.showPickBranch = false
    }
  }
}
</script>
<style lang="scss">
.application-category-panel {
  margin-top: 20px;
  .files-top {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .files-top-branches {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
    }
    .files-top-titles {
      margin-left: 20px;
      font-size: 14px;
      flex: 1;
      align-self: flex-start;
      padding-top: 7px;
      .t1 {
        //  color: #000;
        cursor: pointer;
      }
      .t2 {
        color: $greytext;
        padding: 0 5px;
      }
      .t3 {
        cursor: pointer;
      }
    }
    .el-radio-group {
      float : right;
    }
  }
}
</style>
