<template>
  <div class="content" :class="localUser ? 'visible' : 'hidden'">
    <div class="con">
      <div class="head">
        <span class="title">微盟效能后端开发框架</span>
      </div>
      <div class="bg">
        <el-form ref="form01" class="formInp" label-position="right" :rules="rules" :model="param" @submit.native.prevent>
          <div class="account">
            <el-form-item label class="inpName" prop="account">
              <el-input v-model="param.account" placeholder="登录账号" />
            </el-form-item>
          </div>
          <el-form-item label prop="pwd">
            <el-input v-model="param.pwd" class="pwd" type="password" placeholder="登录密码" />
          </el-form-item>
          <div class="pwddiv">
            <el-form-item label>
              <el-button
                class="inp loginBtn"
                native-type="submit"
                type="primary"
                @click="submitForm('form01',1)"
              >
                登 录
              </el-button>
            </el-form-item>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import tool from '@/utils/tools'
import { login, loginWithTicket, requireConfigs } from '@/apis/common'
import { Notification } from 'element-ui'
export default {
  data() {
    return {
      submitState: false,
      param: {
        account: '',
        pwd: ''
      },
      rules: {
        account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        pwd: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      localUser: tool.isNotBlank(this.$route.query.localUser)
    }
  },
  created() {
    /**
     * 解决iframe内置登陆问题
     */
    if (top.location !== self.location) {
      top.location = self.location
    }
    /**
     * 默认方式走 SSO 登陆界面，除非携带了 localUser 选项
     */
    var ssoTicket
    if (tool.isBlank(ssoTicket = this.$route.query.ticket)) {
      console.log(this.localUser)
      if (!this.localUser) {
        this.toSsoLoginPage()
      }
      return
    }
    /**
     * 如果携带有 sso ticket，则进行验证，如果失效或过期则跳转登陆界面
     */
    var ssoService = tool.trim(this.$route.query.service)
    loginWithTicket(ssoTicket, ssoService).then(res => {
      if (res.status !== 0) {
        this.toSsoLoginPage()
        return
      }
      this.$store.dispatch('user/setUserByToken', res.data.tokenContent)
      this.$store.dispatch('user/setTokenHeader', res.data.tokenHeader)
      this.$router.replace(this.$route.query.redirect || '/dashboard')
    }).catch(res => {
      this.toSsoLoginPage()
      return
    })
  },
  methods: {
    /**
     * 跳转至 SSO 登录页面
     */
    toSsoLoginPage() {
      requireConfigs((configs) => {
        var $lo = window.location
        window.location.href = configs['system.user.login.weimob.sso.login.url'] +
              '?service=' +
              tool.encodeURI($lo.protocol + '//' + $lo.host + $lo.pathname + $lo.hash)
      })
    },

    /**
     * 系统内部登陆表单提交
     * @param {Object} formName
     * @param {Object} type
     */
    submitForm(formName, type) {
      this.$refs[formName].validate(valid => {
        if (valid && !this.submitState) {
          this.submitState = true
          login(this.param.account, this.param.pwd).then(res => {
            this.submitState = false
            if (res.status === 0) {
              this.$store.dispatch('user/setUserByToken', res.data.tokenContent)
              this.$store.dispatch('user/setTokenHeader', res.data.tokenHeader)
              this.$router.replace(this.$route.query.redirect || '/dashboard')
            } else {
              Notification.error('用户名密码错误')
            }
          }).catch(res => {
            this.submitState = false
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.content {
  width: 100%;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
  // background: $navcolor;
  overflow: hidden;
  .con {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -80%);
    .head {
      font-size: 28px;
      font-weight: bold;
      color: #fff;
      text-align: center;
      color: $maintext;
      margin-bottom: 30px;
        .subtitle {
          color: $greytext;
          font-size: 14px;
          font-weight: normal;
        }
    }
    .bg {
      background: #fff;
      padding: 15px 25px;
      margin-top: 10px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      .formInp {
        width: 320px;
        margin-top: 20px;
        .account {
          overflow: hidden;
          .inpName {
            margin-bottom: 18px !important;
          }
        }
        .pwddiv {
          text-align: center;
          .loginBtn {
            width:135px;
            // color: #fff;
            font-weight: normal;
            margin-right: 10px;
            // background: $navcolor;
          }
        }
      }
    }
    .oldlogin {
    font-size: 12px;
    color: #fff;
    cursor: pointer;
    }
  }
}
.content.visible {
  display: block;
}
.content.hidden {
  display: none;
}
</style>
