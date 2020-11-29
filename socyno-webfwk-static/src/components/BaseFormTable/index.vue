<template>
  <div class="basic-form-list">
    <el-table
      ref="eltable"
      :data="data"
      :default-expand-all="expandAll"
      style="width: 100%"
      @expand-change="onRowExpandChanged"
      @selection-change="onSelectionChanged"
    >
      <el-table-column
        v-if="selectable"
        type="selection"
        width="50"
      />
      <el-table-column type="expand">
        <template v-slot:default="scope">
          <slot name="expand" :formName="formName" :formModel="formModel" :rowData="scope">
            <BaseFormContent
              v-if="formModel"
              :editable="false"
              :form-name="formName"
              :form-model="formModel"
              :default-data="scope.row"
              :show-all-fields="showAllFields"
            />
          </slot>
        </template>
      </el-table-column>
      <el-table-column
        v-for="(field, idx) in fieldModels"
        :key="idx"
        :label="field.title"
        :prop="field.key"
        :resizable="columnResizeable"
        :show-overflow-tooltip="showOverflowTooltip"
        :width="field.listWidth > 0 ? field.listWidth : 0"
      >
        <template v-slot:default="{ row, column, $index }">
          <TemplateConfig
            v-if="field.beforeTemplate"
            :template="field.beforeTemplate"
            :field-model="field"
            :form-fields="fieldModels"
            :form-model="formModel"
            :form-data="row"
            :data-index="$index"
          />
          <TemplateConfig
            v-if="field.template"
            :template="field.template"
            :field-model="field"
            :form-fields="fieldModels"
            :form-model="formModel"
            :form-data="row"
            :data-index="$index"
          />
          <div v-else>
            <i :class="getFieldValueIcon(field, row[field.key])" :style="getFieldValueStyle(field, row[field.key])">
              {{ getFieldValueDisplay(field, row[field.key]) }}
            </i>
          </div>
          <TemplateConfig
            v-if="field.afterTemplate"
            :template="field.afterTemplate"
            :field-model="field"
            :form-fields="fieldModels"
            :form-model="formModel"
            :form-data="row"
            :data-index="$index"
          />
        </template>
      </el-table-column>
      <el-table-column
        v-if="rowActions && rowActions.length > 0"
        class="basic-form-list-actpane"
        label="操作"
        :width="actionsColumnWidth > 0 ? actionsColumnWidth : 0"
        :resizeable="columnResizeable"
      >
        <template slot-scope="scope">
          <slot name="operations" :scope="scope" />
          <slot v-for="(action) in rowActions">
            <el-button
              v-show="checkActionVisible(action, scope.row, scope.$index)"
              :type="action.type || 'text'"
              :size="action.size || 'small'"
              :class="action.class || 'basic-form-list-action'"
              @click="onRowActionClick(action, scope.row, scope.$index)"
            >
              {{ action.display || action.text || action.name || action.key }}
            </el-button>
          </slot>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="pageInfo"
      class="basic-form-list-pagesbar"
      :current-page="pageInfo.page"
      :page-sizes="pageInfo.sizes"
      :page-size="pageInfo.limit"
      :layout="pageInfo.layout"
      :total="pageInfo.total"
      @size-change="onSizeChanged"
      @current-change="onPageChanged"
    />
  </div>
</template>
<script>

import '@/styles/form.scss'
import tool from '@/utils/tools'
import {
  parseFormClass,
  getFieldValueDisplay,
  getFieldValueDisplayIcon,
  getFieldValueDisplayStyle,
  getVisibleFieldModels,
  FORM_FIELD_OPTIONS
} from '@/utils/formUtils'
import TemplateConfig from '@/components/BaseFormItem/TemplateConfig'
export default {
  components: {
    TemplateConfig,
    BaseFormContent: () => import('../BaseFormContent')
  },
  props: {
    data: {
      type: Array,
      default: function() {
        return []
      }
    },
    columns: {
      type: [Array, Object],
      default: function() {
        return []
      }
    },
    paging: {
      type: Object,
      default: null
    },
    /**
     * 是否支持行选择
     */
    selectable: {
      type: Boolean,
      default: false
    },
    /**
     * 是否默认展开所有行
     */
    expandAll: {
      type: Boolean,
      default: false
    },
    rowActions: {
      type: Array,
      default: function() {
        return []
      }
    },
    actionsColumnWidth: {
      type: Number,
      default: 0
    },
    formName: {
      type: String,
      default: null
    },
    showOverflowTooltip: {
      type: Boolean,
      default: true
    },
    columnResizeable: {
      type: Boolean,
      default: true
    },
    /**
     * 是否在展开的表单中显示所有字段
     * （包括被明确标记隐藏的字段）
     */
    showAllFields: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      pageInfo: null,
      formModel: null,
      fieldModels: []
    }
  },
  watch: {
    columns: {
      immediate: true,
      handler: function(columns) {
        this.formModel = null
        this.fieldModels = []
        // console.log('表格视图模型 => ', columns)
        if (tool.isArray(columns)) {
          this.fieldModels = columns
        } else if (tool.isPlainObject(columns)) {
          this.formModel = parseFormClass(columns)
          this.fieldModels = getVisibleFieldModels(
            columns,
            FORM_FIELD_OPTIONS.ListFirst |
                FORM_FIELD_OPTIONS.OrderUndefinedExcluded
          )
          // console.log('流程表单的列表字段模型定义解析结果如下：', this.fieldModels)
        }
      }
    }
  },
  mounted() {
    if (this.paging) {
      this.setPaging(this.paging)
    }
  },
  methods: {
    /**
     * 计算字段值的显示文本
     */
    getFieldValueDisplay(fieldModel, fieldValue) {
      // console.log(fieldModel.key, fieldValue, fieldModel)
      return getFieldValueDisplay(fieldModel, fieldValue)
    },

    /**
     * 计算字段值的显示样式
     */
    getFieldValueStyle(fieldModel, fieldValue) {
      return getFieldValueDisplayStyle(fieldModel, fieldValue)
    },

    /**
     * 计算字段值的前置图标
     */
    getFieldValueIcon(fieldModel, fieldValue) {
      return getFieldValueDisplayIcon(fieldModel, fieldValue)
    },

    /**
     * 翻页时的回调
     */
    onPageChanged(page) {
      this.pageInfo.page = page
      this.$emit('paging', page, this.pageInfo.limit)
    },

    /**
     * 页面大小调整时的回调
     */
    onSizeChanged(limit) {
      this.pageInfo.limit = limit
      this.$emit('paging', this.pageInfo.page, limit)
    },

    /**
     * 选中项发生变化时
     * @param {Object} rows
     */
    onSelectionChanged(rows) {
      this.$emit('input', rows)
    },

    /**
     * 行展开或收缩时的回调
     * @param {Object} rows
     */
    onRowExpandChanged(rows, expanedRows) {
      this.$emit('expand-change', rows, expanedRows)
    },

    /**
     * 清除选中的行数据
     */
    clearSelection() {
      this.$refs.eltable.clearSelection()
    },

    /**
     * 行操作按钮触发回调
     */
    onRowActionClick(action, rowData, rowIndex) {
      this.$emit('row-action-click', action, rowData, rowIndex)
    },

    /**
     * 确认当前的操作按钮是否显示
     * 如果父组件中定义有 checkActionVisible 方法，则会交由父组件来决定是否显示。
     * @param {Object} action
     * @param {Object} rowData
     * @param {Object} rowIndex
     */
    checkActionVisible(action, rowData, rowIndex) {
      if (!tool.isFunction(this.$parent.checkRowActionVisible)) {
        return true
      }
      return this.$parent.checkRowActionVisible(action, rowData, rowIndex)
    },

    /**
     * 当分页信息发生变化时触发器
     */
    setPaging(paging) {
      // console.log('设置分页信息 => ', paging)
      if (!paging || !tool.isPlainObject(paging)) {
        this.pageInfo = null
        return
      }
      this.pageInfo = {
        page: 1,
        limit: 20,
        sizes: [10, 20, 30, 50, 100],
        layout: 'prev, next',
        total: -1
      }
      var limit
      if (!isNaN(limit = parseInt(paging.limit)) && limit > 0) {
        this.pageInfo.limit = limit
      }
      var page
      if (!isNaN(page = parseInt(paging.page)) && page > 0) {
        this.pageInfo.page = page
      }
      var total
      if (!isNaN(total = parseInt(paging.total)) && total >= 0) {
        this.pageInfo.total = total
        this.pageInfo.layout = 'total, sizes, prev, pager, next, jumper'
      }
      if (tool.isArray(paging.sizes)) {
        this.pageInfo.sizes = paging.sizes
      }
    },

    /**
     * call toggleRowExpansion on table
     */
    toggleRowExpansion(row, expanded) {
      this.$refs.eltable.toggleRowExpansion(row, expanded)
    }
  }
}
</script>

<style lang="scss">
  .basic-form-list {
    .el-table__row {
      .el-button {
        padding: 2px !important;
        margin: 2px !important;
      }
    }
    .el-table__expanded-cell {
      padding: 0px !important;
    }
    .basic-form-list-actpane {
      min-height: 20px;
    }
    .basic-form-list-pagesbar {
      display: block;
    }
  }

</style>
