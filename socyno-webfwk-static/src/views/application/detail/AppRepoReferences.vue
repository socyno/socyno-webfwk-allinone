<template>
  <div class="app-branch-list">
    <el-tabs v-model="activeName" type="card" @tab-click="handleTabChange">
      <el-tab-pane :label="tabs.branch" name="branch" />
      <el-tab-pane :label="tabs.patch" name="patch" />
      <el-tab-pane :label="tabs.tag" name="tag" />
    </el-tabs>
    <div style="display:flex; justify-content: flex-end; padding-right: 29px;padding-bottom:8px;">
      <el-button v-if="!select" type="primary" size="mini" @click="handleAdd">
        新增{{ tabs[activeName] }}
      </el-button>
    </div>
    <div v-loading="loading" class="branch-items">
      <div v-for="(item, index) in options" :key="index" class="branch-row">
        <div>
          <span class="branch-title common-code">{{ item.name }}</span>
        </div>
        <div>
          <el-button v-if="!select" type="danger" size="small" icon="el-icon-delete" @click="handleDelete(item.name)" />
          <el-button v-if="select" type="primarys" size="small" @click="handleSelect(item.name)">
            选择
          </el-button>
        </div>
      </div>
      <div v-if="!options.length" style="padding-top: 30px;" class="common-nodata">
        暂无数据
      </div>
    </div>
    <br>
    <el-button-group v-model="currentPageFlag">
      <el-button :disabled="noPrev" type="normal" size="small" icon="el-icon-arrow-left" @click="pageChange('prev')">
        上一页
      </el-button>
      <el-button :disabled="!options.length" type="normal" size="small" @click="pageChange('next')">
        下一页<i class="el-icon-arrow-right el-icon--right" />
      </el-button>
      <el-button type="text" size="small" style="margin-left:10px; font-size:14px; color:#666;">
        当前页: {{ page.page }}
      </el-button>
    </el-button-group>
    <el-dialog
      :visible.sync="branchAddDialogVisable"
      :title="'新增' + tabs[activeName]"
      width="60%"
      append-to-body
    >
      <el-form size="medium" :model="branchForm">
        <el-form-item label="选择源分支:">
          <strong>{{ branchForm.branch }}</strong>
          <br>
          <BranchSelect v-if="!select" select :app="app" @click="handleSelected" />
        </el-form-item>
        <el-form-item label="新建分支名称:">
          <el-input v-model="branchForm.name" placeholder="请输入名称" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="branchAddDialogVisable = false">取 消</el-button>
        <el-button type="primary" @click="handleAddBranch">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { getAllBranches, getAllTags, getAllPatches, removeBranch, createBranch } from '@/apis/application/index'
export default {
  components: {

    BranchSelect: () => import('./AppRepoReferences.vue')
  },
  props: {
    select: {
      type: Boolean,
      default: false
    },
    app: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      currentPageFlag: '',
      branchForm: {
        addedBranchName: '',
        selectedBranch: ''
      },
      branchAddDialogVisable: false,
      activeName: 'branch',
      options: [],
      page: {
        page: 1,
        limit: 10
      },
      total: 0,
      loading: false,
      tabs: {
        'branch': '分支',
        'tag': '标签',
        'patch': '补丁'
      },
      noPrev: true,
      noNext: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    pageChange(e) {
      var p = this.page.page
      if (e === 'prev') {
        p = p === 1 ? p : p - 1
      } else {
        p = p + 1
      }
      // console.log(p)
      if (p > 1) {
        this.noPrev = false
      } else if (p === 1) {
        this.noPrev = true
      }
      this.page = {
        page: p,
        limit: 10
      }
      this.loadData()
    },
    loadData() {
      this.loading = true
      switch (this.activeName) {
        case 'branch':
          getAllBranches(this.$route.params.app_id, this.page).then(res => {
            this.options = res.data
            // this.total = res.data.length
            this.loading = false
          })
          break

        case 'patch':

          getAllPatches(this.$route.params.app_id, this.page).then(res => {
            this.options = res.data
            // this.total = res.data.total
            this.loading = false
          })
          break

        case 'tag':
          getAllTags(this.$route.params.app_id, this.page).then(res => {
            this.options = res.data
            // this.total = res.data.total
            this.loading = false
          })
          break
      }
    },
    handleTabChange() {
      this.page = {
        page: 1,
        limit: 10
      }
      this.loadData()
    },
    handleAdd() {
      this.branchAddDialogVisable = true
    },
    handleSelected(e) {
      this.branchForm.branch = e
      this.$forceUpdate()
    },
    handleAddBranch() {
      createBranch(this.activeName, this.$route.params.app_id, { name: this.branchForm.name, refOrCommit: this.branchForm.branch }).then(res => {
        this.branchAddDialogVisable = false
      })
    },
    handleDelete(branchName) {
      this.$confirm('此操作将删除该分支, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeBranch(this.$route.params.app_id, branchName).then(res => {
          this.loadData()
          this.$message({
            type: 'success',
            message: '删除成功'
          })
        })
      }).catch(() => {

      })
    },
    handleSelect(branchName) {
      this.$emit('click', branchName)
    }
  }
}
</script>
<style lang="scss">
    .app-branch-list {
        .branch-items {
            .branch-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 1px $bordercolor solid;
                padding: 10px 28px;

            }
        }
    }
</style>
