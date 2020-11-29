<template>
  <div class="form-change-logs">
    <el-page-header
      class="common-page-header"
      title="关闭"
      :content="formTitle"
      @back="$emit('back')"
    />
    <el-table v-loading="loading" :data="formLogsData" border>
      <el-table-column label="操作人" prop="operateUserDisplay" />
      <el-table-column label="操作时间" prop="operateTime" />
      <el-table-column label="操作类型" prop="operateType" />
      <el-table-column label="操作说明">
        <template slot-scope="scope">
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div v-html="formatOperateDescAsHtml(scope.row.operateDesc)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template slot-scope="scope">
          <el-button v-if="scope.row.operateDetailId" type="text" size="small" @click="showLogsDetail(scope.row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button-group>
      <el-button type="normal" size="small" icon="el-icon-arrow-left" @click="pageLogsLoad('')">
        首页
      </el-button>
      <el-button :disabled="logsPager.page <= 1" type="normal" size="small" icon="el-icon-arrow-left" @click="pageLogsLoad('prev')">
        上一页
      </el-button>
      <el-button :disabled="logsPager.nomore" type="normal" size="small" @click="pageLogsLoad('next')">
        下一页<i class="el-icon-arrow-right el-icon--right" />
      </el-button>
      <el-button type="text" size="small" style="margin-left:10px; font-size:14px; color:#666;">
        当前: 第 {{ logsPager.page }} 页 {{ formLogsData ? formLogsData.length : 0 }} 条
      </el-button>
    </el-button-group>
  </div>
</template>
<script>
import tool from '@/utils/tools'
import { Loading } from 'element-ui'
import FormApi from '@/apis/formApi'
export default {
  props: {
    formName: {
      type: String,
      required: true
    },
    formId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      loading: false,
      formApi: null,
      fromIndex: 0,
      formTitle: '',
      formLogsData: null,
      detailDialogShow: false,
      logsPager: {
        page: 1,
        firstLogId: 0,
        lastLogId: 0,
        nomore: false
      }
    }
  },
  watch: {
    fromIndex: {
      handler: function(fromIndex) {
        this.loadFormLogs()
      }
    }
  },
  methods: {
    /**
     * 初始化操作日志数据
     */
    load() {
      this.fromIndex = 0
      this.loadFormLogs()
    },

    /**
     * 设置标题
     */
    setFormTitle() {
      this.formTitle = '变更日志 - ' + tool.leftPad(this.formId, 8, 0)
    },

    /**
     * 加载操作日志数据
     */
    loadFormLogs() {
      this.loading = true
      this.setFormTitle()
      new FormApi(this.formName).loadFormActionLogs(this.formId, this.fromIndex).then(data => {
        if (!data || data.length < 1) {
          this.logsPager.nomore = true
          if (this.fromIndex > 0) {
            this.$message.info('没有更多数据')
          }
          return
        }
        this.logsPager.nomore = false
        this.formLogsData = data
        if (!this.fromIndex) {
          this.logsPager.page = 1
        } else if (this.fromIndex > 0) {
          this.logsPager.page += 1
        } else {
          this.logsPager.page -= 1
        }
        this.logsPager.firstId = this.formLogsData[0].id
        this.logsPager.lastId = this.formLogsData[data.length - 1].id
      }).finally((res) => {
        this.loading = false
      })
    },

    /**
     * 加载操作日志变更详情
     */
    showLogsDetail(logRow) {
      var loading = Loading.service({
        fullscreen: true,
        text: '请求中…',
        background: 'rgba(0, 0, 0, 0.1)'
      })
      new FormApi(this.formName).loadFormActionLogDetail(this.formId, logRow.operateDetailId).then(data => {
        window.$openDifferDialog(data.operateBefore, data.operateAfter, {
          title: '变更内容对比 - ' + tool.leftPad(logRow.id, 8, 0),
          beforeLabel: '变更前',
          changedLabel: '变更后'
        })
      }).finally((res) => {
        loading.close()
      })
    },

    /**
     * 翻页方向（prev 向前，next 向后，first 首页）
     * @param {String} direction prev / next /first
     */
    pageLogsLoad(direction) {
      if (direction === 'next') {
        this.fromIndex = this.logsPager.lastId
      } else if (direction === 'prev') {
        this.fromIndex = 0 - this.logsPager.firstId
      } else {
        this.fromIndex = 0
      }
    },

    /**
     * 日志内容中替换特殊的标记，实现 HTML 样式
     * @param {String} descText
     */
    formatOperateDescAsHtml(descText) {
      descText = tool.escape(descText)
      var maxLimited = 20
      while (maxLimited-- > 0) {
        var replaced = descText.replace(/\[websocket:([^:]+):([^\]]+)\]/i,
          '<a target="_blank" href="#/form/result/WebSocketViewLink?arg=$1">$2</a>')
        if (replaced === descText) {
          break
        }
        descText = replaced
      }
      return descText
    }
  }
}
</script>
<style lang='scss'>
.el-dialog__header{
  border-bottom: 1px solid #ddd;
}
</style>
