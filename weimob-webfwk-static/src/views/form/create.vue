<template>
  <BaseFormAction
    ref="createForm"
    class="row-form-create-content"
    @cancel="onRowCreatorCancel"
    @prepare="onRowCreatorPrepare"
    @create="onRowCreatorCommit"
  />
</template>
<script>
import FormApi from '@/apis/formApi'
import BaseFormAction from '@/components/BaseFormAction'
export default {
  components: {
    BaseFormAction
  },
  data() {
    return {
      formName: this.$route.params.formName,
      actionName: this.$route.params.formAction,
      actions: []
    }
  },
  mounted() {
    this.initFormDefinition()
  },
  methods: {
    /**
       * 加载流程表单的定义
       */
    initFormDefinition() {
      this.formApi = new FormApi(this.formName)
      this.loading = true
      this.formApi.loadDefinition().then((data) => {
        this.actions = data.quickActions
        let actionMatched = false
        for (let i = 0; i < this.actions.length; i++) {
          if (this.actions[i].name === this.actionName) {
            actionMatched = true
            this.$refs.createForm.create(this.formName, this.actions[i], this.$route.query)
            break
          }
        }
        if (!actionMatched) {
          this.$router.push({ path: `/*` })
        }
      }).finally(res => {
        this.loading = false
      })
    },

    /**
       * 创建事件取消的回调
       */
    onRowCreatorCancel() {
      this.$confirm(`是否确认关闭当前页面?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        window.close()
      })
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
      this.$router.replace({ name: 'Detail', query: { formName: formName, formId: formId }})
    }
  }
}
</script>
