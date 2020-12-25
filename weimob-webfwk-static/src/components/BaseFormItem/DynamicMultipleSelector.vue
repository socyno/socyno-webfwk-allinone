<template>
  <div class="dynamic-value-selector">
    <el-dialog v-if="editable" class="dynamic-value-dialog" title="编辑" :visible.sync="showEditionFormDialog" width="80%" height="600px" append-to-body>
      <BaseFormEditor
        v-if="showEditionFormDialog && currentTableFormModel"
        :form-id="formId"
        :form-name="formName"
        :form-data="curentEditionFormData"
        :form-model="currentTableFormModel"
        :parent-field-models="parentFieldModels"
        @input="editionItemSaved"
        @cancel="showEditionFormDialog = false"
      />
    </el-dialog>
    <el-dialog v-if="editable" class="dynamic-value-dialog" title="选择" :visible.sync="showFormDialog" width="80%" height="600px" append-to-body>
      <BaseFormSelector
        v-if="showFormDialog && fieldModel && availableQueryModels"
        ref="filter"
        :selectable="true"
        :form-id="formId"
        :form-name="formName"
        :query-models="availableQueryModels"
        :table-columns="currentTableColumns"
        :table-init-data="availableTableData"
        :table-page-info="availableTablePager"
        :parent-field-models="parentFieldModels"
        @input="saveSelectedRows"
        @cancel="showFormDialog = false"
        @query-apply="onQueryApply"
      />
    </el-dialog>
    <div v-if="editable && fieldModel && !fieldModel.readonly">
      <el-button v-if="fieldModel && availableQueryModels" type="primary" size="mini" @click="onSelect()">
        选择
      </el-button>
      <DynamicSingleSelector
        v-else
        ref="selector"
        :form-id="formId"
        :form-name="formName"
        :field-model="fieldModel"
        :multiple="simpleMultipleMode"
        :parent-field-models="parentFieldModels"
        :option-is-visible="checkOptionIsVisible"
        :placeholder="placeholder"
        @clear="onSelectorCleared"
        @change="onSelectorChanged"
      />
    </div>
    <div v-else-if="simpleMultipleMode">
      {{ getFieldValueDisplay(fieldModel, fieldModel.value) }}
    </div>
    <el-table
      v-if="!simpleMultipleMode"
      :key="fieldModel.key"
      :data="value"
      style="width: 99%"
      @selection-change="onValueSelectionChanged"
    >
      <el-table-column
        v-if="rowExpandable"
        type="expand"
        width="30"
      >
        <template slot-scope="props">
          <BaseFormContent
            v-if="currentTableFormModel"
            :editable="false"
            :form-name="formName"
            :form-model="currentTableFormModel"
            :default-data="props.row"
          />
        </template>
      </el-table-column>
      <el-table-column
        v-if="rowSelectable"
        type="selection"
        width="30"
        :selectable="rowItemSelectable"
      />
      <el-table-column
        v-if="rowShowIndex"
        type="index"
        :index="1"
        width="40"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        v-for="(column, idx) in currentTableColumns"
        :key="`${column.key}-${idx}`"
        :label="column.title"
        :prop="column.key"
        :show-overflow-tooltip="true"
        :width="column.listWidth > 0 ? column.listWidth : 0"
      >
        <template v-slot:default="{ row, $index }">
          <TemplateConfig
            v-if="column.beforeTemplate"
            :template="column.beforeTemplate"
            :field-model="column"
            :form-fields="currentTableColumns"
            :form-model="currentTableFormModel"
            :form-data="row"
            :data-index="$index"
            :form-editable="editable"
          />
          <TemplateConfig
            v-if="column.template"
            :template="column.template"
            :field-model="column"
            :form-fields="currentTableColumns"
            :form-model="currentTableFormModel"
            :form-data="row"
            :data-index="$index"
            :form-editable="editable"
          />
          <div v-else>
            {{ getFieldValueDisplay(column, row[column.key]) }}
          </div>
          <TemplateConfig
            v-if="column.afterTemplate"
            :template="column.afterTemplate"
            :field-model="column"
            :form-data="row"
            :form-fields="currentTableColumns"
            :form-model="currentTableFormModel"
            :data-index="$index"
            :form-editable="editable"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="editable" label="操作" width="150px">
        <template slot-scope="scope">
          <el-button
            v-if="rowItemDeletable(scope.row, scope.$index)"
            size="mini"
            type="danger"
            @click="onDelete(scope.$index, scope.row)"
          >
            删除
          </el-button>
          <el-button
            v-if="rowItemEditable(scope.row, scope.$index)"
            size="mini"
            type="primary"
            @click="onEdit(scope.$index, scope.row)"
          >
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import tool from '@/utils/tools'
import FormApi from '@/apis/formApi'
import BaseFormEditor from '@/components/BaseFormEditor'
import DynamicSingleSelector from './DynamicSingleSelector'
import BaseFormSelector from '@/components/BaseFormSelector'
import TemplateConfig from '@/components/BaseFormItem/TemplateConfig'
import { getFieldValueDisplay, getVisibleFieldModels, FORM_FIELD_OPTIONS } from '@/utils/formUtils'
export default {
  components: {
    TemplateConfig,
    BaseFormEditor,
    BaseFormSelector,
    DynamicSingleSelector,
    BaseFormContent: () => import('@/components/BaseFormContent')
  },
  props: {
    value: {
      type: Array,
      default: function() {
        return []
      }
    },
    editable: {
      type: Boolean,
      default: false
    },
    fieldModel: {
      type: Object,
      required: true
    },
    formId: {
      type: [String, Number],
      default: null
    },
    formName: {
      type: String,
      required: true
    },
    parentFieldModels: {
      type: Array,
      default: null
    },
    placeholder: {
      type: String,
      default: null
    },
    rowShowIndex: {
      type: Boolean,
      default: false
    },
    rowExpandable: {
      type: Boolean,
      default: true
    },
    rowSelectable: {
      type: [String, Function],
      default: ''
    },
    rowEditable: {
      type: [String, Function],
      default: 'yes'
    },
    rowDeletable: {
      type: [String, Function],
      default: 'yes'
    }
  },
  data() {
    return {
      showFormDialog: false,
      currentTableColumns: [],
      currentTableFormModel: null,
      availableTableData: [],
      availableTablePager: {},
      availableQueryModels: null,
      showEditionFormDialog: false,
      curentEditionFormData: {}
    }
  },
  computed: {
    /**
     * 非多列显示且无自定义选择器的场景下,
     * 使用 el-select 的标准多选控件即可
     */
    simpleMultipleMode() {
      return !this.fieldModel.dynamicSelectedEditable && !this.availableQueryModels && this.currentTableColumns.length === 1
    }
  },
  watch: {
    fieldModel: {
      immediate: true,
      handler: function(fieldModel) {
        if (!fieldModel) {
          return
        }
        this.availableQueryModels = null
        if (fieldModel.dynamicFilterFormClass) {
          this.availableQueryModels = [{
            formClass: fieldModel.dynamicFilterFormClass
          }]
        }
        // console.log(fieldModel)
        this.currentTableFormModel = fieldModel.items
        this.currentTableColumns = getVisibleFieldModels(
          fieldModel.items,
          FORM_FIELD_OPTIONS.ListFirst |
            FORM_FIELD_OPTIONS.OrderUndefinedExcluded
        )
      }
    },
    formName: {
      immediate: true,
      handler: function(formName) {
        if (tool.isBlank(formName)) {
          this.formApi = null
          return
        }
        this.formApi = new FormApi(formName)
      }
    }
  },
  methods: {
    /**
     * 添加数据项
     */
    onSelect() {
      this.showFormDialog = true
    },
    /**
     * 删除数据项
     * @param {Object} params
     */
    onDelete(index, item) {
      this.value.splice(index, 1)
      this.$emit('input', this.value)
    },
    /**
     * 编辑数据项
     * @param {Object} params
     */
    onEdit(index, item) {
      if (!item) {
        return
      }
      item['__new_index'] = tool.genUuid()
      this.curentEditionFormData = item
      this.showEditionFormDialog = true
    },
    /**
     * 完成选中项的编辑并保存
     */
    editionItemSaved(params) {
      if (!params || !tool.isPlainObject(params)) {
        return
      }
      this.showEditionFormDialog = false
      if (!tool.isArray(this.value)) {
        return
      }
      var newIndex = tool.inArray(this.curentEditionFormData, this.value, function(a, b) {
        return a['__new_index'] === b['__new_index']
      })
      if (newIndex < 0) {
        return
      }
      this.value.splice(newIndex, 1, params)
      this.$emit('input', this.value)
    },
    /**
     * 新的值被选中
     */
    saveSelectedRows(rows) {
      this.showFormDialog = false
      if (!tool.isArray(rows)) {
        return
      }
      if (!tool.isArray(this.value)) {
        this.value = []
      }
      for (const row of rows) {
        this.value.push(row)
      }
      this.$emit('input', this.value)
    },
    /**
     * 简单选择项被清空时的回调, 只有的单列多选时才需要处理
     */
    onSelectorCleared() {
      if (this.simpleMultipleMode) {
        this.value = []
        this.$emit('input', this.value)
      }
    },
    /**
     * 简单选择项被选中时
     */
    onSelectorChanged(option) {
      if (!tool.isArray(this.value)) {
        this.value = []
      }
      if (!tool.isArray(option)) {
        option = [option]
      }
      if (this.simpleMultipleMode) {
        this.value = option
      } else {
        for (var o of option) {
          this.value.push(o)
        }
        /**
         * 选中后多列模式下强制失焦,以确保下次下拉可重建选项
         */
        this.$refs.selector.blur()
      }
      this.$emit('input', this.value)
    },
    /**
     * 当选择项发生变化时触发
     */
    onValueSelectionChanged(rows) {
      for (var v of this.value) {
        v['__selected__'] = false
      }
      if (rows) {
        for (var r of rows) {
          r['__selected__'] = true
        }
      }
    },
    /**
     * 格式化字段的显示文本
     * @param {Object} fieldModel
     * @param {Object} fieldValue
     */
    getFieldValueDisplay(fieldModel, fieldValue) {
      // console.log(fieldModel, fieldValue)
      return getFieldValueDisplay(fieldModel, fieldValue)
    },
    /**
     * 可选列表数据查询
     */
    onQueryApply(query) {
      // console.log('动态可选项复杂检索参数如下：', query)
      if (!this.formApi) {
        throw new Error('当前的表单还未设置，请确认流程单上下文正确设置')
      }
      var params = {}
      if (this.parentFieldModels && tool.isArray(this.parentFieldModels)) {
        for (const field of this.parentFieldModels) {
          params[field.key] = field.value
        }
      }
      var keyword = ''
      if (tool.isPlainObject(query)) {
        Object.assign(params, query)
      } else {
        keyword = query
      }
      var loading = this.$loading({ lock: true, text: 'Loading' })
      this.formApi.loadFormFieldOptionsWithQuery(
        this.fieldModel.fieldTypeKey,
        keyword,
        this.formId, params
      ).then(res => {
        this.availableTableData = res.data ? res.data : []
        // console.log('动态可选项复杂检索结果如下', res.data)
      }).finally(res => {
        loading.close()
      })
    },
    /**
     * 过滤已经选择的待选项
     */
    checkOptionIsVisible(option) {
      if (this.simpleMultipleMode) {
        return true
      }
      if (!option || !option.hasOwnProperty('optionValue')) {
        return true
      }
      if (!tool.isArray(this.value) || this.value.length <= 0) {
        return true
      }
      return tool.inArray(option, this.value, function(a, b) {
        return a === b || tool.stringify(a.optionValue) === tool.stringify(b.optionValue)
      }) < 0
    },

    /**
     * 是否允许值可编辑
     */
    rowItemEditable(row, index) {
      if (!this.fieldModel || !this.fieldModel.dynamicSelectedEditable) {
        return false
      }
      if (tool.isBoolean(this.rowEditable)) {
        return this.rowEditable
      }
      if (tool.isFunction(this.rowEditable)) {
        return this.rowEditable(row, index)
      }
      var rowEditable = tool.trim(this.rowEditable)
      if (rowEditable === '' || tool.inArray(rowEditable.toLowerCase(),
        ['true', 'yes', 'on', 't', 'y', '0']) >= 0) {
        return true
      }
      if (tool.inArray(rowEditable.toLowerCase(), ['false', 'off', 'no', 'f', 'n', '0']) >= 0) {
        return false
      }
      try {
        rowEditable = new Function('$row', '$index', rowEditable)
        return rowEditable(row, index)
      } catch (e) {
        // eslint-disable-next-line
        console.error('字段的可编辑属性定义的函数无法解析：', e)
        return false
      }
    },

    /**
     * 是否允许值可选择
     */
    rowItemSelectable(row, index) {
      if (tool.isBoolean(this.rowSelectable)) {
        return this.rowSelectable ? 1 : 0
      }
      if (tool.isFunction(this.rowSelectable)) {
        return this.rowSelectable(row, index) ? 1 : 0
      }
      var rowSelectable = tool.trim(this.rowSelectable)
      if (rowSelectable === '' || tool.inArray(rowSelectable.toLowerCase(),
        ['false', 'off', 'no', 'f', 'n', '0']) >= 0) {
        return 0
      }
      if (tool.inArray(rowSelectable.toLowerCase(), ['true', 'yes', 'on', 't', 'y', '0']) >= 0) {
        return 1
      }
      try {
        rowSelectable = new Function('$row', '$index', rowSelectable)
        return rowSelectable(row, index) ? 1 : 0
      } catch (e) {
        // eslint-disable-next-line
        console.error('字段的可选择属性定义的函数无法解析：', e)
        return 0
      }
    },

    /**
     * 是否允许值可删除
     */
    rowItemDeletable(row, index) {
      if (!this.fieldModel) {
        return false
      }
      if (tool.isBoolean(this.rowDeletable)) {
        return this.rowDeletable
      }
      if (tool.isFunction(this.rowDeletable)) {
        return this.rowDeletable(row, index)
      }
      var rowDeletable = tool.trim(this.rowDeletable)
      if (rowDeletable === '' || tool.inArray(rowDeletable.toLowerCase(),
        ['true', 'yes', 'on', 't', 'y', '0']) >= 0) {
        return true
      }
      if (tool.inArray(rowDeletable.toLowerCase(), ['false', 'off', 'no', 'f', 'n', '0']) >= 0) {
        return false
      }
      try {
        rowDeletable = new Function('$row', '$index', rowDeletable)
        return rowDeletable(row, index)
      } catch (e) {
        // eslint-disable-next-line
        console.error('字段的可删除属性定义的函数无法解析：', e)
        return false
      }
    }
  }
}
</script>
<style lang="scss">
.dynamic-value-creation {

}
.dynamic-value-dialog {

}
</style>
