<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="innerHTML" />
</template>
<script>
import tool from '@/utils/tools'
import { getFieldValueDisplay, setFieldDefaultValue } from '@/utils/formUtils'

export default {
  props: {
    template: {
      type: String,
      default: ''
    },
    formData: {
      type: Object,
      default: null
    },
    fieldModel: {
      type: Object,
      default: null
    },
    formFields: {
      type: Array,
      default: null
    },
    formModel: {
      type: Object,
      default: null
    },
    dataIndex: {
      type: Number,
      default: 0
    },
    formEditable: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      innerHTML: '',
      cacheGuid: tool.genUuid()
    }
  },
  watch: {
    formData: {
      immediate: true,
      handler: function(n, o) {
        this.render()
        this.$forceUpdate()
      }
    }
  },
  beforeDestroy() {
    this.dataDestroy()
  },
  methods: {
    render() {
      /**
       * 当模板数据发生切换时，需要执行销毁的回调
       */
      this.dataDestroy()
      var tmpldata = {}
      window[this.cacheGuid] = tmpldata
      Object.assign(tmpldata, this.fieldModel)
      tmpldata.data = this.formData
      setFieldDefaultValue(tmpldata, this.formData)
      tmpldata['$rowIndex'] = this.dataIndex
      tmpldata['$formModel'] = this.formModel
      tmpldata['$cacheGuid'] = this.cacheGuid
      tmpldata['$formEditable'] = this.formEditable
      tmpldata['$tool'] = tool
      tmpldata.textDisplay = getFieldValueDisplay(tmpldata, tmpldata.value)
      // console.log("Template Form TMPL: ", this.template)
      // console.log("Template Form DATA: ", JSON.stringify(tmpldata))
      this.innerHTML = window.layui.laytpl(this.template).render(tmpldata)
      this.$nextTick(() => {
        if (tool.isFunction(window[this.cacheGuid].ready)) {
          window[this.cacheGuid].ready(window[this.cacheGuid])
        }
      })
    },
    dataDestroy() {
      var tmpldata = window[this.cacheGuid]
      delete window[this.cacheGuid]
      if (tmpldata && tool.isFunction(tmpldata.destory)) {
        try {
          tmpldata.destory(tmpldata)
        } catch (e) {
          // eslint-disable-next-line
          console.error(e)
        }
      }
    }
  }
}
</script>
