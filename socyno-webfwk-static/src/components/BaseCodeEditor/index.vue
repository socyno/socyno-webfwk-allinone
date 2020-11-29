<template>
  <textarea ref="codeEditor" />
</template>

<script type="text/ecmascript-6">

import tool from '@/utils/tools'
// 引入全局实例和基础样式
import './codemirror-5.57.0/lib/codemirror.js'
import './codemirror-5.57.0/lib/codemirror.css'
// 引入主题样式
import './codemirror-5.57.0/theme/mdn-like.css'
// 全屏样式支持
import './codemirror-5.57.0/addon/display/fullscreen.js'
import './codemirror-5.57.0/addon/display/fullscreen.css'
// 行注释
import './codemirror-5.57.0/addon/comment/comment.js'
import './codemirror-5.57.0/addon/comment/continuecomment.js'
// 括弧和标签匹配
import './codemirror-5.57.0/addon/edit/matchtags.js'
import './codemirror-5.57.0/addon/edit/matchbrackets.js'
// 支持代码折叠
import './codemirror-5.57.0/addon/fold/foldcode.js'
import './codemirror-5.57.0/addon/fold/foldgutter.js'
import './codemirror-5.57.0/addon/fold/xml-fold.js'
import './codemirror-5.57.0/addon/fold/brace-fold.js'
import './codemirror-5.57.0/addon/fold/comment-fold.js'
import './codemirror-5.57.0/addon/fold/foldgutter.css'
// 需要引入具体的语法高亮库才会有对应的语法高亮效果
import './codemirror-5.57.0/mode/css/css.js'
import './codemirror-5.57.0/mode/xml/xml.js'
import './codemirror-5.57.0/mode/htmlmixed/htmlmixed.js'
import './codemirror-5.57.0/mode/javascript/javascript.js'
// lint 语法检查
import { JSHINT } from 'jshint'
window.JSHINT = JSHINT
import { HTMLHint } from 'htmlhint'
window.HTMLHint = HTMLHint
import { CSSLint } from 'csslint'
window.CSSLint = CSSLint
import './codemirror-5.57.0/addon/lint/lint.css'
import './codemirror-5.57.0/addon/lint/lint.js'
import './codemirror-5.57.0/addon/lint/css-lint.js'
import './codemirror-5.57.0/addon/lint/json-lint.js'
import './codemirror-5.57.0/addon/lint/javascript-lint.js'
import './codemirror-5.57.0/addon/lint/htmlmixed-lint.js'

// 尝试获取全局实例
const CodeMirror = window.CodeMirror

export default {
  name: 'BaseCodeEditor',
  props: {
    /**
     * 内容
     */
    value: {
      type: String,
      default: ''
    },
    /**
     * 语法类型, 默认 text/html
     * text/css
     * text/xml
     * text/html
     * text/javascript
     */
    type: {
      type: String,
      default: 'text/html'
    },
    /**
     * 是否只读, 默认否
     */
    readonly: {
      type: Boolean,
      default: false
    },
    /**
     * 宽度, 默认 auto
     */
    width: {
      type: String,
      default: 'auto'
    },
    /**
     * 高度, 默认 100px
     */
    height: {
      type: String,
      default: '100px'
    },
    /**
     * 是否显示行号, 默认否
     */
    numbers: {
      type: Boolean,
      default: false
    },
    /**
     * 全屏模式的 top 值
     */
    fullScreenTop: {
      type: String,
      default: function() {
        return tool.inIframe() ? '0px' : '60px'
      }
    },
    /**
     * 全屏模式的 left 值
     */
    fullScreenLeft: {
      type: String,
      default: '0px'
    },
    /**
     * 全屏模式的 right 值
     */
    fullScreenRight: {
      type: String,
      default: '0px'
    },
    /**
     * 全屏模式的 bottom 值
     */
    fullScreenBottom: {
      type: String,
      default: '0px'
    }
  },
  data() {
    return {
      /**
       * 显示内容
       */
      code: '',
      /**
       * 编辑器实例
       */
      coder: null
    }
  },
  watch: {
    value: {
      handler: function(newValue, oldValue) {
        if (newValue !== this.code) {
          this._setValue()
        }
      }
    },
    width: {
      handler: function(newValue, oldValue) {
        this._setSize()
      }
    },
    height: {
      handler: function(newValue, oldValue) {
        this._setSize()
      }
    }
  },
  mounted() {
    // 初始化编辑器实例
    this.coder = CodeMirror.fromTextArea(this.$refs.codeEditor, {
      /**
       * Tab 单位
       */
      tabSize: 2,
      /**
       * 缩进单位
       */
      indentUnit: 2,
      /**
       * 主题
       */
      theme: 'mdn-like',
      /**
       * 显示行号
       */
      lineNumbers: this.numbers,
      /**
       * 语法类型
       */
      mode: this.type || 'text/html',
      /**
       * 开启 lint 语法检查
       */
      lint: true,
      /**
       * 是否只读
       */
      readOnly: this.readonly,
      /**
       * 支持代码折叠
       */
      foldGutter: true,
      /**
       * 支持自动换行
       */
      lineWrapping: false,
      /**
       * 支持标签和括号匹配
       */
      matchTags: true,
      matchBrackets: true,
      /**
       * 支持块注释自动继续（屏蔽行注释继续）
       */
      continueComments: true,
      continueLineComment: false,
      /**
       * 相关侧边组件注册
       */
      gutters: [
      //  'CodeMirror-linenumbers',
        'CodeMirror-foldgutter',
        'CodeMirror-lint-markers'
      ],
      /**
       * 快捷键
       */
      extraKeys: {
        // 注释切换
        'Ctrl-/': 'toggleComment',
        // 撤销
        'Ctrl-Z': function(coder) {
          coder.undo()
        },
        // 恢复
        'Ctrl-Y': function(coder) {
          coder.redo()
        },
        // 保存
        'Ctrl-S': function(coder) {
          coder._vue._save(true)
        },
        // 全屏切换
        F11: function(coder) {
          coder.setOption('fullScreen', !coder.getOption('fullScreen'))
          setTimeout(function() {
            if (coder.getOption('fullScreen')) {
              coder.setOption('lineNumbers', true)
              coder.display.wrapper.style.setProperty('top', coder._vue.fullScreenTop)
              coder.display.wrapper.style.setProperty('left', coder._vue.fullScreenLeft)
              coder.display.wrapper.style.setProperty('right', coder._vue.fullScreenRight)
              coder.display.wrapper.style.setProperty('bottom', coder._vue.fullScreenBottom)
            } else {
              coder.setOption('lineNumbers', coder._vue.numbers)
              coder.display.wrapper.style.removeProperty('top')
              coder.display.wrapper.style.removeProperty('left')
              coder.display.wrapper.style.removeProperty('right')
              coder.display.wrapper.style.removeProperty('bottom')
            }
          }, 20)
        },
        /**
         * Tab 缩进转空格
         */
        Tab: function(coder) {
          if (coder.somethingSelected()) {
            coder.indentSelection('add')
          } else {
            coder.replaceSelection(Array(coder.getOption('indentUnit') + 1).join(' '), 'end', '+input')
          }
        },
        /**
         * Shift-Tab 反缩进
         */
        'Shift-Tab': function(coder) {
          if (coder.somethingSelected()) {
            coder.indentSelection('subtract')
          } else {
            const cursor = coder.getCursor()
            coder.setCursor({ line: cursor.line, ch: cursor.ch - 4 })
          }
        }
      }
    })
    /**
     * 记录当前的组件对象
     */
    this.coder._vue = this
    /**
     * 内容的初始化
     */
    this._setValue()
    /**
     * 设置显示区域大小
     */
    this._setSize()
    /**
     * 内容变更实时更新
     */
    this.coder.on('change', (coder) => {
      this._save()
    })
  },
  beforeDestroy() {
    this.coder._vue = null
    this.coder.display.wrapper.remove()
    this.coder = null
  },
  methods: {
    _setSize() {
      if (!this.coder) {
        return
      }
      this.coder.setSize(this.width || 'auto', this.height || 'auto')
    },

    _setValue() {
      if (!this.coder) {
        return
      }
      this.coder.setValue(tool.isNullOrUndef(this.value) ? '' : this.value)
    },

    _save(triggerSave) {
      if (!this.coder) {
        return
      }
      this.code = this.coder.getValue()
      this.$emit('input', this.code)
      if (triggerSave === true) {
        this.$emit('save', this.code)
      }
    }
  }
}
</script>

<style>
.CodeMirror {
    font-family: monospace;
    font-size: 16px;
}
</style>
