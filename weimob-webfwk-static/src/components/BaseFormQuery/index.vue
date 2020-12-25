<template>
  <div class="form-query-filter">
    <BaseFormContent
      v-if="queryModel"
      ref="queryForm"
      :editable="true"
      :collapsible="true"
      :form-id="formId"
      :form-name="formName"
      :form-model="queryModel.formClass"
      :parent-field-models="parentFieldModels"
      :actions="[{ name: '查询' }]"
      @actions="onFormSubmitOrQuery"
    />
  </div>
</template>
<script>
import tool from '@/utils/tools'
export default {
  name: 'BaseFormQuery',
  components: {
    BaseFormContent: () => import('../BaseFormContent')
  },
  props: {
    formId: {
      type: [String, Number],
      default: null
    },
    formName: {
      type: String,
      required: true
    },
    /**
     * 可用查询模型清单
     */
    queryModels: {
      type: Array,
      required: true
    },
    parentFieldModels: {
      type: Array,
      default: null
    }
  },
  data() {
    return {
      selected: 0,
      queryModel: null
    }
  },
  watch: {
    selected: {
      // immediate: true,
      handler: function(selected) {
        if (!this.queryModels) {
          return
        }
        // console.log('当前选择查询为 ：', selected)
        this.queryModel = this.queryModels[selected]
        // console.log('当前选择查询为 ：', this.queryModel)
        this.onSelectedChanged(this.queryModel)
      }
    },
    queryModels: {
      immediate: true,
      handler: function(queries) {
        if (tool.isUndefOrNull(this.selected) || this.selected < 0 || this.selected >= queries.length) {
          this.selected = 0
          return
        }
        this.queryModel = queries[this.selected]
        this.onSelectedChanged(this.queryModel)
      }
    }
  },
  methods: {
    /**
     * 表单的查询或提交操作
     */
    onFormSubmitOrQuery() {
      var params
      if (!(params = this.$refs.queryForm.getFormValidData())) {
        return
      }
      this.$emit('input', params)
    },
    /**
     * 切换查询时的回调
     */
    onSelectedChanged(queryModel) {
      // console.log('查询发生变化', selected)
      this.$emit('change', queryModel)
    }
  }
}
</script>
<style lang="scss">
.form-query-filter {
  background-color: #fff;
  color: #222;
  display: block;
  .form-query-buttons {
    margin-left: 135px;
  }
}
</style>
