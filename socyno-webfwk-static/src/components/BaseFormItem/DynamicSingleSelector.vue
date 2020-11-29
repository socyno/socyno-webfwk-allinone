<template>
  <div class="dynamic-single-selector">
    <el-select
      ref="selector"
      v-model="value"
      size="mini"
      :placeholder="placeholder"
      :clearable="true"
      :filterable="true"
      :loading="loading"
      :remote="true"
      :disabled="!editable"
      :multiple="multiple"
      :remote-method="onQueryApply"
      value-key="optionValue"
      @clear="onClear"
      @change="onChange"
      @focus="onFocus"
    >
      <el-option-group
        v-for="group in availableOptions"
        :key="group.label"
        :label="group.label"
      >
        <el-option
          v-for="(opt, idx) in group.options"
          v-show="checkOptionVisible(opt)"
          :key="`${idx} - ${opt.optionValue}`"
          :value="opt"
          :label="opt.optionDisplay"
        />
      </el-option-group>
    </el-select>
  </div>
</template>
<script>
import tool from '@/utils/tools'
import FormApi from '@/apis/formApi'

export default {
  model: {
    event: 'change'
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
    fieldModel: {
      type: Object,
      required: true
    },
    parentFieldModels: {
      type: Array,
      default: null
    },
    multiple: {
      type: Boolean,
      default: false
    },
    optionIsVisible: {
      type: Function,
      default: null
    },
    placeholder: {
      type: String,
      default: null
    },
    editable: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      value: null,
      loading: false,
      formApi: null,
      availableOptions: []
    }
  },
  watch: {
    formName: {
      immediate: true,
      handler: function(formName) {
        if (tool.isBlank(formName)) {
          this.formApi = null
          return
        }
        this.formApi = new FormApi(formName)
      }
    },
    fieldModel: {
      immediate: true,
      handler: function(fieldModel) {
        this.value = null
        this.availableOptions = []
        // console.log('sdddddddddd', fieldModel.value)
        if (fieldModel) {
          if (this.multiple) {
            if (!tool.isArray(fieldModel.value)) {
              fieldModel.value = [fieldModel.value]
            }
            this.setAvailableOptions(fieldModel.value)
            this.value = fieldModel.value
          } else {
            if (tool.isString(fieldModel.value)) {
              fieldModel.value = { optionValue: fieldModel.value, optionDisplay: fieldModel.value }
            }
            if (tool.isPlainObject(fieldModel.value)) {
              this.setAvailableOptions([fieldModel.value])
              this.value = fieldModel.value
            }
          }
        }
      }
    }
  },
  methods: {
    /**
     * 是否隐藏选项，在多选场景下过滤已选择项
     * @param {Object} option
     */
    optionWillBeSelectable(option) {
      if (!this.fieldModel.value) {
        return true
      }
      if (this.fieldModel.type !== 'array') {
        return true
      }
      return tool.inArray(option, this.fieldModel.value, function(a, b) {
        return a && a.optionValue === b.optionValue
      }) < 0
    },
    /**
     * 可选列表数据查询
     */
    onQueryApply(keyword) {
      // console.log('动态可选项简单检索参数如下：', keyword)
      if (!this.formApi) {
        throw new Error('当前的表单还未设置，请确认流程单上下文正确设置')
      }
      var params = {}
      // console.log('动态下拉选项查询：父表单模型 - ', this.parentFieldModels)
      if (this.parentFieldModels && tool.isArray(this.parentFieldModels)) {
        for (const field of this.parentFieldModels) {
          params[field.key] = field.value
        }
      }
      this.loading = true
      this.formApi.loadFormFieldOptionsWithQuery(
        this.fieldModel.fieldTypeKey,
        keyword,
        this.formId,
        params
      ).then((res) => {
        this.setAvailableOptions(res.data)
        // console.log('动态可选项简单检索结果如下', res.data)
      }).finally(() => {
        this.loading = false
      })
    },
    /**
     * 动态注入当前的下拉菜单可选项，为确保当前选中的项目被添加进来。
     * 多选的场景下仅用于选择，因此无需考虑当前的选择值。
     * @param {Array} options
     */
    setAvailableOptions(options) {
      if (!tool.isArray(options)) {
        return
      }
      var nextAvailableOptions = []
      if (tool.isArray(this.value || this.fieldModel.value)) {
        nextAvailableOptions = options
      } else {
        if (this.value) {
          nextAvailableOptions.push(this.value)
        }
        for (const option of options) {
          if (!option) {
            continue
          }
          if (this.value && this.value.optionValue === option.optionValue) {
            continue
          }
          nextAvailableOptions.push(option)
        }
      }
      /* 可用的下拉选项分组 */
      var optionGroup
      var oneGroupedOptions
      var allGroupedOptions = {}
      for (const option of nextAvailableOptions) {
        optionGroup = tool.trim(option.optionGroup)
        if (!(oneGroupedOptions = allGroupedOptions[optionGroup])) {
          oneGroupedOptions = []
          allGroupedOptions[optionGroup] = oneGroupedOptions
        }
        oneGroupedOptions.push(option)
      }
      nextAvailableOptions = []
      for (optionGroup in allGroupedOptions) {
        nextAvailableOptions.push({
          label: optionGroup,
          options: allGroupedOptions[optionGroup]
        })
      }
      // console.log(nextAvailableOptions)
      this.availableOptions = nextAvailableOptions
    },
    /**
     * 检查待选项是否可见
     * @param {Object} option
     */
    checkOptionVisible(option) {
      return !tool.isFunction(this.optionIsVisible) ||
          this.optionIsVisible.call(this.$parent, option)
    },
    onChange(value) {
      this.$emit('change', value)
      this.$forceUpdate()
    },
    onClear() {
      this.$emit('clear')
      this.$forceUpdate()
    },
    onFocus() {
      this.onQueryApply('')
    },
    blur() {
      this.$refs.selector.blur()
    },
    focus() {
      this.$refs.selector.focus()
    }
  }
}
</script>
<style lang="scss">
.dynamic-single-selector {

}
</style>
