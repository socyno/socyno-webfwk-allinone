<template>
  <el-table :data="files" border>
    <el-table-column label="文件名" prop="name" />
    <el-table-column label="文件大小">
      <template slot-scope="scope">
        {{ scope.row.size }}kb
      </template>
    </el-table-column>
    <el-table-column label="添加人" prop="createdName" />
    <el-table-column label="添加时间" prop="createdAt" />
    <el-table-column label="操作" width="100">
      <template slot-scope="scope">
        <div class="shell">
          <el-button type="text" size="small" @click="onDownloadClick(scope.row.id)">
            下载
          </el-button>
          &nbsp;&nbsp;
          <el-popconfirm
            v-if="removable"
            title="确定删除吗？"
            confirm-button-type="danger"
            @onConfirm="onDeleteClick(scope.$index)"
          >
            <el-button slot="reference" type="text" size="small">
              删除
            </el-button>
          </el-popconfirm>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>
<script>
import store from '@/store'
import tool from '@/utils/tools'
export default {
  props: {
    /**
     * 附件清单
     */
    files: {
      type: Array,
      default: null
    },
    /**
     * 是否可移除
     */
    removable: {
      type: Boolean,
      default: false
    },
    /**
     * 表单名称
     */
    formName: {
      type: String,
      default: function() {
        return this.$route.params.formName || this.$route.query.formName
      }
    }
  },
  methods: {
    /**
     * 获取文件下载地址
     * @param {String} attachementId
     */
    getFileDownloadUrl(attachementId) {
      return store.getters['user/getApiUrl']() +
        `form/upload/${tool.encodeURI(this.formName)}/0/${attachementId}/download`
    },
    onDownloadClick(attachementId) {
      var params = {}
      var tokenHeader = '__' + store.getters['user/getTokenHeader']()
      params[tokenHeader] = store.getters['user/getToken']()
      var form = document.createElement('form')
      form.id = 'form-attachment-download'
      form.name = 'form-attachment-download'
      form.target = '_blank'
      document.body.appendChild(form)
      for (var obj in params) {
        if (params.hasOwnProperty(obj)) {
          var input = document.createElement('input')
          input.tpye = 'hidden'
          input.name = obj
          input.value = params[obj]
          form.appendChild(input)
        }
      }
      form.method = 'POST' // 请求方式
      form.action = this.getFileDownloadUrl(attachementId)
      form.submit()
      document.body.removeChild(form)
    },
    /**
     * 移除上传的文件
     * @param {Integer} index
     */
    onDeleteClick(index) {
      this.$emit('delete', index)
    }
  }
}
</script>
<style scoped lang="scss">
.pline {
    .externalLink {
        cursor: pointer;
        color: #409EFF;
        font-size: 13px;
    }
}
</style>
