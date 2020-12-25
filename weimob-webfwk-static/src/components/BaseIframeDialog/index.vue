<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="options.title || '提示'"
    :visible.sync="dialogVisible"
    :width="options.width || '95%'"
    append-to-body
    top="10vh"
    class="base-iframe-dialog"
    @closed="onClose()"
  >
    <iframe
      class="base-iframe-dialog-window"
      :style="'height:' + ( options.height || '65vh') +';'"
      :src="urlProcess(options.src)"
      frameborder="0"
      width="100%"
      @load="iframeLoaded"
    />
    <span v-if="options.showConfrim" slot="footer" class="dialog-footer">
      <el-button @click="onClose()">取 消</el-button>
      <el-button type="primary" @click="onOk()">确 定</el-button>
    </span>
  </el-dialog>
</template>
<script>
export default {
  /**
 *
 * window.$openIframeDialog(options)
 * 打开弹窗
 * @param options 框体参数 {
 *         height     高度 55vh (默认)
 *         width     宽 95% (默认)
 *         title     标题
 *         src        URL
 *         showConfrim     是否显示【确定】【关闭】按钮 true,false(默认) 打开时,通过onClose处理事件
 *         bizdata 传递给iframe的业务数据 数据会绑定到iframe的window.$bizdata中
 *         @onClose 窗口关闭自定义回调方法
 *         @onOk 点击确定的回调 通过window.$closeIframeDialog()关闭
 *     }
 *
 * window.$closeIframeDialog()
 * 关闭弹窗
 */
  model: {
    prop: 'showBaseIframe',
    event: 'input'
  },
  props: {
    options: {
      type: Object,
      default() { return {} }
    },
    showBaseIframe: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      iframe: null
    }
  },
  watch: {
    showBaseIframe: {
      immediate: true,
      handler: function(nval) {
        this.dialogVisible = nval
      }
    }
  },
  methods: {
    urlProcess(urlStr) {
      if (!urlStr) {
        return
      }
      if (urlStr.includes('nav=0')) return urlStr

      urlStr += (urlStr.match(/[\?]/g) ? '&' : '?') + 'nav=0'
      return urlStr
    },
    iframeLoaded() {
      var myFrame = document.getElementsByClassName('base-iframe-dialog-window')[0]
      myFrame.contentWindow.$bizdata = this.options.bizdata
      this.iframe = myFrame
    },
    onClose() {
      this.$emit('input', false)
      if (this.options.onClose) { // trigger close event
        this.options.onClose()
      }
      this.ifrmae = null
    },
    onOk() {
      if (this.options.onOk) {
        this.options.onOk()
      }
      this.ifrmae = null
    }
  }
}
</script>
<style lang="scss">
.base-iframe-dialog{
  .el-dialog__header {
    padding: 10px 20px 0;
  }

  .el-dialog__body {
    padding: 10px 20px;
  }
}
</style>
