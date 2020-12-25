<template>
  <div class="basic-form-list">
    <BaseFormQuery
      v-if="queryModels && queryModels.length > 0"
      :form-id="formId"
      :form-name="formName"
      :query-models="queryModels"
      :parent-field-models="parentFieldModels"
      @input="onQueryApply"
      @change="onQueryChanged"
    />
    <BaseActionsPane
      v-if="tableActions && tableActions.length > 0"
      :actions="tableActions"
      @input="onTableActionClick"
    />
    <BaseFormTable
      ref="selectionTable"
      :selectable="selectable"
      :data="tableInitData"
      :columns="tableColumns"
      :paging="tablePageInfo"
      @input="onSelectionChanged"
      @paging="onPageChanged"
    />
    <div v-if="selectable" class="basic-form-buttons">
      <el-button type="primary" size="mini" @click="onSelectoinSave()">
        {{ saveText || '添加' }}
      </el-button>
      <el-button type="info" style="float:right" size="mini" @click="onSelectoinCancel()">
        {{ cancelText || '关闭' }}
      </el-button>
    </div>
  </div>
</template>
<script>
import '@/styles/form.scss'
import BaseFormQuery from '@/components/BaseFormQuery'
import BaseFormTable from '@/components/BaseFormTable'
import BaseActionsPane from '@/components/BaseActionsPane'
export default {
  components: {
    BaseFormTable,
    BaseFormQuery,
    BaseActionsPane
  },
  props: {
    selectable: {
      type: Boolean,
      default: false
    },
    formId: {
      type: [String, Number],
      default: null
    },
    formName: {
      type: String,
      default: null
    },
    queryModels: {
      type: Array,
      default: null,
      required: false
    },
    tableActions: {
      type: Array,
      default: null,
      required: false
    },
    tableInitData: {
      type: Array,
      default: null,
      required: false
    },
    tableColumns: {
      type: Array,
      default: null,
      required: false
    },
    tablePageInfo: {
      type: Object,
      default: null,
      required: false
    },
    parentFieldModels: {
      type: Array,
      default: null
    },
    saveText: {
      type: String,
      default: ''
    },
    cancelText: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      selection: []
    }
  },
  watch: {
    tableInitData: {
      handler: function(data) {
        // console.log('基础表单选择器数据更新：', data)
      }
    }
  },
  methods: {
    /**
     * 查询重置的回调
     */
    onQueryReset() {
      this.$emit('queryReset', this.filterParams)
    },
    /**
     * 查询触发的回调
     */
    onQueryApply(params) {
      // console.log('查询的参数数据如下：', params)
      this.filterParams = params
      this.$emit('query-apply', params)
    },
    /**
     * 查询切换时的回调
     */
    onQueryChanged(changedQuery) {
      if (!changedQuery) {
        return
      }
      this.filterParams = {}
      this.$emit('queryChanged', changedQuery)
    },
    /**
     * 列表事件点击
     */
    onTableActionClick(e) {
      this.$emit('tableActionClick', e.name)
    },
    /**
     * 重置结果列表（回首页）
     */
    onPageChanged(page, limit) {
      this.$emit('pageChanged', page, limit, this.filterParams)
    },
    /**
     * 现中的表格记录变更回调
     */
    onSelectionChanged(rows) {
      this.selection = rows
    },
    /**
     * 保存选中的表格条目
     */
    onSelectoinSave() {
      this.$emit('input', this.selection)
    },
    /**
     * 取消选择操作
     */
    onSelectoinCancel() {
      this.$refs.selectionTable.clearSelection()
      this.$emit('cancel')
    }
  }
}
</script>
