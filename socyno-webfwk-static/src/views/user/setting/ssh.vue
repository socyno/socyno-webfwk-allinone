<template>
  <div class="user-setting-ssh">
    <div class="ssh-edit">
      <h3>新增SSH Key</h3>
      <el-form :model="form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="SSH Key">
          <el-input v-model="form.keyContent" type="textarea" placeholder="以ssh-rsa开头的SSH Key" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            立即新增
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="ssh-list">
      <h3>已增加的SSH Key</h3>
      <div class="ssh-list-wrap">
        <div v-for="(item,index) in list" :key="item.sshId" class="item">
          <div class="summary">
            <div class="title" @click="handleTitle(index)">
              <span>{{ item.title }}</span>
              <!-- <span class="sub">{{ item.id }}</span> -->
            </div>
            <el-button
              type="danger"
              icon="el-icon-delete"
              size="small"
              circle
              plain
              @click="handleDelete(item.id)"
            />
          </div>
          <div v-show="selects[index] === true" class="content">
            {{ item.keyContent }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { getSSHKeyList, addSSHKey, deleteSSHKey } from '@/apis/user/setting'
export default {
  data() {
    return {
      form: {},
      list: [],
      selects: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getSSHKeyList().then(res => {
        this.list = res.data
        this.selects = Array(res.data.length)
      })
    },
    handleTitle(index) {
      var item = this.selects
      if (item[index] === true) {
        item[index] = false
      } else {
        item[index] = true
      }
      this.selects = item
      this.$forceUpdate()
    },
    handleSubmit() {
      addSSHKey(this.form).then(res => {
        this.$notify.success('新增成功')
        this.form = {}
        this.loadData()
      })
    },
    handleDelete(sshid) {
      const that = this
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          deleteSSHKey(sshid).then(res => {
            that.$notify.success('删除成功')
            that.loadData()
          })
        })
        .catch(() => {})
    }
  }
}
</script>
<style lang="scss">
.user-setting-ssh {
  .ssh-list-wrap {
    .item {
      border-bottom: 1px $bordercolor solid;
      width: 50%;
      padding: 10px 0;
      .summary {
        display: flex;
        align-items: center;
        justify-content: space-between;
        .title {
          font-weight: bold;
          cursor: pointer;
          .sub {
            font-weight: normal;
            font-size: 12px;
            margin-left: 20px;
          }
        }
      }
      .content {
        /*media all*/
         font-family: "Menlo", "DejaVu Sans Mono", "Liberation Mono", "Consolas", "Ubuntu Mono",
            "Courier New", "andale mono", "lucida console", monospace;
          display: block;
          padding: 8px 12px;
          margin: 0 0 8px;
          font-size: 13px;
          word-break: break-all;
          word-wrap: break-word;
          color: #2e2e2e;
          background-color: #fafafa;
          border: 1px solid #e5e5e5;
          border-radius: 2px;
          margin-top: 10px;
      }
    }
  }
}
</style>
