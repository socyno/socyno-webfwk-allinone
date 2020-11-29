<template>
  <div class="dynamic-value-creation">
    <el-dialog v-if="editable" class="dynamic-value-dialog" title="添加" :visible.sync="showFormDialog" width="80%" height="600px" append-to-body>
      <BaseFormEditor
        v-if="showFormDialog && fieldModel && curentCreationFormModel"
        :form-id="formId"
        :form-name="formName"
        :form-data="curentCreationFormData"
        :form-model="curentCreationFormModel"
        :parent-field-models="parentFieldModels"
        @input="listItemSaved"
        @cancel="showFormDialog = false"
      />
    </el-dialog>
    <el-button v-if="editable" type="primary" size="mini" @click="onCreate()">
      添加
    </el-button>
    <el-table
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
            v-if="curentCreationFormModel"
            :editable="false"
            :form-name="formName"
            :form-model="curentCreationFormModel"
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
        v-for="(column, idx) in curentCreationFormColumns"
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
            :form-fields="curentCreationFormColumns"
            :form-model="curentCreationFormModel"
            :form-data="row"
            :data-index="$index"
            :form-editable="editable"
          />
          <TemplateConfig
            v-if="column.template"
            :template="column.template"
            :field-model="column"
            :form-fields="curentCreationFormColumns"
            :form-model="curentCreationFormModel"
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
            :form-fields="curentCreationFormColumns"
            :form-model="curentCreationFormModel"
            :form-data="row"
            :data-index="$index"
            :form-editable="editable"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="editable" label="操作">
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
import BaseFormEditor from '@/components/BaseFormEditor'
import TemplateConfig from '@/components/BaseFormItem/TemplateConfig'
import { parseFormClass, getFieldValueDisplay, getVisibleFieldModels, FORM_FIELD_OPTIONS } from '@/utils/formUtils'
export default {
  name: 'DynamicValueCreator',
  components: {
    BaseFormEditor,
    TemplateConfig,
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
      curentCreationFormData: {},
      curentCreationFormModel: {},
      curentCreationFormColumns: []
    }
  },
  watch: {
    fieldModel: {
      immediate: true,
      handler: function(newModel, oldModel) {
        this.curentCreationFormModel = null
        this.curentCreationFormColumns = []
        if (!newModel) {
          return
        }
        if (newModel.listItemCreationFormClass) {
          this.curentCreationFormModel = parseFormClass(
            newModel.listItemCreationFormClass
          )
          this.curentCreationFormColumns = getVisibleFieldModels(
            this.curentCreationFormModel,
            FORM_FIELD_OPTIONS.ListFirst |
              FORM_FIELD_OPTIONS.OrderUndefinedExcluded
          )
        }
      }
    }
  },
  methods: {
    /**
     * 添加数据项
     */
    onCreate() {
      this.curentCreationFormData = {}
      this.showFormDialog = true
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
      this.curentCreationFormData = item
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
     * 新的值被创建或编辑
     */
    listItemSaved(params) {
      // console.log('动态添加项存储：', params)
      if (!params || !tool.isPlainObject(params)) {
        return
      }
      this.showFormDialog = false
      if (!tool.isArray(this.value)) {
        this.value = []
      }
      if (this.curentCreationFormData && this.curentCreationFormData['__new_index']) {
        var newIndex = tool.inArray(this.curentCreationFormData, this.value, function(a, b) {
          return a['__new_index'] === b['__new_index']
        })
        if (newIndex >= 0) {
          this.value.splice(newIndex, 1)
        }
      }
      this.value.push(params)
      // console.log('动态添加项存储添加完成：', this.value)
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
      return getFieldValueDisplay(fieldModel, fieldValue)
    },

    /**
     * 是否允许值可编辑
     */
    rowItemEditable(row, index) {
      if (!this.fieldModel) {
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
