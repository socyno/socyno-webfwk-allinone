<template>
  <div v-loading="loading" class="form-query-result-list">
    <BaseFormQuery
      v-if="queries && queries.length > 0"
      :form-name="formName"
      :query-models="queries"
      @input="onQueryApply"
      @change="onQueryChanged"
    />
    <BaseActionsPane
      :actions="actions"
      :title="formTitle"
      @input="onFormActionTriggerd"
    />
    <BaseFormTable
      v-if="currentQuery && currentQuery.resultClass && currentTableData"
      ref="formTable"
      :form-name="formName"
      :data="currentTableData"
      :columns="currentQuery.resultClass"
      :row-actions="rowActions"
      :actions-column-width="200"
      @paging="pageListData"
      @expand-change="onRowExpandChanged"
      @row-action-click="onRowActionClick"
    >
      <template v-if="showRowDetail()" v-slot:expand="{ formName, formModel, rowData }">
        <BaseFormDetail
          :form-id="rowData.row.id"
          :form-name="formName"
          :form-top="inIframe() ? 0 : 60"
          :show-form-header="false"
          :show-action="expandDetail !== 'noactions'"
          :refresh-interval="refreshDetail"
          @loaded="onRowFormLoaded"
          @delete="onRowFormDeleted"
        />
      </template>
      <template v-if="!showRowDetail() || expandDetail === 'noactions'" v-slot:operations="{ scope }">
        <el-dropdown
          ref="actionDropdown"
          trigger="click"
          :placement="eventsDropDown.placement"
          @command="openEventFormWindow"
        >
          <span class="el-dropdown-link" @click="loadFormActions(scope.$index, scope.row)">
            事件<i class="el-icon-arrow-down el-icon--right" />
          </span>
          <el-dropdown-menu slot="dropdown" class="action-dropdown">
            <slot v-for="(itemAction) in eventsDropDown.actions">
              <el-dropdown-item :command="itemAction" :disabled="itemAction.disabled">
                {{ itemAction.display }}
              </el-dropdown-item>
            </slot>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
    </BaseFormTable>

    <el-drawer
      ref="detailDrawer"
      :visible.sync="rowDetailDrawer.visible"
      class="row-form-detail-drawer"
      custom-class="row-form-detail-wrapper"
      :with-header="false"
      :modal="false"
      :append-to-body="true"
    >
      <BaseFormDetail
        v-if="rowDetailDrawer.visible"
        ref="formDetail"
        :form-top="inIframe() ? 0 : 60"
        :form-id="rowDetailDrawer.formId || ''"
        :form-name="formName"
        :enable-auto-load="false"
        :refresh-interval="refreshDetail"
        @back="rowDetailDrawer.visible = false"
        @loaded="onRowFormLoaded"
        @delete="onRowFormDeleted"
      />
    </el-drawer>

    <el-drawer
      ref="createDrawer"
      :visible.sync="rowCreateDrawer.visible"
      class="row-form-create-drawer"
      custom-class="row-form-create-wrapper"
      :with-header="false"
      :modal="false"
      :append-to-body="true"
    >
      <BaseFormAction
        v-if="rowCreateDrawer.visible"
        ref="createForm"
        class="row-form-create-content"
        @cancel="onRowCreatorCancel"
        @prepare="onRowCreatorPrepare"
        @create="onRowCreatorCommit"
      />
    </el-drawer>
  </div>
</template>
<script>

import '@/styles/form.scss'
import tool from '@/utils/tools'
import FormApi from '@/apis/formApi'
import { getVisibleFieldModels, FORM_FIELD_OPTIONS } from '@/utils/formUtils'
import BaseFormQuery from '@/components/BaseFormQuery'
import BaseFormTable from '@/components/BaseFormTable'
import BaseFormAction from '@/components/BaseFormAction'
import BaseFormDetail from '@/components/BaseFormDetail'
import BaseActionsPane from '@/components/BaseActionsPane'
export default {
  components: {
    BaseActionsPane,
    BaseFormQuery,
    BaseFormAction,
    BaseFormTable,
    BaseFormDetail
  },
  data() {
    return {
      actions: [],
      queries: [],
      formTitle: '',
      loading: false,
      pagingInfo: null,
      currentQuery: null,
      rowActions: [
        {
          text: '详情',
          onClick: function(row, index) {
            this.open(`#/form/detail?formName=${this.formName}&formId=${row.id}&autoRefresh=${this.refreshDetail}`)
          }
        },
        {
          text: '流程',
          onClick: function(row, index) {
            this.open(`#/form/flowchart/${this.formName}?formId=${row.id}`)
          }
        }
      ],
      formName: null,
      eventsDropDown: null,
      currentFieldModels: null,
      filterParams: null,
      currentTableData: null,
      rowDetailDrawer: {
        visible: false
      },
      rowCreateDrawer: {
        visible: false
      },
      rowDetailExpand: null,
      expandDetail: this.$route.query.expandDetail,
      refreshDetail: this.$route.query.refreshDetail
    }
  },

  watch: {
    '$route': function(to, from) {
      this.initFormDefinition()
    }
  },

  mounted() {
    this.initFormDefinition()
    window.addEventListener('message', this.eventMessage)
  },

  destroyed() {
    window.removeEventListener('message', this.eventMessage)
  },

  methods: {
    /**
     * 重置所有数据
     */
    resetAllData() {
      this.loading = false
      this.actions = []
      this.queries = []
      this.pagingInfo = {
        page: 1,
        limit: 10,
        total: -1
      }
      this.filterParams = {}
      this.currentQuery = {
        formClass: {}
      }
      this.eventsDropDown = {}
      this.currentFieldModels = {}
      this.expandDetail = this.$route.query.expandDetail
      this.refreshDetail = this.$route.query.refreshDetail
      this.formName = this.$route.params.formName
      this.formApi = new FormApi(this.formName)
      this.initAction = this.$route.query ? (tool.stringify(this.$route.query._action)) : ''
      this.resetPageData()
    },

    /**
     * 重置当前页的数据
     */
    resetPageData() {
      this.loading = false
      this.currentTableData = null
      this.rowDetailDrawer = {
        formId: '',
        visible: false
      }
      this.rowCreateDrawer = {
        visible: false
      }
    },

    inIframe() {
      return tool.inIframe()
    },

    /**
     * 加载当前流程实例的事件菜单
     */
    loadFormActions(index, row) {
      this.eventsDropDown = {
        actions: [],
        placement: index > 2 ? 'top-end' : 'bottom-end'
      }
      this.formApi.loadFormActionSimple(row.id).then((data) => {
        for (var event in data) {
          this.eventsDropDown.actions.push({
            name: event,
            display: data[event],
            formId: row.id,
            disabled: false
          })
        }
        if (this.eventsDropDown.actions.length <= 0) {
          this.eventsDropDown.actions.push({
            name: '',
            disabled: true,
            display: '无可执行事件'
          })
        }
      })
    },

    /**
     * 跳转到指定的事件操作界面
     */
    openEventFormWindow(itemAction) {
      tool.open(`#/form/detail?formName=${this.formName}&formId=${itemAction.formId}&initAction=${itemAction.name}&showAction=false&closeWhenActionCancel=true`)
    },

    /**
     * message 事件交互
     */
    eventMessage() {
      if (!tool.isPlainObject(event.data)) {
        return
      }
      if (event.data.listEvent === 'update-list-row' &&
            event.data.listFormName === this.formName &&
            tool.isPlainObject(event.data.formData) &&
            tool.isNumber(event.data.formData.id)) {
        this.onRowFormLoaded(event.data.formData, this.formName, event.data.formData.id)
      } else if (event.data.listEvent === 'delete-list-row' &&
            event.data.listFormName === this.formName &&
            event.data.formAction &&
            tool.looksLikeInteger(event.data.formId)) {
        this.onRowFormDeleted(event.data.resData, this.formName, event.data.formId, event.data.formAction)
        this.$notify.success('删除成功!')
      }
    },

    /**
     * 查询重置的回调
     */
    onQueryReset() {
      window.location.reload()
    },

    /**
     * 查询触发的回调
     */
    onQueryApply(params) {
      // console.log('查询的参数数据如下：', params)
      this.filterParams = params
      this.initListData()
    },

    /**
     * 查询切换时的回调
     */
    onQueryChanged(changedQuery) {
      if (!changedQuery) {
        return
      }
      // console.log('切换查询如下：', changedQuery)
      this.filterParams = {}
      this.currentQuery = changedQuery
      this.currentFieldModels = getVisibleFieldModels(
        changedQuery.resultClass,
        FORM_FIELD_OPTIONS.ListFirst
      )
      this.initListData()
    },

    /**
     * 加载流程表单的定义
     */
    initFormDefinition() {
      this.resetAllData()
      this.loading = true
      this.formApi.loadDefinition().then((data) => {
        this.queries = data.queries
        this.actions = data.quickActions
        this.formTitle = data.title
        if (tool.isBlank(this.expandDetail)) {
          this.expandDetail = data.properties.expandDetail
        }
        if (tool.isBlank(this.refreshDetail)) {
          this.refreshDetail = data.properties.refreshDetail
        }
        tool.title(data.title || '表单列表', true)
      }).finally(res => {
        this.loading = false
      })
    },

    /**
     * 重置结果列表（回首页）
     */
    initListData() {
      var params = {}
      this.resetPageData()
      this.pagingInfo.page = 1
      Object.assign(params, this.pagingInfo)
      Object.assign(params, this.filterParams)
      this.loading = true
      this.formApi.queryFormPagedData(this.currentQuery.name, params, true).then((data) => {
        this.pagingInfo.total = data.total
        this.currentTableData = data.list || []
        this.$nextTick(function() {
          this.$refs.formTable.setPaging(this.pagingInfo)
          if (!tool.isBlank(this.initAction)) {
            var initAction = this.initAction
            this.initAction = ''
            var initActionIndex = tool.inArray(initAction, this.actions, function(a, v) {
              return a.name === v
            })
            if (initActionIndex < 0) {
              this.$notify.error(`初始化事件(${initAction})不存在或未经授权`)
              return
            }
            this.onFormActionTriggerdFromList(initAction, this.actions[initActionIndex])
          }
        })
      }).finally(() => {
        this.loading = false
      })
    },

    /**
     * 结果列表翻页
     */
    pageListData(page, limit) {
      var params = {}
      this.resetPageData()
      this.pagingInfo.page = page
      this.pagingInfo.limit = limit
      Object.assign(params, this.pagingInfo)
      Object.assign(params, this.filterParams)
      this.loading = true
      this.formApi.queryFormPagedData(this.currentQuery.name, params).then((data) => {
        this.currentTableData = data.list || []
        this.$nextTick(function() {
          this.$refs.formTable.setPaging(this.pagingInfo)
        })
      }).finally(() => {
        this.loading = false
      })
    },

    // 处理action回调
    onFormActionTriggerd(actionName, formAction) {
      const routeUrl = this.$router.resolve({
        path: `/form/create/${this.formName}/${actionName}`,
        query: this.$route.query
      })
      window.open(routeUrl.href, '_blank')
    },

    onFormActionTriggerdFromList(actionName, formAction) {
      this.rowCreateDrawer.visible = true
      this.$nextTick(function() {
        this.$refs.createForm.create(this.formName, formAction, this.$route.query)
      })
    },

    /**
     *  在新窗口中打开指定页面
     */
    open(linkUrl) {
      tool.open(linkUrl)
    },

    /**
     * 行操作按钮点击的回调方法
     */
    onRowActionClick(action, row, index) {
      if (!tool.isFunction(action.onClick)) {
        return
      }
      action.onClick.call(this, row, index)
    },

    /**
     * 行操作按钮是否显示的回调方法
     */
    checkRowActionVisible(action, row, index) {
      if (!tool.isFunction(action.visible)) {
        return true
      }
      return action.visible.call(this, row, index)
    },

    /**
     * 是否在展开行中显示详情
     */
    showRowDetail() {
      if (tool.isBlank(this.expandDetail)) {
        return false
      }
      this.expandDetail = tool.toLower(tool.trim(this.expandDetail))
      if (this.expandDetail === '0' || this.expandDetail === 'false' ||
              this.expandDetail === 'off' || this.expandDetail === 'no') {
        return false
      }
      return true
    },

    /**
     * 行展开时的回调方法，用于确保详情模式下只展开一行。
     * 否则，基于详情的扩展功能就会出现异常（系统缺陷，待修复）。
     * @param {Object} row 当前操作行
     * @param {Object} expandRows 当前展开行列表
     */
    onRowExpandChanged(row, expandRows) {
      this.rowDetailExpand = null
      if (!this.showRowDetail()) {
        return
      }
      for (var r = 0; r < expandRows.length; r++) {
        if (row === expandRows[r]) {
          this.rowDetailExpand = row
          break
        }
      }
      if (!this.rowDetailExpand) {
        return
      }
      // console.log('当前操作为展开行 = ', row.id)
      for (r = 0; r < expandRows.length; r++) {
        if (row.id !== expandRows[r].id) {
          // console.log('收起已展开行 = ', expandRows[r].id)
          this.$refs.formTable.toggleRowExpansion(expandRows[r], false)
        }
      }
    },

    /**
     * 加载并显示操作事件面板
     * @param {Number} formId
     */
    showFormDetail(formId) {
      this.rowDetailDrawer.visible = true
      this.rowDetailDrawer.formId = formId
      this.$nextTick(function() {
        this.$refs.formDetail.load()
      })
    },

    /**
     * 表单详情的数据加载或更新后的回调
     */
    onRowFormLoaded(formData, formName, formId) {
      // var newTableData = []
      var hasDataReplaced = false
      var length = this.currentTableData.length
      for (var r = length - 1; r >= 0; r--) {
        if (this.currentTableData[r].id === formData.id) {
          hasDataReplaced = true
          // console.log('替换当前列表中的数据项：', r, ' => ', formData)
          // 防止单数据中存在重复项时，引发展开和收起的功能同时生效
          if (formData.revision !== this.currentTableData[r].revision) {
            var newRow = tool.jsonCopy(formData)
            this.currentTableData.splice(r, 1, newRow)
            this.$nextTick(() => {
              if (this.rowDetailExpand && this.rowDetailExpand.id === newRow.id && this.$refs.formTable) {
                this.$refs.formTable.toggleRowExpansion(newRow, true)
              }
            })
          }
          // } else {
          //   newTableData[r] = this.currentTableData[r]
        }
      }
      if (!hasDataReplaced) {
        this.currentTableData.splice(0, 0, tool.jsonCopy(formData))
        // newTableData.splice(0, 0, formData)
      }
      // this.currentTableData = newTableData
    },

    /**
     * 表单内容更新回调
     */
    onRowFormDeleted(resData, formName, formId, formAction) {
      /* 当事件为删除时，则关闭详情页并从表格中移除数据 */
      if (this.$refs.detailDrawer) {
        this.$refs.detailDrawer.closeDrawer()
      }
      var newTableData = []
      for (var item of this.currentTableData) {
        if (tool.trim(item.id) === tool.trim(formId)) {
          continue
        }
        newTableData.push(item)
      }
      this.currentTableData = newTableData
    },

    /**
     * 创建事件取消的回调
     */
    onRowCreatorCancel() {
      this.rowCreateDrawer.visible = false
    },

    /**
     * 创建事件就绪的回调
     */
    onRowCreatorPrepare() {

    },

    /**
     * 创建事件提交的回调
     */
    onRowCreatorCommit(resData, formName, formId, formAction) {
      this.rowCreateDrawer.visible = false
      this.showFormDetail(formId)
    }
  }
}
</script>
<style lang="scss">
.form-query-result-list {
}

.row-form-detail-drawer {
  top: 60px !important;
  .row-form-detail-wrapper {
    display: block !important;
    width: 100% !important;
  }
}
.row-form-create-drawer {
  top: 60px !important;
  .row-form-create-wrapper {
    display: block !important;
    width: 100% !important;
    .row-form-create-content {
      position: absolute !important;
      top: 0px !important;
      left: 0px !important;
      right: 0px !important;
      bottom: 0px !important;
      overflow-y: auto;
    }
  }
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  font-size: 12px;
}
.el-icon-arrow-down {
  font-size: 12px;
}
.action-dropdown {
  max-height: 200px;
  overflow-y: auto;
  overflow-x: hidden;
}
.action-dropdown::-webkit-scrollbar-track {
  border-radius: 5px;
  background-color: #928c8c;
}
.el-dropdown-menu {
  width: 14%;
}
</style>
