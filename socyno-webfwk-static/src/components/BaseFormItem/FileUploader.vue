<template>
  <div class="form-uploader">
    <el-upload
      v-if="editable"
      :drag="dragable"
      :action="getUploadTargetUrl()"
      :multiple="fieldModel.type === 'array'"
      :limit="fieldModel.type === 'array' ? 99 : 1"
      :name="fieldModel.key"
      :on-success="onFileUploaded"
      :file-list="resultFiles"
      :show-file-list="false"
      :headers="getUploadHeaders()"
    >
      <i v-if="dragable" class="el-icon-upload" />
      <div v-if="dragable" class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <el-button v-if="!dragable" size="small" type="primary">
        点击上传
      </el-button>
    </el-upload>
    <FileUploaderTable
      :files="resultFiles"
      :form-name="formName"
      :removable="editable"
      @delete="onFileRemoved"
    />
  </div>
</template>
<script>
import tool from '@/utils/tools'
import { Notification } from 'element-ui'
import FileUploaderTable from './FileUploaderTable'
export default {
  components: {
    FileUploaderTable
  },
  props: {
    value: {
      type: Array,
      default: function() {
        return []
      }
    },
    formName: {
      type: String,
      default: function() {
        return this.$route.params.formName || this.$route.query.formName
      }
    },
    fieldModel: {
      type: Object,
      required: true
    },
    editable: {
      type: Boolean,
      default: function() {
        return !this.fieldModel.readonly
      }
    },
    dragable: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      resultFiles: []
    }
  },
  watch: {
    value: {
      immediate: true,
      handler: function(newValue) {
        this.resultFiles = []
        if (newValue) {
          // console.log('附件初始化清单数据如下：', newValue)
          this.resultFiles = tool.isArray(newValue) ? newValue : [newValue]
        }
      }
    }
  },
  methods: {
    /**
     * 新的文件上传完成回调处理
     * @param {Object} response       服务器上传后的响应
     * @param {Object} file           当前上传的文件
     * @param {Object} resultFiles    最终文件结果清单
     */
    onFileUploaded(response, file, resultFiles) {
      // console.log(file)
      // console.log(resultFiles)
      var fileIndex = -1
      for (var i in resultFiles) {
        if (resultFiles[i].uid && resultFiles[i].uid === file.uid) {
          fileIndex = i
          break
        }
      }
      // 确认上传成功后，保存上传的附件数据
      if (response.data && response.data.length > 0) {
        resultFiles[fileIndex] = response.data[0]
      } else {
        // 否则丢弃掉页面的附件项，并弹出提示信息
        resultFiles.splice(fileIndex, 1)
        Notification.error(response.message || '文件上传失败')
      }
      this.resultFiles = resultFiles
      this.$emit('input', this.resultFiles)
    },
    /**
     * 文件删除的回调处理
     * @param {Object} index
     */
    onFileRemoved(index) {
      this.resultFiles.splice(index, 1)
      this.$emit('input', this.resultFiles)
    },
    /**
     * 上传接口的请求头
     */
    getUploadHeaders() {
      var headers = {}
      var tokenContent = this.$store.getters['user/getToken']()
      var tokenHeader = this.$store.getters['user/getTokenHeader']()
      headers[tokenHeader] = tokenContent
      return headers
    },
    /**
     * 上传接口的访问地址
     */
    getUploadTargetUrl() {
      return this.$store.getters['user/getApiUrl']() + 'form/upload/' + tool.encodeURI(this.formName)
    }
  }
}
</script>
<style lang='scss'>

</style>
