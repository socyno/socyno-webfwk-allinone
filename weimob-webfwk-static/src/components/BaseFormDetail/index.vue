<template>
  <div class="form-detail-wrapper">
    <div :class="getDetailFormClass()">
      <div v-if="showFormHeader && !showFormClose" class="form-title">
        {{ formTitle }}
      </div>
      <el-page-header
        v-else-if="showFormHeader"
        class="common-page-header"
        title="关闭"
        :content="formTitle"
        @back="onFormClose"
      />
      <BaseFormContent
        v-if="formModel"
        ref="baseFormContent"
        :form-id="formId"
        :form-name="formName"
        :form-model="formModel"
        :default-data="formData"
        :watch-default-data="true"
      />
      <Comments
        ref="formComments"
        :form-name="formName"
        :form-id="formId"
      />
    </div>
    <div v-if="formActions" :class="getActionsPaneClass()">
      <el-button
        v-for="(action, index) in formActions"
        :key="`${action.name}-${index}`"
        :style="{ display: action.visible ? '' : 'none' }"
        type="primary"
        @click="triggerFormAction(action)"
      >
        {{ action.display }}
      </el-button>
      <el-button type="info" @click="openFlowChart()">
        查看流程图
      </el-button>
      <el-button type="info" @click="showChangeLogs()">
        变更日志
      </el-button>
      <el-button type="info" @click="load()">
        刷新页面
      </el-button>
      <!-- <el-button type="info" @click="loadSnapshot()">
        加载快照
      </el-button>
      <el-button v-if="!snapshotModel" type="info" @click="createSnapshot()">
        创建快照
      </el-button> -->
    </div>
    <el-drawer
      :visible.sync="actionDrawer.visible"
      class="form-action-drawer"
      custom-class="form-action-wrapper"
      :modal="true"
      :with-header="false"
      :wrapper-closable="false"
      direction="ltr"
      :append-to-body="true"
      :modal-append-to-body="true"
      @open="onActionDrawerOpen"
    >
      <div v-if="actionDrawer.showChangeLogs">
        <Logs
          ref="formLogs"
          class="form-action-content"
          :form-name="formName"
          :form-id="formId"
          @back="onFormLogsClosed"
        />
      </div>
      <BaseFormAction
        v-else-if="actionDrawer.visible"
        ref="actionForm"
        class="form-action-content"
        @cancel="onActionFormCancel"
        @prepare="onActionFormPrepare"
        @delete="onFormDelete"
        @change="onFormCommit"
        @triggerFinished="onActionTriggerFinished"
        @triggerCancelled="onActionTriggerCancelled"
      />
    </el-drawer>

    <el-dialog
      title="创建快照"
      :visible.sync="snapshotCreator.visible"
      class="form-snapshot-create"
      :modal="true"
      :wrapper-closable="false"
      :append-to-body="true"
      :modal-append-to-body="true"
    >
      <span>请输入快照的名称：</span>
      <el-input v-model="snapshotCreator.name" type="input" />
      <el-button type="primary" @click="_createSnapshot(snapshotCreator.name)">
        保存
      </el-button>
      <el-button style="float:right;" type="info" @click="snapshotCreator.visible = false">
        取消
      </el-button>
    </el-dialog>

    <el-dialog
      width="80%"
      title="请选择快照"
      :visible.sync="snapshotSelector.visible"
      class="form-snapshot-select"
      :modal="true"
      :wrapper-closable="false"
      :append-to-body="true"
      :modal-append-to-body="true"
    >
      <BaseFormField label-text="名称" label-width="60" placement="3">
        <el-input v-model="snapshotSelector.name" size="mini" />
      </BaseFormField>
      <BaseFormField label-text="创建者" label-width="60" placement="3">
        <el-input v-model="snapshotSelector.name" size="mini" />
      </BaseFormField>
      <BaseFormField label-text="创建时间" label-width="80" placement="3">
        <el-date-picker v-model="snapshotSelector.createdStart" size="mini" type="date" placeholder="起始日期" style="width: 100%;" />
      </BaseFormField>
      <BaseFormField label-width="0" placement="2">
        <el-date-picker v-model="snapshotSelector.createdEnd" size="mini" type="date" placeholder="结束时间" style="width: 100%;" />
      </BaseFormField>
      <BaseFormField label-width="0" placement="1">
        <el-button size="mini" type="primary" @click="_querySnapshot()">
          查询
        </el-button>
      </BaseFormField>
      <el-table
        :data="snapshotSelector.queriedData"
        style="width: 99%"
      >
        <el-table-column
          type="index"
          :index="1"
          width="40"
          show-overflow-tooltip
        />
        <el-table-column
          label="名称"
          prop="name"
          show-overflow-tooltip
        />
        <el-table-column
          label="创建时间"
          prop="createdAt"
          show-overflow-tooltip
        />
        <el-table-column
          label="创建者"
          prop="createdBy"
          show-overflow-tooltip
        />
        <el-table-column>
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="_loadSnapshot(scope.row.id)"
            >
              加载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>
<script>

import '@/styles/form.scss'
import Logs from './log'
import Comments from './comment'
import tool from '@/utils/tools'
import FormApi from '@/apis/formApi'
import BaseFormField from '@/components/BaseFormField'
import BaseFormAction from '@/components/BaseFormAction'
import BaseFormContent from '@/components/BaseFormContent'
export default {
  components: {
    Logs,
    Comments,
    BaseFormField,
    BaseFormAction,
    BaseFormContent
  },
  props: {
    /**
     * 是否页头关闭按钮
     */
    showFormClose: {
      type: Boolean,
      default: true
    },
    /**
     * 是否显示的页头
     */
    showFormHeader: {
      type: Boolean,
      default: true
    },
    showAction: {
      type: Boolean,
      default: true
    },
    formId: {
      type: [Number, String],
      required: true
    },
    formName: {
      type: [String],
      required: true
    },
    formTop: {
      type: [Number],
      default: 0
    },
    /**
     * 是否组件初始化后自动加载表单数据。
     * 默认值为 true。
     */
    enableAutoLoad: {
      type: Boolean,
      default: true
    },
    /**
     * 详情数据自动刷新频率(单位为秒)。
     * 当值小于或等于0时，禁用自动刷新。
     */
    refreshInterval: {
      type: [Number, String],
      default: 0
    }
  },
  data() {
    return {
      formTitle: '',
      formModel: null,
      formData: {},
      formActions: [],
      snapshotModel: false,
      actionDrawer: null,
      snapshotCreator: null,
      snapshotSelector: null,
      autoRefreshTimer: null
    }
  },
  watch: {
    formId: {
      handler: function() {
        if (this.formId && this.enableAutoLoad) {
          this.load()
        }
      }
    }
  },
  created() {
    if (this.enableAutoLoad) {
      this.load()
    } else {
      this.resetAllData()
    }
  },
  mounted() {
    /**
     * 以便在表单模板中使用
     */
    window.$formDetailsVueComponent = this
  },
  beforeDestroy() {
    window.$formDetailsVueComponent = null
    if (this.autoRefreshTimer) {
      clearTimeout(this.autoRefreshTimer)
    }
  },
  methods: {
    /**
     * 根据给定的事件名称自动触发事件
     */
    triggerNamedAction(formActionName) {
      if (tool.isBlank(formActionName)) {
        return
      }
      for (const action of this.formActions) {
        if (action.name === formActionName) {
          this.triggerFormAction(action)
          return
        }
      }
      this.$notify.info(`指定事件：${formActionName} 不可执行！`)
    },

    /**
     * 重置所有数据
     */
    resetAllData() {
      this.actionDrawer = {
        visible: false,
        hidden: false,
        showChangeLogs: false
      }
      this.snapshotCreator = {
        name: '',
        visible: false
      }
      this.snapshotSelector = {
        name: '',
        createdBy: '',
        createdStart: '',
        createdEnd: '',
        queriedData: [],
        visible: false
      }
      this.formModel = null
      this.formData = {}
      this.formActions = []
      if (this.autoRefreshTimer) {
        clearTimeout(this.autoRefreshTimer)
        this.autoRefreshTimer = null
      }
    },

    /**
     * 加载并显示操作事件面板
     *
     * @param {Object} extendParams 额外的查询参数，可用于扩展详情页数据的扩展
     */
    load(extendParams) {
      this.resetAllData()
      this.formApi = new FormApi(this.formName)
      this.$emit('loading', this.formName, this.formId)
      this.formApi.loadDetailWithActions(this.formId, extendParams).then((data) => {
        this.snapshotModel = false
        this.formData = data.form
        this.formModel = data.formClass
        this.formActions = data.actions
        this.formTitle = this.genFormTitle()
        this.$nextTick(function() {
          var refreshInterval
          if ((refreshInterval = tool.parseInteger(this.refreshInterval)) > 0) {
            this.autoRefreshTimer = setTimeout(() => {
              this.load(extendParams)
            }, refreshInterval * 1000)
          }
          if (this.$refs.formComments) {
            this.$refs.formComments.load()
          }
        })
        this.$emit('loaded', data.form, this.formName, this.formId)
      })
    },

    /**
     * 创建快照
     */
    createSnapshot() {
      this.snapshotCreator.name = ''
      this.snapshotCreator.visible = true
    },

    _createSnapshot(title) {
      if (tool.isBlank(title)) {
        this.$notify.error('快照的名称不能为空')
        return
      }
      this.formApi = new FormApi(this.formName)
      this.formApi.createSnapshotData(this.formId, title, {
        formData: this.formData,
        formModel: this.formModel,
        formActions: this.formActions
      }).then((data) => {
        this.$notify('保存成功')
      })
    },

    /**
     * 查询快照
     */
    _querySnapshot() {
      this.formApi.listSnapshotRecords(this.formId, {
        name: this.snapshotSelector.name,
        createdBy: this.snapshotSelector.createdBy,
        createdStart: this.snapshotSelector.createdStart,
        createdEnd: this.snapshotSelector.createdEnd
      }).then((data) => {
        this.snapshotSelector.queriedData = data
      })
    },

    /**
     * 加载快照数据
     */
    loadSnapshot() {
      this.snapshotSelector.visible = true
    },

    _loadSnapshot(snapshotId) {
      this.resetAllData()
      this.snapshotSelector.visible = false
      this.formApi = new FormApi(this.formName)
      this.formApi.loadSnapshotData(this.formId, snapshotId).then((data) => {
        this.snapshotModel = true
        this.formData = data.formData
        this.formModel = data.formModel
        this.formActions = data.formActions
        this.formTitle = this.genFormTitle()
      })
    },

    /**
     * 获取指定名称的事件对象
     * @param {String} actionName
     */
    getFormAction(actionName) {
      if (!tool.isString(actionName) || tool.isBlank(actionName)) {
        return null
      }
      if (!this.formActions || !tool.isArray(this.formActions)) {
        return null
      }
      for (var formAction of this.formActions) {
        if (formAction && formAction.name === actionName) {
          return formAction
        }
      }
      return null
    },

    /**
     * 执行指定的事件
     * @param {FormAction} formAction
     */
    triggerFormAction(formAction, actionParams, withoutForm, options) {
      if (this.snapshotModel) {
        this.$notify.error('当前为快照模式，禁止执行任何操作')
        return
      }
      // console.log('点击操作：', formAction)
      if (tool.isString(formAction)) {
        formAction = this.getFormAction(formAction)
      }
      if (!tool.isPlainObject(formAction)) {
        throw new Error('No such form action found')
      }
      withoutForm = withoutForm ||
          tool.toUpper(formAction.eventFormType) === 'NULL'
      this.actionDrawer.visible = true
      this.actionDrawer.hidden = withoutForm
      this.actionDrawer.showChangeLogs = false
      this.$nextTick(function() {
        this.$refs.actionForm.trigger(
          this.formName,
          formAction,
          this.formId,
          this.formData,
          actionParams,
          withoutForm,
          options
        )
      })
    },

    /**
     * 页面关闭的回调
     */
    onFormClose() {
      this.$emit('back', this.formName, this.formId)
    },

    /**
     * 当表单日志窗口关闭时的回调
     */
    onFormLogsClosed() {
      this.actionDrawer.visible = false
    },

    /**
     * 表单操作的取消回调
     */
    onActionFormCancel(formName, formId, formAction) {
      this.actionDrawer.visible = false
      this.$emit('actionCancelled', formAction)
    },

    /**
     * 事件触发取消的回调
     */
    onActionTriggerCancelled(formName, formId, formAction) {
      if (this.actionDrawer.hidden) {
        this.actionDrawer.visible = false
        this.$emit('actionCancelled', formAction)
      }
    },

    /**
     * 事件触发结束的回调
     */
    onActionTriggerFinished(success, formName, formId, formAction) {
      if (success || this.actionDrawer.hidden) {
        this.actionDrawer.visible = false
      }
      this.$emit('actionFinished', success, formAction)
    },

    /**
     * 表单操作的就绪回调
     */
    onActionFormPrepare() {

    },

    /**
     * 表单删除操作的回调
     */
    onFormDelete(resData, formName, formId, formAction) {
      this.actionDrawer.visible = false
      this.$emit('delete', resData, formName, formId, formAction)
    },

    /**
     * 表单更新操作的回调
     */
    onFormCommit(resData, formName, formId, formAction) {
      this.actionDrawer.visible = false
      this.load()
    },

    /**
     *  设置当前表单的标题
     */
    genFormTitle() {
      var title = null
      if (this.formModel) {
        if (this.formModel.title) {
          title = this.formModel.title
        }
      }
      var summary = null
      if (this.formData) {
        if (this.formData.summary) {
          summary = this.formData.summary
        } else if (this.formData.id) {
          summary = tool.leftPad(this.formData.id, 8, 0)
        }
      }
      title = tool.stringify(title)
      if (!tool.isNullOrUndef(summary)) {
        title = title + ' - ' + summary
      }
      return title
    },

    /**
     * 展示变更日志
     */
    showChangeLogs() {
      this.actionDrawer.visible = true
      this.actionDrawer.hidden = false
      this.actionDrawer.showChangeLogs = true
      this.$nextTick(function() {
        this.$refs.formLogs.load()
      })
    },

    /**
     * 强制操作界面的顶部位置
     */
    onActionDrawerOpen() {
      this.$nextTick(function() {
        var actionDrawer = document.getElementsByClassName('form-action-drawer')[0]
        actionDrawer.style.top = this.actionDrawer.hidden ? '-100000px' : (this.formTop + 'px')
      })
    },

    /**
     * 获取表单的内部绑定数据
     */
    getInnerFormData() {
      this.$refs.baseFormContent.formDataValidation()
      return this.$refs.baseFormContent.innerFormData
    },
    /**
     * 显示流程实例的图例
     */
    openFlowChart() {
      tool.open(`#/form/flowchart/${this.formName}?formId=${this.formId}`)
    },
    /**
     * 强制刷新表单的内容数据
     */
    forceFormRerender() {
      this.$refs.baseFormContent.forceFormRerender()
    },

    /**
     * 计算详情表单的 class 样式
     */
    getDetailFormClass() {
      var clazz = 'form-detail-content'
      if (!this.showAction) {
        clazz += ' form-detail-acthidden'
      }
      return clazz
    },

    /**
     * 计算操作面板的 class 样式
     */
    getActionsPaneClass() {
      var clazz = 'form-detail-btnpane'
      if (!this.showAction) {
        clazz += ' form-detail-acthidden'
      }
      return clazz
    }
  }
}
</script>
<style lang="scss">
.form-detail-wrapper {
  // position:absolute !important;
  // top: 0px !important;
  // left: 0px !important;
  // bottom: 0px !important;
  display: block !important;
  background-color: #FFF !important;
  opacity: initial !important;
  width: 100% !important;
  min-height: 300px;
  .form-detail-content {
    // position: absolute !important;
    // top: 0px !important;
    // left: 0px !important;;
    // bottom: 0px !important;
    // overflow-y: auto;
    border-right:2px #EEE solid;
    width: calc(100% - 150px) !important;
  }
  .form-detail-content.form-detail-acthidden {
    width: 100% !important;
  }
  .form-detail-btnpane {
    padding-top: 20px;
    position:absolute !important;
    top: 0px !important;
    right: 0px !important;
    bottom: 0px !important;
    width: 140px !important;
  }
  .form-detail-btnpane.form-detail-acthidden {
    display: none !important;
  }
  .form-detail-btnpane .el-button {
     display: block;
     margin-left: 0px;
     width: 130px;
   }
  .form-title {
    margin-top: 10px;
    margin-left: 40px;
    font-size: 18px;
    color: #303133;
  }
}
.form-action-drawer {
    background-color: #FFF;
    right: 0px !important;
    width: calc(100% - 0px) !important;
  .form-action-wrapper {
     width: 99% !important;
    .form-action-content {
      position: absolute !important;
      top: 0px !important;
      left: 0px !important;
      right: 0px !important;
      bottom: 0px !important;
      overflow-y: auto;
    }
  }
}
</style>
