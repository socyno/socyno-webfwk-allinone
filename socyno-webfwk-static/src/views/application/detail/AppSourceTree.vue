<template>
  <div class="application-source-tree">
    <el-table v-loading="loading" :data="tableData" style="width: 100%">
      <el-table-column show-overflow-tooltip prop="name" label="Name" width="300">
        <template slot-scope="scope">
          <div class="file-col">
            <i v-if="scope.row.isDir" class="el-icon-folder" />
            <i v-else class="el-icon-tickets" />
            <span style="margin-left: 10px" @click="handleItem(scope.$index, scope.row)">{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="提交备注">
        <template slot-scope="scope">
          <el-tooltip effect="dark" placement="top">
            <!-- eslint-disable -->
            <div slot="content" v-html="$tool.htmlLineBreak($tool.escape(scope.row.message))" />
            <span class="overflow-ellipsis">{{ scope.row.message }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column show-overflow-tooltip prop="lastUpdated" label="提交时间" width="200px" />
      <el-table-column show-overflow-tooltip prop="commiter" label="提交人" width="250px" />
    </el-table>
    <el-dialog
      :title="currentFile"
      append-to-body
      :visible.sync="dialogVisible"
      @opened="handleOpenDialog"
    >
      <pre class="pre">
        <code id="CodeContainer">
        {{ codeStr }}
      </code>
      </pre>
    </el-dialog>
  </div>
</template>
<script>

import { getDirs, getFileContent } from '@/apis/application/index'
export default {
  props: {
    app: {
      type: Object,
      required: true
    },
    branch: {
      type: String,
      required: false,
      default: 'master'
    }
  },
  data() {
    return {
      tableData: [
      ],
      path: '',
      loading: false,
      dialogVisible: false,
      codeStr: '',
      currentFile: ''
    }
  },
  watch: {
    branch: {
      immediate: true,
      handler: function(newv) {
        if (newv && this.app.id) {
          this.reloadDir(this.path, newv)
        }
      }
    }
  },
  methods: {
    reloadDir(path, branch) {
      getDirs(this.app.id, path, branch).then(res => {
        this.tableData = res.data
        this.$emit('input', path.length ? path.split('/') : [])
      }).finally(() => {
        this.loading = false
      })
    },
    handleItem(idx, row) {
      this.loading = true
      if (row.isDir) {
        this.reloadDir(row.path, this.branch)
      } else {
        getFileContent(this.app.id, row.path, this.branch).then(res => {
          var str = ''
          try {
            const bytesView = new Uint8Array(res.data)
            str = new TextDecoder().decode(bytesView)
          } catch (error) {
            // eslint-disable-next-line
            console.error(error)
            this.$notify.info('该文件无法预览.')
          }
          if (str.length) {
            this.dialogVisible = true
            this.currentFile = row.path
            this.codeStr = str
          }
        }).finally(() => {
          this.loading = false
        })
      }
    },
    handleOpenDialog() {
      // nothing
    }
  }
}
</script>
<style lang="scss">
.pre {
    background: #eee;
    font-family: "Courier 10 Pitch", Courier, monospace;
    font-size: 15px;
    line-height: 1.6;
    max-width: 100%;
    overflow: auto;
    padding: 10px;
}
.application-source-tree {
  .el-table {
    width: 100%;
    border-radius: 3px;
    border: 1px solid #EBEEF5;
    border-bottom: 0;
    .cell {
        font-weight: normal;
    }

    .file-col {
      cursor: pointer;
      text-align: left;
      padding-left: 5px;
    }
  }
  .el-table th,
  .el-table tr th {
    background-color: #fafafa !important;
  }

}
</style>
