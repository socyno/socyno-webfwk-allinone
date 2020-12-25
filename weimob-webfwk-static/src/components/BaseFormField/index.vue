<template>
  <div :class="genFieldContainerStyle()">
    <div :class="genFieldLabelSytle()">
      <el-tooltip effect="light" placement="right">
        <span class="overflow-ellipsis">
          {{ labelText }}
          <i v-if="labelTooltip" class="el-icon-question" />
          <span v-if="labelColon">：</span>
        </span>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <div slot="content" v-html="getFieldTooltip()" />
      </el-tooltip>
    </div>
    <div :class="genFieldContentSytle()">
      <slot />
    </div>
  </div>
</template>
<script>
import tool from '@/utils/tools'
export default {
  name: 'BaseFormField',
  components: {
  },
  props: {
    /**
     * 空间占用，1-12 即一行被均分为12栏，默认： 4
     */
    placement: {
      type: [String, Number],
      default: 4
    },
    /**
     * 标签字串
     */
    labelText: {
      type: String,
      default: ''
    },
    /**
     * 标签是否显示冒号，默认： ture
     */
    labelColon: {
      type: Boolean,
      default: true
    },
    /**
     * 标签宽度，0-200 间的整十数， 默认： 120
     */
    labelWidth: {
      type: [Number, String],
      default: '120'
    },
    /**
     * 字段说明信息（html格式）
     */
    labelTooltip: {
      type: String,
      default: null
    }
  },
  methods: {
    /**
     * 计算字段容器的样式
     */
    genFieldContainerStyle() {
      return 'basic-form-field-wrapper basic-form-field-wrapper' +
                  this.getFieldPlacement()
    },

    /**
     *  获取字段的空间占用值
     */
    getFieldPlacement() {
      var placement = 4
      if (tool.looksLikeInteger(this.placement)) {
        placement = tool.parseInteger(this.placement, 4)
      }
      if (placement < 0) {
        return 4
      }
      if (placement > 12) {
        return 12
      }
      return placement
    },

    /**
     * 计算字段标题的样式
     */
    genFieldLabelSytle() {
      var clazz = 'basic-form-field-title'
      var titleWidth = tool.trim(this.labelWidth)
      if (tool.inArray(titleWidth, ['0', '10', '20', '30', '40',
        '50', '60', '70', '80', '90', '100', '110', '120', '130',
        '140', '150', '160', '170', '180', '190', '200']) >= 0) {
        clazz += ' basic-form-field-title-' + titleWidth
      }
      return clazz
    },

    /**
     * 计算字段内容的样式
     */
    genFieldContentSytle() {
      var clazz = 'basic-form-field-content'
      var titleWidth = tool.trim(this.labelWidth)
      if (tool.inArray(titleWidth, ['0', '10', '20', '30', '40',
        '50', '60', '70', '80', '90', '100', '110', '120', '130',
        '140', '150', '160', '170', '180', '190', '200']) >= 0) {
        clazz += ' basic-form-field-withtitle-' + titleWidth
      }
      return clazz
    },

    /**
     * 计算字段的提示信息
     */
    getFieldTooltip() {
      if (tool.isBlank(this.labelTooltip)) {
        return this.labelText
      }
      return this.labelTooltip
    }
  }
}
</script>
<style lang='scss'>
  .basic-form-field-wrapper {
    display: inline-block;
    vertical-align: top;
    margin-top: 15px;
  }
  .basic-form-field-wrapper1 {
    width: calc(8.25% - 0px);
  }
  .basic-form-field-wrapper2 {
    width: calc(16.5% - 0px);
  }
  .basic-form-field-wrapper3 {
    width: calc(24.75% - 0px);
  }
  .basic-form-field-wrapper4 {
    width: calc(33% - 0px);
  }
  .basic-form-field-wrapper5 {
    width: calc(41.25% - 0px);
  }
  .basic-form-field-wrapper6 {
    width: calc(49.5% - 0px);
  }
  .basic-form-field-wrapper7 {
    width: calc(57.75% - 0px);
  }
  .basic-form-field-wrapper8 {
    width: calc(66% - 0px);
  }
  .basic-form-field-wrapper9 {
    width: calc(74.25% - 0px);
  }
  .basic-form-field-wrapper10 {
    width: calc(82.5% - 0px);
  }
  .basic-form-field-wrapper11 {
    width: calc(90.75% - 0px);
  }
  .basic-form-field-wrapper12 {
    width: calc(99% - 0px);
  }
  .basic-form-field-title {
    color: #606266;
    width: 120px;
    display: inline-block;
    white-space: nowrap;
    // font-weight: bold;
    text-align: right;
    padding-right: 5px;
    overflow: hidden;
    text-overflow: ellipsis !important;
  }
  .basic-form-field-title-0 {
    // width: 0px !important;
    display: none !important;
  }
  .basic-form-field-title-10 {
    width: 10px !important;
  }
  .basic-form-field-title-20 {
    width: 20px !important;
  }
  .basic-form-field-title-30 {
    width: 30px !important;
  }
  .basic-form-field-title-40 {
    width: 40px !important;
  }
  .basic-form-field-title-50 {
    width: 50px !important;
  }
  .basic-form-field-title-60 {
    width: 60px !important;
  }
  .basic-form-field-title-70 {
    width: 70px !important;
  }
  .basic-form-field-title-80 {
    width: 80px !important;
  }
  .basic-form-field-title-90 {
    width: 90px !important;
  }
  .basic-form-field-title-100 {
    width: 100px !important;
  }
  .basic-form-field-title-110 {
    width: 110px !important;
  }
  .basic-form-field-title-120 {
    width: 120px !important;
  }
  .basic-form-field-title-130 {
    width: 130px !important;
  }
  .basic-form-field-title-140 {
    width: 140px !important;
  }
  .basic-form-field-title-150 {
    width: 150px !important;
  }
  .basic-form-field-title-160 {
    width: 160px !important;
  }
  .basic-form-field-title-170 {
    width: 170px !important;
  }
  .basic-form-field-title-180 {
    width: 180px !important;
  }
  .basic-form-field-title-190 {
    width: 190px !important;
  }
  .basic-form-field-title-200 {
    width: 200px !important;
  }
  .basic-form-field-content {
    overflow: hidden;
    display: inline-block;
    width: calc(100% - 125px);
    vertical-align: top;
    .form-field-configger {
      display: inline-block !important;
    }
    .el-input,
    .el-select,
    .el-range-editor--mini.el-input__inner {
      width: 100% !important;
    }
    .el-button {
      margin: 0 !important;
      padding: 5px !important;
    }
    .el-tooltip {
      white-space: nowrap;
      text-overflow: ellipsis !important;
    }
  }
  .basic-form-field-content.basic-form-field-withtitle-0 {
    width: calc(100% - 5px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-10 {
    width: calc(100% - 15px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-20 {
    width: calc(100% - 25px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-30 {
    width: calc(100% - 35px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-40 {
    width: calc(100% - 45px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-50 {
    width: calc(100% - 55px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-60 {
    width: calc(100% - 65px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-70 {
    width: calc(100% - 75px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-80 {
    width: calc(100% - 85px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-90 {
    width: calc(100% - 95px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-100 {
    width: calc(100% - 105px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-110 {
    width: calc(100% - 115px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-120 {
    width: calc(100% - 125px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-130 {
    width: calc(100% - 135px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-140 {
    width: calc(100% - 145px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-150 {
    width: calc(100% - 155px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-160 {
    width: calc(100% - 165px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-170 {
    width: calc(100% - 175px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-180 {
    width: calc(100% - 185px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-190 {
    width: calc(100% - 195px) !important;
  }
  .basic-form-field-content.basic-form-field-withtitle-200 {
    width: calc(100% - 205px) !important;
  }
</style>
