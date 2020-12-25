<template>
  <div class="todo-list">
    <div class="todo-list-wrap">
      <!-- <div v-for="(item, index) in tableData" :key="index" class="todo-wrap-row">
        <div class="todo-wrap-summary" @click="handleRowClick(index)">
          <div class="line">
            <span>任务名称：{{ item.title }}</span>
            <span>任务类型：MQ技术经理审批</span>
            <span>任务描述：w</span>
          </div>
          <div class="line">
            <span>上线项目：TEST-HOME-Image</span>
            <span>任务申请人：超级管理员</span>
            <span>最后提交时间：2019-12-19 14:55:42</span>
          </div>
        </div>
      </div> -->
      <el-table :data="tableData" style="width: 100%">
        <!-- <el-table-column prop="id" label="ID" width="100" /> -->
        <el-table-column prop="category" label="类型" />
        <el-table-column prop="title" label="任务名" />

        <!-- <el-table-column prop="status" label="当前状态" /> -->
        <el-table-column prop="createdAt" label="创建时间" />
        <!-- <el-table-column prop="createdBy" label="创建人" /> -->
        <el-table-column fixed="right" label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleClick(scope.row)">
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      :title="clickedRow.title || '审批'"
      append-to-body
      :visible.sync="dialogVisible"
      width="85%"
    >
      <iframe class="html-iframe" style="height:60vh;" :src="applyExternalUrl" frameborder="0" width="100%" height="60vh" @load="iframeLoaded" />
    </el-dialog>
  </div>
</template>
<script>
import { getTodoList } from '@/apis/common'
export default {
  data() {
    return {
      expandRow: -1,
      dialogVisible: false,
      tableData: [],
      applyExternalUrl: '',
      clickedRow: {}
    }
  },
  mounted() {
    getTodoList().then(res => {
      this.tableData = res.data
    })

    var that = this

    window.addEventListener('message', function(event) {
      if (event.data === 'close-approval-frame') {
        that.dialogVisible = false
        location.reload()
      }
    }, false)
  },
  methods: {
    iframeLoaded() {
      var myFrame = document.getElementsByClassName('html-iframe')[0]
      myFrame.contentWindow.closeParentLayerModel = function() {
        this.dialogVisible = false
        location.reload()
      }
    },
    handleRowClick(index) {
      this.expandRow = index
    },
    handleClick(row) {
      this.clickedRow = row
      this.applyExternalUrl = row.targetPage
      this.dialogVisible = true
    }
  }
}
</script>
<style lang="scss">
.todo-list {
  .todo-list-wrap {
    .el-table td, .el-table th {
      text-align: left !important;
      background-color: #fff !important;
    }
    .todo-wrap-row {
      background-color: #fff;
      padding: 10px;
      font-size: 14px;
      border-bottom: 1px solid #ebeef5;
      .todo-wrap-summary {
        margin-bottom: 10px;
        .line {
          display: flex;
          flex-direction: row;
          align-items: center;
          justify-content: space-between;
        }
      }
      .el-table td {
        border-bottom: none !important;
      }

    }
  }

  .html-iframe {
    width: 100%;
    height: 100%;
  }
}
</style>
