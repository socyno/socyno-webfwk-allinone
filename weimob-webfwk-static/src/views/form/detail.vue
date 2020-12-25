<template>
  <!--
    此处判断是否在 iframe 内嵌入来决定事件表单的顶部位置
   （ 此时菜单是不可见的, 也就无需预留其显示位置）
  -->
  <BaseFormDetail
    ref="baseDetailForm"
    :show-action="showAction"
    :show-form-close="showFormClose"
    :show-form-header="showFormHeader"
    :form-id="formId"
    :form-name="formName"
    :form-top="inIframe() ? 0 : 60"
    :refresh-interval="refreshInterval"
    @back="onFormClose"
    @loaded="onFormLoaded"
    @loading="onFormLoading"
    @delete="onFormDeleted"
    @actionFinished="onActionFinished"
    @actionCancelled="onActionCancelled"
  />
</template>
<script>
import tool from '@/utils/tools'
import BaseFormDetail from '@/components/BaseFormDetail'
export default {
  components: {
    BaseFormDetail
  },
  data() {
    return {
      refreshInterval: this.$route.query.autoRefresh,
      showAction: this.$route.query.showAction !== 'false',
      showFormClose: this.$route.query.showFormClose !== 'false',
      showFormHeader: this.$route.query.showFormHeader !== 'false',
      closeWhenActionCancelled: this.$route.query.closeWhenActionCancel === 'true',
      formId: '',
      formName: '',
      respData: null,
      initAction: this.$route.query.initAction
    }
  },
  watch: {
    '$route': function(to, from) {
      this.resetData()
    }
  },
  created() {
    this.resetData()
  },
  methods: {
    /**
     * 事件操作完成后触发
     */
    onActionFinished(success, formAction) {
      // TODO: 考虑是否延时关闭窗口
    },

    /**
     * 事件操作取消后触发
     */
    onActionCancelled(formAction) {
      if (this.closeWhenActionCancelled) {
        this.onFormClose()
      }
    },

    inIframe() {
      return tool.inIframe()
    },

    resetData() {
      this.formId = this.$route.query.formId
      this.formName = this.$route.query.formName
    },

    onFormLoading(formName, formId) {
    },

    /**
     * 表单内容加载完成时，
     * 发送列表项更新的消息
     * @param {Object} data
     */
    onFormLoaded(data) {
      if (!tool.isBlank(this.initAction)) {
        this.$refs['baseDetailForm'].triggerNamedAction(this.initAction)
        this.initAction = ''
      }
      if (window.opener) {
        window.opener.postMessage({
          listEvent: 'update-list-row',
          listFormName: this.formName,
          formData: data
        }, '*')
      }
    },

    /**
     * 表单内容加载完成时，
     * 发送列表项删除的消息
     * @param {Object} data
     */
    onFormDeleted(resData, formName, formId, formAction) {
      if (window.opener) {
        window.opener.postMessage({
          listEvent: 'delete-list-row',
          listFormName: formName,
          formId: formId,
          formAction: formAction,
          resData: resData
        }, '*')
      }
      this.onFormClose(formName, formId)
    },

    /**
     * 表单关闭事件回调
     * @param {String} formName
     * @param {Number} formId
     */
    onFormClose(formName, formId) {
      /**
       * iframe 模式下，使关闭窗口失效
       */
      if (tool.inIframe()) {
        window.parent.postMessage('close-window', '*')
      }
      tool.close()
    }
  }
}
</script>
