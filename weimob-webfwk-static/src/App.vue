<template>
  <div id="app">
    <router-view />
    <BaseIframeDialog
      v-model="showBaseIframe"
      :options="iframeOptions"
    />
    <el-dialog
      :modal="false"
      :width="differOptions.width"
      :title="differOptions.title"
      :visible.sync="showDifferDialog"
      @opened="differDialogOpened"
      @closed="differDialogClosed"
    >
      <div class="webfwk-common-differ-label">
        <label style="float:left;">{{ differOptions.beforeLabel }}</label>
        <label style="float:right;">{{ differOptions.changedLabel }}</label>
      </div>
      <div id="webfwk-common-differ-content" />
    </el-dialog>
  </div>
</template>
<script>
import tool from '@/utils/tools'
import formUtil from '@/utils/formUtils'
import { Notification } from 'element-ui'
import pako from 'pako'
import CodeMirror from 'codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/addon/merge/merge.js'
import 'codemirror/addon/merge/merge.css'
import DiffMatchPatch from 'diff-match-patch'
window.diff_match_patch = DiffMatchPatch
window.DIFF_DELETE = -1
window.DIFF_INSERT = 1
window.DIFF_EQUAL = 0
import BaseIframeDialog from '@/components/BaseIframeDialog'
export default {
  components: {
    BaseIframeDialog
  },
  data() {
    return {
      showBaseIframe: false,
      iframeOptions: {},
      showDifferDialog: false,
      differOptions: {}
    }
  },
  created() {
    this.$store.dispatch('user/initUserInfo')
  },
  mounted() {
    var that = this

    window.$openIframeDialog = function(options) {
      that.showBaseIframe = true
      that.iframeOptions = options || {}
    }

    window.$closeIframeDialog = function() {
      that.showBaseIframe = false
    }

    /**
     * GZ 解压缩（Base64编码）
     *
     * @param {String} gzipped
     */
    window.$ungzip = function(gzipped, resultToJson) {
      // base64 解码
      var decoded = window.atob(gzipped)
      // 字符串转数组 在循环返回一个 Unicode表所在位置的新数组
      var charData = decoded.split('').map(x => x.charCodeAt(0))
      // Uint8Array 数组类型表示一个8位无符号整型数组，创建时内容被初始化为0。
      // 创建完后，可以以对象的方式或使用数组下标索引的方式引用数组中的元素。
      var binData = new Uint8Array(charData)
      // 调用 pako 解码数据
      var data = pako.inflate(binData)
      // 接受 Unicode 值，然后返回字符串。
      var decodedIndex = 0
      var decodedResult = ''
      var decodedNextChar = ''
      var decodedNext2Char = ''
      var decodedNext3Char = ''
      var decodedLength = data.length
      while (decodedIndex < decodedLength) {
        decodedNextChar = data[decodedIndex++]
        switch (decodedNextChar >> 4) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
            // 0xxxxxxx
            decodedResult += String.fromCharCode(decodedNextChar)
            break
          case 12:
          case 13:
            // 110x xxxx   10xx xxxx
            decodedNext2Char = data[decodedIndex++]
            decodedResult += String.fromCharCode(((decodedNextChar & 0x1F) << 6) |
                        (decodedNext2Char & 0x3F))
            break
          case 14:
            // 1110 xxxx  10xx xxxx  10xx xxxx
            decodedNext2Char = data[decodedIndex++]
            decodedNext3Char = data[decodedIndex++]
            decodedResult += String.fromCharCode(((decodedNextChar & 0x0F) << 12) |
                       ((decodedNext2Char & 0x3F) << 6) |
                       ((decodedNext3Char & 0x3F) << 0))
            break
        }
      }
      return resultToJson ? JSON.parse(decodedResult) : decodedResult
    }
    // console.log(window.$ungzip('H4sIAAAAAAAAA3vWP+H5kl1WiUnJhkbGALeI35MNAAAA'))
    // console.log(window.$ungzip(
    //     'H4sIAAAAAAAAAI1UW08TQRT+K2afNLHrAqaR8sQ1gYgorfqyiZndPaWDuzN1drZQCUljAoKkSqIk\n' +
    //     '3oIYQfClkWgCgsY/w271yb/gmd12KZcHmrTpfN93rnPmzGke8SWICZF3SQW0XOusXdUc666Pf3Ia\n' +
    //     'uB4eZbWsaPthxvHxaHPGwJaUs3HCyFQslNyziUQS7SrUhlvEg9QeDSQwiedo7WtUb0QHq+GL19G7\n' +
    //     '7ybLXOhjsmjvMHy2EX5+crS/kjr59/OtKUw2xhyq4uFRgi8zRra7O2MYRlaRiTg8eNncPQzXV1Az\n' +
    //     'XvUfuScoBLt6bujXuy5jvlcU1Xy1Ey3that1pBBT0NH+Ybj5FNX4G33bQSJpV8wdrDSXvzQbH5ur\n' +
    //     'i+EbxSX9iKO8X47WlqLadvSpFn3YQs6XgtotrhZubv/5vR4934p2N1QehsJT5O9CvfmrES4uhI0f\n' +
    //     'OcWYgWH02JPg80DYYDISyFKMOaY2iG0mlGFKmskca5QVuWpLm/ZU3Q+wGvVVkiKxJRfVNm9zT7eo\n' +
    //     '6/b26j4QvwSuqwOzRbUs9UJczphj2SMtI7SfxuMo3iwmUkbMbzviYkonZWKXQE/aoCulXubc1emx\n' +
    //     'Xh9M5ygviYS+i9rFag8HaoQy4tLHIPo6cqesmNgNWcqyI0GVs0dm+zFkJe0KNlzBlA3j2EpiuTDq\n' +
    //     'uFCgHoyjP5oW1WOoj9Kyjp6emjdkBXi8Av0WYQ5n4KRCEcA5tIrDA9lWZeMAyukEG+BC8JnT9gk3\n' +
    //     'CTIQ7DzufokmFZwhMdIAyBmApFLs+mTA/HOKjNX44tvgNKmQWR1nRx8ikuSTwUNNBXvvEOXoTgDH\n' +
    //     'Y5Qfvjk8WLjUpSTXYhDUCigRNgWFZJEQx1FbhUoXzu6EeP201kfHq0SYSHw4VoBlark5XFazt/F+\n' +
    //     '83j/Smlo852b6R4IWqwikTy22Gkrevz+EZjGtaE214kr1Ob/A04S5xAXBQAA\n', true))
    /**
     * 显示提示消息
     */
    window.$notify = {
      success: function(params) {
        Notification.success(params)
      },
      error: function(params) {
        Notification.error(params)
      },
      info: function(params) {
        Notification.info(params)
      },
      warning: function(params) {
        Notification.info(params)
      }
    }

    /**
     * 工具类输出
     */
    window.$tool = tool
    window.$formUtil = formUtil

    /**
     * 将数字形式的版本转换为显示字串格式
     * @param {String} versionNumber
     */
    window.$versionConvertByNum = function(num) {
      var version = tool.stringify(num)
      if (!version.match(/^\d+$/)) {
        return num
      }
      version = tool.leftPad(version, 16, 0)
      var startIndex = version.length - 12
      return [
        version.slice(0, startIndex),
        version.slice(startIndex, startIndex + 4),
        version.slice(startIndex + 4, startIndex + 8),
        version.slice(startIndex + 8, startIndex + 12)
      ].map(tool.parseInteger).join('.')
    }

    /**
     * 详情上下文中检测是否允许指定事件
     * 返回值说明如下:
     *  null : 当前非详情页上下文
     *  false : 不允许指定事件
     *  true : 允许指定事件
     */
    window.$formActionsAllowed = function(event) {
      if (!window.$formDetailsVueComponent) {
        return null
      }
      if (!tool.isFunction(window.$formDetailsVueComponent.getFormAction)) {
        return null
      }
      return !!window.$formDetailsVueComponent.getFormAction(event)
    }

    /**
     * 详情上下文中触发执行指定的事件
     * @param { String } event 事件名称
     * @param { Object } params 事件参数
     * @param { Boolean } withoutForm 是否屏蔽表单显示
     * @param { Object } options 事件操作的可选项，可用如下：
     *        success   事件成功后的回调函数
     *        confirmOk 事件执行确认框中的确认按钮显示
     *        confirmCancel 事件执行确认框中的取消按钮显示
     *        confirmMessage 事件执行确认框中的消息显示内容
     */
    window.$triggerFormAction = function(event, params, withoutForm, options) {
      var detailFormRef
      if (!(detailFormRef = window.$formDetailsVueComponent)) {
        throw new Error('Form detail component not valid')
      }
      if (!tool.isFunction(detailFormRef.triggerFormAction)) {
        throw new Error('Form detail component not valid')
      }
      if (tool.isFunction(options)) {
        options = { success: options }
      }
      detailFormRef.triggerFormAction(event, params, withoutForm, options)
    }

    /**
     * 获取详情页的数据
     */
    window.$getFormDetail = function() {
      var detailFormRef
      if (!(detailFormRef = window.$formDetailsVueComponent)) {
        throw new Error('Form detail component no found')
      }
      if (!tool.isFunction(detailFormRef.getInnerFormData)) {
        throw new Error('Form detail component not valid')
      }
      return detailFormRef.getInnerFormData()
    }

    /**
     * 替换详情页的指定字段的数据
     */
    window.$accessFormDetailFeildData = function(fieldKey, accessor) {
      var formData
      if (!(formData = window.$getFormDetail())) {
        throw new Error('No form detail data found')
      }
      // console.log('Current form data is :', formData)
      if (tool.isFunction(accessor)) {
        accessor(formData[fieldKey])
      }
      return formData[fieldKey]
    }

    /**
     * 重新加载流程详情表单的数据内容
     */
    window.$reloadFormDetail = function(extendParams) {
      var detailFormRef
      if (!(detailFormRef = window.$formDetailsVueComponent)) {
        throw new Error('Form detail component not found')
      }
      if (!tool.isFunction(detailFormRef.load)) {
        throw new Error('Form detail component not found')
      }
      return detailFormRef.load(extendParams)
    }

    /**
     * 强制刷新表单的内容数据
     */
    window.$forceFormDetailRerender = function() {
      var detailFormRef
      if (!(detailFormRef = window.$formDetailsVueComponent)) {
        throw new Error('Form detail component not found')
      }
      if (!tool.isFunction(detailFormRef.forceFormRerender)) {
        throw new Error('Form detail component not found')
      }
      return detailFormRef.forceFormRerender()
    }

    /**
     * 获取当前用户信息
     */
    window.$getCurrentUser = function() {
      if (that.$store && that.$store.state && that.$store.state.user) {
        return that.$store.state.user.info
      }
      return null
    }

    /**
     * 打开文本差异对比窗口
     * @param {String} leftText
     * @param {String} rightText
     * @param {Object} options
     */
    window.$openDifferDialog = function(beforeText, changedText, options) {
      options = options || {}
      var differOptions = {
        beforeText: tool.stringify(beforeText),
        changedText: tool.stringify(changedText),
        beforeLabel: tool.ifBlank(options.beforeLabel, '变更前'),
        changedLabel: tool.ifBlank(options.beforeLabel, '变更后'),
        title: tool.ifBlank(options.title, '内容比较'),
        width: tool.ifBlank(options.width, '80%')
      }
      that.showDifferDialog = true
      that.differOptions = differOptions
    }
  },
  methods: {
    /**
     * 当差异比较窗口打开时, 差异比较空间
     */
    differDialogOpened() {
      const target = document.getElementById('webfwk-common-differ-content')
      target.innerHTML = ''
      CodeMirror.MergeView(target, {
        value: this.differOptions.beforeText || '',
        origLeft: null,
        orig: this.differOptions.changedText || '',
        lineNumbers: true,
        mode: 'text/plain',
        highlightDifferences: true,
        connect: 'align',
        revertButtons: false,
        readOnly: true
      })
    },

    /**
     * 当差异比较窗口关闭时, 清理数据
     */
    differDialogClosed() {
      this.differOptions = {}
    }
  }
}
</script>
<style lang="scss">
html {
  font-family: "Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei","微软雅黑",Arial,sans-serif;
}
html, body, #fullheight {
    min-height: 100% !important;
    height: 100%;
}

a {
  all: unset;
}
.clearfix:after {
  visibility: hidden;
  display: block;
  font-size: 0;
  content: " ";
  clear: both;
  height: 0;
}

* {
  outline: none;
}

table {
  width: 100% !important;
}
thead tr th {
  background: #f5f7fa !important;
  line-height: 27px !important;
}
.el-table {
  td,
  th {
    padding: 6px 0 !important;
    text-align: center !important;
  }
  .cell {
    line-height: 20px;
  }
  td.el-table__expanded-cell {
    text-align: left !important;
  }
}

.userRwcord {
  .el-dialog {
    width: 100%;
    max-width: 850px;
  }
}

.clear_hid{
  overflow: hidden;
}
.l{
  float: left;
}
.r{
  float: right;
}
div.webfwk-common-differ-label {
  overflow: hidden;
  margin-top: -30px;
  border-top:1px solid #eee;
  padding: 10px 38% 10px 0;
  font-size: 16px;
  color: #aaa;
}
</style>
