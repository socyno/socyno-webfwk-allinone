<template>
  <div class="user-setting fit-container">
    <el-card>
      <el-tabs tab-position="left">
        <el-tab-pane label="GitSSH配置">
          <SSH />
        </el-tab-pane>
        <el-tab-pane label="SVN密码重置">
          <SvnPwdRst />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
<script>
import SSH from './ssh'
import SvnPwdRst from './svnPwdRst'
export default {
  components: {
    SSH,
    SvnPwdRst
  }
}
</script>
<style lang='scss'>
.el-tab-pane {
    padding: 0 100px;
}
</style>
