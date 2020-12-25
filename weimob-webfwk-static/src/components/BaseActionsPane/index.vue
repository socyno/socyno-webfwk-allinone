<template>
  <div class="base-form-actions-pane">
    <div v-if="showTitle()" :class="genFormActionTitleClass()">
      {{ title }}
    </div>
    <div v-if="actions && actions.length > 0" :class="genFormActionButtonsClass()">
      <el-button
        v-for="(item, idx) in actions"
        :key="idx"
        :type="item.styleType || type"
        :size="size"
        :value="item.name"
        :style="{ display: item.visible ? '' : 'none' }"
        @click="onActionClick(item)"
      >
        {{ item.display }}
      </el-button>
    </div>
  </div>
</template>
<script>
import tool from '@/utils/tools'
export default {
  props: {
    title: {
      type: String,
      default: null
    },
    actions: {
      type: Array,
      default: null,
      required: true
    },
    /**
     * direction
     * left | right | center
     */
    direction: {
      type: String,
      default: 'right'
    },
    /**
     * el-button
     * medium | small | mini
     */
    size: {
      type: String,
      default: 'mini'
    },
    /**
     * el-button
     * primary | success | warning | danger | info | text
     */
    type: {
      type: String,
      default: 'primary'
    }
  },
  methods: {
    onActionClick(action) {
      this.$emit('input', action.name, action)
    },

    /**
     * 是否显示标题栏
     */
    showTitle() {
      return !tool.isBlank(this.title)
    },

    /**
     * 解析按钮的布局方向
     */
    getButtonsAlignment() {
      var direction = tool.trim(this.direction).toLowerCase()
      if (direction !== 'left' && direction !== 'right' && direction !== 'center') {
        direction = 'right'
      }
      return direction
    },

    /**
     * 拼装标题的显示样式
     */
    genFormActionTitleClass() {
      var clazz = 'base-form-actions-title'
      var direction = this.getButtonsAlignment()
      if (direction !== 'right') {
        clazz += ' base-form-actions-title-right'
      }
      return clazz
    },

    /**
     * 拼装按钮面板的样式
     */
    genFormActionButtonsClass() {
      var clazz = 'base-form-actions-button'
      var direction = this.getButtonsAlignment()
      clazz += ' base-form-actions-button-' + direction
      if (this.showTitle()) {
        clazz += ' base-form-actions-button-with-title'
      }
      return clazz
    }
  }
}
</script>
<style lang="scss">
.base-form-actions-pane {
  position: relative;
  background-color: #fff;
  height: 40px;
  .base-form-actions-title {
    position: absolute;
    display: inline-block;
    width: 300px;
    font-weight: normal;
    font-size: 16px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: #606266;
    padding: 10px;
    margin: 0 10px;
  }
  .base-form-actions-title-right {
    right: 0px;
    text-align: right;
  }
  .base-form-actions-button {
    position: absolute;
    top: 0;
    bottom: 0;
    display: block;
    width: calc(100%);
    margin: 0 10px;
  }
  .base-form-actions-button-with-title {
    width: calc(100% - 360px);
  }
  .base-form-actions-button-left {
    left: 0px;
    text-align: left;
  }
  .base-form-actions-button-center {
    left: 0px;
    text-align: center;
  }
  .base-form-actions-button-right {
    left: 340px;
    text-align: right;
  }
}
.el-button {
  margin-top: 10px !important;
}
</style>
