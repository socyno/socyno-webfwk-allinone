<template>
  <div>
    <div class="tabbar">
      <el-radio-group v-model="selectedTab" size="small" @change="handlePageChange">
        <el-radio-button label="todo">
          待处理的代办
        </el-radio-button>
        <el-radio-button label="closed">
          我处理的待办
        </el-radio-button>
        <el-radio-button label="applied">
          我发起的待办
        </el-radio-button>
      </el-radio-group>
    </div>
    <div v-loading="loading" class="index-todo-list">
      <BaseInfoRow
        v-for="(item, index) in items"
        :key="index"
        :title="item.title"
        :detail="getTodoResult(item)"
        :time="item.createdAt"
        :subtitle="getTodoSubTitle(item)"
        @click="showTodoTargetPage(item)"
      />
      <div v-if="!items.length" class="common-nodata" style="padding-bottom:10%;">
        暂无数据
      </div>
    </div>
    <BasePagination
      v-show="selectedTab != 'todo'"
      v-model="page"
      :options="page"
      :total="total"
      class="pagination"
      @change="loadData"
    />
  </div>
</template>
<style>

</style>
<script>
import tool from '@/utils/tools.js'
import BaseInfoRow from '@/components/BaseInfoRow'
import { getTodoList, getTodoListClosed, getTodoListApplied } from '@/apis/common'
import BasePagination from '@/components/BasePagination'

export default {
  components: {
    BaseInfoRow,
    BasePagination
  },
  data() {
    return {
      items: [],
      selectedTab: 'todo',
      total: 0,
      page: {
        limit: 10,
        page: 1
      },
      loading: false,
      layerDologIndex: ''
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('message', this.eventMessage)
  },
  destroyed() {
    window.removeEventListener('message', this.eventMessage)
  },
  methods: {
    /**
     * 代办内外交互处理事件
     * @param {Object} event
     */
    eventMessage(event) {
      if (event.data === 'close-window' ||
            event.data === 'close-approval-frame') {
        this.loadData()
        if (this.layerDologIndex) {
          window.layui.layer.close(this.layerDologIndex)
        }
      }
    },

    /**
     * 拼装待办的副标题（包括发起人，审批人等）
     * @param {Object} todo
     */
    getTodoSubTitle(todo) {
      var subtitle = '审批人包括：'
      if (todo && tool.isArray(todo.assignees) && todo.assignees.length > 0) {
        for (var a = 0; a < todo.assignees.length; a++) {
          var assignee = todo.assignees[a]
          if (assignee && tool.isNotBlank(assignee.display)) {
            subtitle += assignee.display + ', '
          }
        }
      }
      return tool.remove(/,\s*$/, subtitle)
    },

    /**
     * 拼装待办的状态信息（包括状态，发起人，处理结果等）
     * @param {Object} todo
     */
    getTodoResult(todo) {
      var status = ''
      if (todo && tool.isNotBlank(todo.state)) {
        status += '状态：' + tool.trim(todo.state) + ', '
      }
      if (todo && tool.isNotBlank(todo.applyUserDisplay)) {
        status += '发起人：' + tool.trim(todo.applyUserDisplay) + ', '
      }
      if (todo && tool.isNotBlank(todo.closedUserName)) {
        status += '处理人：' + tool.trim(todo.closedUserDisplay) + ', '
      }
      if (todo && tool.isNotBlank(todo.result)) {
        status += '处理时间：' + tool.trim(todo.closedAt) + ', '
      }
      if (todo && tool.isNotBlank(todo.result)) {
        status += '处理结果：' + tool.trim(todo.result) + ', '
      }
      if (status.length > 0) {
        return status.substring(0, status.length - 2)
      }
      return status
    },

    /**
     * 切换Tab面板，并初始化数据
     */
    handlePageChange() {
      this.page = {
        limit: 10,
        page: 1
      }
      this.loadData()
    },
    /**
     * 加载当前的页面数据
     */
    loadData() {
      /**
       * 已发起的待办事项
       */
      if (this.selectedTab === 'applied') {
        this.loading = true
        getTodoListApplied(this.page).then(res => {
          this.items = res.data.list
          this.total = res.data.total
        }).finally(res => {
          this.loading = false
        })
      } else if (this.selectedTab === 'closed') {
        /**
         * 已处理的待办事项
         */
        this.loading = true
        getTodoListClosed(this.page).then(res => {
          this.items = res.data.list
          this.total = res.data.total
        }).finally(res => {
          this.loading = false
        })
      } else if (this.selectedTab === 'todo') {
        /**
         * 待处理的待办事项
         */
        this.loading = true
        getTodoList().then(res => {
          this.items = res.data
          this.total = res.data.length
        }).finally(res => {
          this.loading = false
        })
      } else {
        /**
         * 未知的TAB页面
         */
        this.total = 0
        this.items = []
      }
    },
    /**
     * 点击行记录时，打开代办详情窗口
     * @param {Object} row 行数据
     */
    showTodoTargetPage(row) {
      var that = this
      window.layui.layer.open({
        type: 2, /* open with iframe */
        resie: true,
        maxmin: true,
        area: ['80%', '80%'],
        title: row.title,
        success: function(layero, index) {
          that.layerDologIndex = index
          window.layui.layer.full(index)
          layero.find('iframe')[0].contentWindow.closeParentLayerModel =
            function() {
              window.layui.layer.close(index)
              that.loadData()
            }
        },
        cancel: function(index, layero) {
          that.loadData()
        },
        content: tool.trim(row.targetPage)
      })
    }
  }
}
</script>
