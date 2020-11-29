<template>
  <div>
    <div class="tabbar">
      <el-radio-group v-model="selectedTab" size="small" @change="handlePageChange">
        <el-radio-button label="todo">
          待处理
        </el-radio-button>
        <!-- <el-radio-button label="closed">
          已审批
        </el-radio-button>
        <el-radio-button label="applied">
          已发起
        </el-radio-button> -->
      </el-radio-group>
    </div>
    <div v-loading="loading" class="index-todo-list">
      <BaseInfoRow v-for="(item, index) in items" :key="index" :title="item.title" :time="item.createdAt" @click="handleRow(item)" />
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
import { getTodoList, getTodoListClosed, getTodoListCreated } from '@/apis/common'
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
        getTodoListCreated(this.page).then(res => {
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
    handleRow(row) {
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
