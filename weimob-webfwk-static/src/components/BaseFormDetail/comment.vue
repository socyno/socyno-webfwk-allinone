<template>
  <div v-show="formLogsData" v-loading="loading" class="form-comment-lists">
    <hr style="background-color: #EEE; border: 0; height: 1px;">
    <el-table :data="formLogsData" border>
      <el-table-column label="注释编号" prop="id" width="100" />
      <el-table-column label="创建人" prop="operateTime" width="200" />
      <el-table-column label="操作时间" prop="operateUserDisplay" width="200" />
      <el-table-column label="注释内容" prop="operateDesc" show-overflow-tooltip />
    </el-table>
    <el-button-group>
      <el-button type="normal" size="small" icon="el-icon-arrow-left" @click="pageLogsLoad('')">
        首页
      </el-button>
      <el-button :disabled="logsPager.page <= 1" type="normal" size="small" icon="el-icon-arrow-left" @click="pageLogsLoad('prev')">
        上一页
      </el-button>
      <el-button :disabled="logsPager.nomore" type="normal" size="small" @click="pageLogsLoad('next')">
        下一页<i class="el-icon-arrow-right el-icon--right" />
      </el-button>
      <el-button type="text" size="small" style="margin-left:10px; font-size:14px; color:#666;">
        当前: 第 {{ logsPager.page }} 页 {{ formLogsData ? formLogsData.length : 0 }} 条
      </el-button>
    </el-button-group>
  </div>
</template>
<script>
import FormApi from '@/apis/formApi'
export default {
  props: {
    formName: {
      type: String,
      required: true
    },
    formId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      loading: false,
      fromIndex: 0,
      formLogsData: null,
      logsPager: {
        page: 1,
        firstLogId: 0,
        lastLogId: 0,
        nomore: false
      }
    }
  },
  watch: {
    fromIndex: {
      handler: function(fromIndex) {
        this.loadFormLogs()
      }
    }
  },
  methods: {
    /**
     * 重置讨论列表
     */
    load() {
      this.fromIndex = 0
      this.loadFormLogs()
    },

    /**
     * 加载操作日志数据
     */
    loadFormLogs() {
      this.loading = true
      new FormApi(this.formName).loadComments(this.formId, this.fromIndex).then(data => {
        if (!data || data.length < 1) {
          this.logsPager.nomore = true
          if (this.fromIndex > 0) {
            this.$message.info('没有更多数据')
          }
          return
        }
        this.logsPager.nomore = false
        this.formLogsData = data
        if (!this.fromIndex) {
          this.logsPager.page = 1
        } else if (this.fromIndex > 0) {
          this.logsPager.page += 1
        } else {
          this.logsPager.page -= 1
        }
        this.logsPager.firstId = this.formLogsData[0].id
        this.logsPager.lastId = this.formLogsData[data.length - 1].id
      }).finally((res) => {
        this.loading = false
      })
    },
    /**
     * 翻页方向（prev 向前，next 向后，first 首页）
     * @param {String} direction prev / next /first
     */
    pageLogsLoad(direction) {
      if (direction === 'next') {
        this.fromIndex = this.logsPager.lastId
      } else if (direction === 'prev') {
        this.fromIndex = 0 - this.logsPager.firstId
      } else {
        this.fromIndex = 0
      }
    }
  }
}
</script>
