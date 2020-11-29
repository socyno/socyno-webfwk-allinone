<template>
  <div class="svn-pwd-rst">
    <h3>SVN 密码重置</h3>
    <div class="svn-pwd-rst-form">
      <el-form :model="form">
        <el-form-item label="登录密码">
          <el-input v-model="form.password" type="password" placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="SVN密码(空则使用登录密码)">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入SVN密码(空则使用登录密码)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import { svpPwdReset } from '@/apis/user/setting'
export default {
  data() {
    return {
      form: {},
      loading: false
    }
  },
  methods: {
    handleSubmit() {
      this.loading = true
      var params = this.form
      params.username = this.$store.state.user.info.username
      svpPwdReset(this.form).then(res => {
        this.$notify.success('重置成功')
        this.loading = false
      }).catch(res => {
        this.loading = false
      })
    }
  }

}
</script>
<style>

</style>
