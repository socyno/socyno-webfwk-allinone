<template>
  <BaseFormContentBase
    v-if="!renderTimer"
    ref="baseFormContent"
    :editable="editable"
    :collapsible="collapsible"
    :field-models="fieldModels"
  >
    <template v-slot:content="{ field }">
      <!-- 前置模板渲染 -->
      <TemplateConfig
        v-if="field.beforeTemplate"
        :template="field.beforeTemplate"
        :field-model="field"
        :form-fields="fieldModels"
        :form-model="formModel"
        :form-data="defaultData"
        :form-editable="editable"
      />
      <!-- 内嵌的表单 -->
      <div v-if="field.comType === 'innerForm'">
        <BaseFormContent
          v-for="(vitem, vindex) in field.value"
          :ref="`InnerForm:${field.key}`"
          :key="`${field.key}-${vindex}`"
          :form-id="formId"
          :form-name="formName"
          :form-model="field.items"
          :parent-field="field"
          :parent-field-models="parentFieldModelsConcat()"
          :default-data="vitem"
          :editable="editable && innerEditable && !field.readonly"
          :collapsible="innerCollapsible"
          :show-form-error="false"
        />
      </div>
      <!-- 长文本本显示 -->
      <div v-else-if="field.comType === 'areaText'">
        <el-input
          v-if="editable && !field.readonly"
          v-model="field.value"
          size="mini"
          type="textarea"
          :rows="field.placerows > 0 ? field.placerows : 3"
          :placeholder="getPlaceholder(field)"
          @input="$forceUpdate()"
        />
        <TemplateConfig
          v-else-if="field.template"
          :template="field.template"
          :field-model="field"
          :form-fields="fieldModels"
          :form-model="formModel"
          :form-data="defaultData"
          :form-editable="editable"
        />
        <el-input
          v-else-if="field.readonlyAsEditor === 'yes'"
          v-model="field.value"
          size="mini"
          type="textarea"
          :readonly="true"
          :rows="field.placerows > 0 ? field.placerows : 3"
        />
        <pre v-else>{{ getTextDisplay(field) }}</pre>
      </div>
      <!-- html -->
      <div v-else-if="field.comType === 'textHtml'">
        <el-input
          v-if="editable && !field.readonly"
          v-model="field.value"
          size="mini"
          type="textarea"
          :rows="field.placerows > 0 ? field.placerows : 3"
          :placeholder="getPlaceholder(field)"
          @input="$forceUpdate()"
        />
        <TemplateConfig
          v-else-if="field.template"
          :template="field.template"
          :field-model="field"
          :form-fields="fieldModels"
          :form-model="formModel"
          :form-data="defaultData"
          :form-editable="editable"
        />
        <!-- eslint-disable-next-line vue/no-v-html -->
        <div v-else v-html="getTextDisplay(field)" />
      </div>
      <!-- 文件上传 -->
      <div v-else-if="field.comType === 'file'">
        <FileUploader
          v-model="field.value"
          :dragable="!field.placerows || field.placerows > 1"
          :editable="editable && !field.readonly"
          :field-model="field"
          :form-id="formId"
          :form-name="formName"
        />
      </div>
      <!-- 动态下拉多选框场景 -->
      <div v-else-if="field.comType === 'tableView'">
        <TemplateConfig
          v-if="field.template && (!editable || field.readonly)"
          :template="field.template"
          :field-model="field"
          :form-fields="fieldModels"
          :form-model="formModel"
          :form-data="defaultData"
          :form-editable="editable"
        />
        <DynamicMultipleCreator
          v-else-if="field.listItemCreationFormClass"
          v-model="field.value"
          :form-id="formId"
          :form-name="formName"
          :editable="editable && !field.readonly"
          :field-model="field"
          :row-editable="field.tableViewEditable"
          :row-deletable="field.tableViewDeletable"
          :row-selectable="field.tableViewSelectable"
          :row-show-index="field.tableViewShowIndex === 'yes'"
          :row-expandable="field.tableViewExpandable === 'yes'"
          :parent-field-models="parentFieldModelsConcat()"
        />
        <DynamicMultipleSelector
          v-else
          v-model="field.value"
          :form-id="formId"
          :form-name="formName"
          :editable="editable && !field.readonly"
          :field-model="field"
          :row-editable="field.tableViewEditable"
          :row-deletable="field.tableViewDeletable"
          :row-selectable="field.tableViewSelectable"
          :row-show-index="field.tableViewShowIndex === 'yes'"
          :row-expandable="field.tableViewExpandable === 'yes'"
          :parent-field-models="parentFieldModelsConcat()"
          :placeholder="getPlaceholder(field)"
        />
      </div>
      <!-- 动态下拉单选框场景 -->
      <div v-else-if="field.comType === 'dynamicSelect'">
        <DynamicSingleSelector
          v-if="editable && !field.readonly"
          v-model="field.value"
          :form-id="formId"
          :form-name="formName"
          :field-model="field"
          :parent-field-models="parentFieldModelsConcat()"
          :placeholder="getPlaceholder(field)"
        />
        <DynamicSingleSelector
          v-else-if="field.readonlyAsEditor === 'yes'"
          v-model="field.value"
          :form-id="formId"
          :editable="false"
          :form-name="formName"
          :field-model="field"
        />
        <span v-else>
          <i :class="getFieldValueIcon(field)" :style="getFieldValueStyle(field)">
            {{ getTextDisplay(field) }}
          </i>
        </span>
      </div>
      <!-- 操作按钮 -->
      <div v-else-if="field.comType === 'button'">
        <el-button :size="field.styleSize || 'mini'" :type="field.styleType || 'text'" @click="onFieldButtonClick(field)">
          {{ field.title }}
        </el-button>
      </div>
      <!-- Bar Simple 简单柱状图-->
      <div v-else-if="field.comType === 'chartBarSimple'">
        <ChartBarSimple
          :chart-data="field.value"
          :chart-properties="field.chartProperties"
          :form-id="formId"
          :form-name="formName"
          :field-model="field"
        />
      </div>
      <!-- Lime Simple 简单折线图-->
      <div v-else-if="field.comType === 'chartLineSimple'">
        <ChartLineSimple
          :chart-data="field.value"
          :chart-properties="field.chartProperties"
          :form-id="formId"
          :form-name="formName"
          :field-model="field"
        />
      </div>
      <!-- Pie Simple 简单饼图-->
      <div v-else-if="field.comType === 'chartPieSimple'">
        <ChartPieSimple
          :chart-data="field.value"
          :chart-properties="field.chartProperties"
          :form-id="formId"
          :form-name="formName"
          :field-model="field"
        />
      </div>
      <!-- 常规短文本字段 -->
      <div v-else>
        <TemplateConfig
          v-if="(!editable || field.readonly) && field.template"
          :template="field.template"
          :field-model="field"
          :form-fields="fieldModels"
          :form-model="formModel"
          :form-data="defaultData"
          :form-editable="editable"
        />
        <el-input
          v-else-if="(!editable || field.readonly) && field.readonlyAsEditor === 'yes'"
          v-model="field.value"
          size="mini"
          type="text"
          :readonly="true"
        />
        <el-tooltip
          v-else-if="(!editable || field.readonly)"
          effect="dark"
          :content="getTextDisplay(field)"
          placement="left-start"
        >
          <span>
            <i :class="getFieldValueIcon(field)" :style="getFieldValueStyle(field)">
              {{ getTextDisplay(field) }}
            </i>
          </span>
        </el-tooltip>
        <!-- 密码框 -->
        <el-input
          v-else-if="field.comType === 'password'"
          v-model="field.value"
          size="mini"
          type="password"
          :placeholder="getPlaceholder(field)"
          @input="$forceUpdate()"
        />
        <!-- 静态单选 -->
        <el-select
          v-else-if="field.comType === 'select'"
          v-model="field.value"
          size="mini"
          :placeholder="getPlaceholder(field)"
          :clearable="true"
          @change="$forceUpdate()"
        >
          <slot v-for="(opt,optidx) in field.staticOptions">
            <div v-if="opt.group && (optidx >= 1 ? field.staticOptions[optidx - 1].group != opt.group : true)" :key="optidx + 1000" class="common-option-group">
              {{ opt.optionGroup || opt.group }}
            </div>
            <el-option :key="optidx" :label="opt.display" :value="opt.value" />
          </slot>
        </el-select>
        <!-- 静态多选 -->
        <el-checkbox-group
          v-else-if="field.comType === 'checkbox'"
          v-model="field.value"
          size="mini"
          @change="$forceUpdate()"
        >
          <el-checkbox v-for="(opt, optidx) in field.staticOptions" :key="optidx" :label="opt.optionValue" :value="opt.optionValue">
            {{ opt.optionDisplay }}
          </el-checkbox>
        </el-checkbox-group>
        <!-- 开关选择 -->
        <el-switch
          v-else-if="field.comType === 'switch'"
          v-model="field.value"
          size="mini"
          @change="$forceUpdate()"
        />
        <!-- 日期选择 -->
        <Datetime
          v-else-if="field.comType === 'DateOnly'"
          v-model="field.value"
          type="date"
          input-format="YYYY-MM-DD"
          :placeholder="getPlaceholder(field)"
          :i18n="{ok:'确定', cancel:'取消'}"
          :auto-close="true"
          @input="$forceUpdate()"
        />
        <!-- 时间选择 -->
        <Datetime
          v-else-if="field.comType === 'TimeOnly'"
          v-model="field.value"
          type="time"
          input-format="HH:mm"
          :placeholder="getPlaceholder(field)"
          :i18n="{ok:'确定', cancel:'取消'}"
          @input="$forceUpdate()"
        />
        <!-- 日期和时间选择 -->
        <Datetime
          v-else-if="field.comType === 'DateTime'"
          v-model="field.value"
          type="datetime"
          input-format="YYYY-MM-DD HH:mm"
          :placeholder="getPlaceholder(field)"
          :i18n="{ok:'确定', cancel:'取消'}"
          :auto-continue="true"
          :auto-close="false"
          @input="$forceUpdate()"
        />
        <!-- 分隔线: 分割线无需绘制，在 label 中体现 -->
        <div v-else-if="field.comType === 'separator'" />
        <!-- 简单文本框 -->
        <el-input
          v-else
          v-model="field.value"
          size="mini"
          :placeholder="getPlaceholder(field)"
          :type="field.type === 'integer' ? 'number' : 'text'"
          @input="$forceUpdate()"
        />
      </div>
      <!-- 后置模板渲染 -->
      <TemplateConfig
        v-if="field.afterTemplate"
        :template="field.afterTemplate"
        :field-model="field"
        :form-fields="fieldModels"
        :form-model="formModel"
        :form-data="defaultData"
        :form-editable="editable"
      />
    </template>
    <template v-slot:after>
      <div class="form-content-buttons">
        <pre v-if="showFormError" style="padding:0; margin:0;color:orangered;" v-text="formErrorMassage" />
        <div v-if="actions && actions.length > 0">
          <el-button v-for="(item, index) in actions" :key="`form-action-${index}`" :type="item.type || 'primary'" :size="item.size || 'mini'" @click="$emit('actions', item)">
            {{ item.name || item.title || item.dispaly }}
          </el-button>
        </div>
      </div>
    </template>
  </BaseFormContentBase>
</template>
<script>
import tool from '@/utils/tools'
import { Datetime } from 'vue-datetime'
import FileUploader from '@/components/BaseFormItem/FileUploader'
import TemplateConfig from '@/components/BaseFormItem/TemplateConfig'
import DynamicSingleSelector from '@/components/BaseFormItem/DynamicSingleSelector'
import DynamicMultipleCreator from '@/components/BaseFormItem/DynamicMultipleCreator'
import DynamicMultipleSelector from '@/components/BaseFormItem/DynamicMultipleSelector'
import ChartBarSimple from '@/components/BaseFormItem/Echarts/ChartBarSimple'
import ChartLineSimple from '@/components/BaseFormItem/Echarts/ChartLineSimple'
import ChartPieSimple from '@/components/BaseFormItem/Echarts/ChartPieSimple'
import {
  setFieldDefaultValue,
  getFieldValueDisplay,
  getFieldValueDisplayIcon,
  getFieldValueDisplayStyle,
  getVisibleFieldModels,
  FORM_FIELD_OPTIONS
} from '@/utils/formUtils'
export default {
  name: 'BaseFormContent',
  components: {
    FileUploader,
    TemplateConfig,
    Datetime,
    DynamicSingleSelector,
    DynamicMultipleCreator,
    DynamicMultipleSelector,
    ChartBarSimple,
    ChartLineSimple,
    ChartPieSimple,
    BaseFormContentBase: () => import('./base')
  },
  props: {
    /**
     * 是否只读
     */
    editable: {
      type: Boolean,
      default: false
    },
    /**
     * 当前的表单名称
     */
    formName: {
      type: String,
      default: null
    },
    /**
     * 当前的表单编号
     */
    formId: {
      type: [String, Number],
      default: null
    },
    /**
     * 表单数据模型
     */
    formModel: {
      type: [Object, String],
      required: true
    },
    /**
     * 表单的默认填充数据
     */
    defaultData: {
      type: Object,
      default: function() {
        return {}
      }
    },
    /**
     * 是否监听 defaultData 变更并重绘
     */
    watchDefaultData: {
      type: Boolean,
      default: false
    },
    /**
     * 嵌入式表单的父字段
     */
    parentField: {
      type: Object,
      default: null
    },
    /**
     * 父组件的字段模型列表
     */
    parentFieldModels: {
      type: Array,
      default: null
    },
    /**
     * 是否可折叠
     */
    collapsible: {
      type: Boolean,
      default: false
    },
    /**
     * 内嵌表单是否可编辑
     */
    innerEditable: {
      type: Boolean,
      default: false
    },
    /**
     * 是否可折叠内嵌表单
     */
    innerCollapsible: {
      type: Boolean,
      default: false
    },
    /**
     * 是否显示所有的字段（包括隐藏字段）
     */
    showAllFields: {
      type: Boolean,
      default: false
    },
    /**
     * 是否显示表单的校验提示
     */
    showFormError: {
      type: Boolean,
      default: function() {
        return this.editable
      }
    },
    /**
     * 自定义的操作按钮
     */
    actions: {
      type: Array,
      default: null
    }
  },
  data() {
    return {
      fieldModels: [],
      innerFormData: {},
      innerErrorMessages: '',
      renderTimer: null,
      renderSkipped: false
    }
  },
  computed: {
    formValidationFunction() {
      if (tool.isBlank(this.formModel.formValidation)) {
        return null
      }
      try {
        return new Function('formData', this.formModel.formValidation)
      } catch (e) {
        // eslint-disable-next-line
        console.error('Form validation function initialize failed: ', e)
      }
      return null
    },
    formDynamicChangedFunction() {
      if (tool.isBlank(this.formModel.formDynamicChanged)) {
        return null
      }
      try {
        return new Function('fieldModels', 'formData', '$o',
          this.formModel.formDynamicChanged)
      } catch (e) {
        // eslint-disable-next-line
        console.error('Form change function initialize failed: ', e)
      }
      return null
    },
    // fieldModelsEx() {
    //   return this.getVisibleFieldModels(this.formModel, this.defaultData)
    // },
    formErrorMassage() {
      this.formDataValidation()
      return this.innerErrorMessages
    }
  },
  watch: {
    defaultData: {
      handler: function(newData, oldData) {
        if (this.watchDefaultData !== true && this.fieldModels) {
          return
        }
        this.fieldModels.forEach((fieldModel) => {
          setFieldDefaultValue(fieldModel, newData)
        })
      }
    }
  },
  mounted() {
    this.getVisibleFieldModels(this.formModel, this.defaultData)
  },
  updated() {
    this.formDataValidation()
  },
  methods: {
    getPlaceholder(field) {
      if (!tool.isBlank(field.placeholder)) {
        return field.placeholder
      }
      let defaultPlaceholder = ''
      switch (field.comType) {
        case 'DateTime':
          defaultPlaceholder = '请选择时间'
          break
        case 'TimeOnly':
          defaultPlaceholder = '请选择时间'
          break
        case 'DateOnly':
          defaultPlaceholder = '请选择日期'
          break
        case 'select':
          defaultPlaceholder = '请选择' + field.title
          break
        case 'tableView':
          defaultPlaceholder = '可输入关键词进行动态筛选'
          break
        case 'dynamicSelect':
          defaultPlaceholder = '可输入关键词进行动态筛选'
          break
        default:
          defaultPlaceholder = ('请输入' + (field.title || ''))
      }
      return defaultPlaceholder
    },

    /**
     * 解析可显示的字段模型清单
     *
     * 通过 computed 计算的数据在使用时会出现 input 控件
     * 无法输入的情况，因此这里将计算结果保存在 data 的
     * fieldModels 属性中。
     *
     * 至于导致 input 控件无法输入的原因暂不明确，通过
     * 这种方式从结果看可以规避该问题的出现
     */
    getVisibleFieldModels(formModel, formData) {
      var options = FORM_FIELD_OPTIONS.SeparatorIncluded |
                     FORM_FIELD_OPTIONS.HiddenIfAllEmpty |
                     FORM_FIELD_OPTIONS.OrderUndefinedExcluded
      if (this.showAllFields === true) {
        options = FORM_FIELD_OPTIONS.All
      }
      this.fieldModels = []
      if (tool.isPlainObject(formModel) ||
          (tool.isString(formModel) && formModel.indexOf('{') === 0)) {
        this.fieldModels = getVisibleFieldModels(formModel, formData, options)
      }
      // console.log('解析到的表单字段模型数据如下：', this.fieldModels, formModel, formData)
      return this.fieldModels
    },

    /*
     * 获取字段的只读显示文本
     * @param {Object} fieldModel
     */
    getTextDisplay(fieldModel) {
      if (!fieldModel) {
        return ''
      }
      // console.log('获取字段的显示文本: ', fieldModel.key , ' => ', fieldModel)
      return tool.stringify(getFieldValueDisplay(fieldModel, fieldModel.value))
    },

    /**
     * 计算字段值的显示样式
     */
    getFieldValueStyle(fieldModel) {
      return getFieldValueDisplayStyle(fieldModel, fieldModel.value)
    },

    /**
     * 计算字段值的前置图标
     */
    getFieldValueIcon(fieldModel) {
      return getFieldValueDisplayIcon(fieldModel, fieldModel.value)
    },

    /**
     * 获取表单的验证内容
     */
    getFormValidData() {
      if (this.formDataValidation()) {
        return this.innerFormData
      }
      this.$notify.error(this.innerErrorMessages)
      return null
    },

    /**
     * 表单内容整体验证脚本的执行
     */
    formDataValidation() {
      this.innerFormData = null
      this.innerErrorMessages = ''
      // console.log('表单单个字段的验证进入')
      if (!this.$refs.baseFormContent) {
        return
      }
      var formData = {}
      var fieldErrors = []
      var errorMessages = ''
      var mappedFieldModels = {}
      // console.log('表单单个字段的验证开始')
      for (const field of this.fieldModels) {
        mappedFieldModels[field.key] = field
        if (field.comType === 'innerForm' && !field.readonly && this.innerEditable) {
          var fieldValues = []
          if (this.$refs[`InnerForm:${field.key}`]) {
            for (var innerForm of this.$refs[`InnerForm:${field.key}`]) {
              if (!innerForm.formDataValidation()) {
                errorMessages = innerForm.innerErrorMessages
              }
              fieldValues.push(innerForm.innerFormData)
            }
          }
          field.value = fieldValues
        }
        if (this.editable && !field.readonly) {
          if (field.comType === 'DateOnly') {
            field.value = tool.formatUtcDateTime(field.value, 'date')
          } else if (field.comType === 'DateTime') {
            field.value = tool.formatUtcDateTime(field.value)
          } else if (field.comType === 'TimeOnly') {
            field.value = tool.formatUtcDateTime(field.value, 'time')
          }
        }
        if (field.dynamicSelectedEditable && tool.isArray(field.value)) {
          for (var v of field.value) {
            v['__skip_option_value_convert__'] = true
          }
        }
        if (this.$refs.baseFormContent.showFieldErrorMsg(field)) {
          fieldErrors.push(field)
        } else {
          formData[field.key] = field.value
        }
      }
      if (fieldErrors && fieldErrors.length > 0) {
        var curerrors = 0
        var maxerrors = 3
        errorMessages = '表单字段填写不符合要求, 错误信息如下:'
        while (curerrors < maxerrors) {
          if (curerrors >= fieldErrors.length) {
            break
          }
          var field = fieldErrors[curerrors++]
          errorMessages += `\n字段[${field.title}]: ${field.errormsg}`
        }
        if (fieldErrors.length > maxerrors) {
          errorMessages += '\n...'
        }
      }
      // console.log('表单单个字段的验证结束')
      /**
       *  执行表单内容验证脚本
       */
      if (!tool.isBlank(this.formModel.formValidation)) {
        if (!tool.isFunction(this.formValidationFunction)) {
          errorMessages = '表单内容验证配置异常: 脚本初始化失败, 请联系系统管理员'
        } else {
          var formErrorMsg = null
          try {
            formErrorMsg = this.formValidationFunction.call(tool, formData)
          } catch (e) {
            // eslint-disable-next-line
            console.error(e)
            formErrorMsg = '表单内容验证配置异常: 脚本执行失败, 请联系系统管理员'
          }
          if (!tool.isString(formErrorMsg)) {
            formErrorMsg = '表单内容验证配置异常: 脚本要求返回字符串, 请联系系统管理员'
          }
          if (!tool.isBlank(formErrorMsg)) {
            errorMessages = '表单内容验证失败: ' + formErrorMsg
          }
        }
        // console.log('表单整体内容的验证结束')
      }
      /**
       *  执行表单内容变更脚本
       */
      if (!tool.isBlank(this.formModel.formDynamicChanged)) {
        if (!tool.isFunction(this.formDynamicChangedFunction)) {
          errorMessages = '表单内容变更配置异常: 脚本初始化失败, 请联系系统管理员'
        } else {
          try {
            this.formDynamicChangedFunction(mappedFieldModels, formData, {
              'tool': tool,
              'editable': this.editable
            })
          } catch (e) {
            // eslint-disable-next-line
            console.error(e)
            errorMessages = '表单内容变更配置异常: 脚本执行失败, 请联系系统管理员'
          }
        }
        // console.log('表单内容自动刷新结束')
      }
      this.innerFormData = formData
      this.innerErrorMessages = errorMessages
      return tool.isBlank(errorMessages) ? formData : null
    },

    /**
     * 整合级联产生的父表单模型
     */
    parentFieldModelsConcat() {
      var concated = []
      if (tool.isArray(this.parentFieldModels)) {
        concated = concated.concat(this.parentFieldModels)
      }
      if (tool.isArray(this.fieldModels)) {
        concated = concated.concat(this.fieldModels)
      }
      return concated
    },
    /**
     * 自定义按钮点击回调
     */
    onFieldButtonClick(field) {
      // console.log('表单按钮点击事件：', field)
      this.$emit('button', field, this)
      var fields = [{
        field: field,
        form: this
      }]
      var $this = this
      while ($this.parentField && $this.parentField.comType === 'innerForm') {
        fields.push({ field: $this.parentField, form: $this.$parent })
        // console.log('表单按钮点击事件：', fields, $this.$parent)
        $this.$parent.$emit('button', fields, $this.$parent)
        $this = $this.$parent
      }
    },
    /**
     * 强制表单界面重新渲染。
     */
    forceFormRerender() {
      /*
       * 为避免在内容变更脚本中使用该方法导致陷入死循环, 设置了一个刷新的延时(200毫秒)，
       * 同时设置了 5 秒的最小刷新间隔
       */
      if (this.renderSkipped || this.renderTimer || !this.$refs['baseFormContent']) {
        return
      }
      this.renderSkipped = true
      this.renderTimer = setTimeout(() => {
        this.renderTimer = false
        setTimeout(() => {
          this.renderSkipped = false
        }, 5000)
      }, 200)
    }
  }
}
</script>
<style lang="scss">
.form-content-wrapper {
  .form-content-buttons {
    padding: 5px 50px;
    text-align: center;
  }
  .vdatetime-popup__month-selector {
    padding: 0px !important;
  }
}
</style>
