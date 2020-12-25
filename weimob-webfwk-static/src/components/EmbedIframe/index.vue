<template>
  <div class="embed-iframe">
    <iframe v-if="url" ref="iframe" class="embed-iframe-item" scrolling="yes" frameborder="0" :src="url" style="width: 100%; min-height: 200px; overflow: visible; background: rgb(255, 255, 255); height: calc(100vh - 70px);" :style="{height:height}" @load="handleIframeLoad" />
  </div>
</template>
<script>
export default {
  props: {
    url: {
      type: String,
      required: true
    },
    height: {
      type: String,
      default: 'calc(100vh - 90px)'
    }
  },
  mounted() {
    // const recaptchaScript = document.createElement('script')
    // recaptchaScript.setAttribute('src', './js/dictionary.js')
    // document.head.appendChild(recaptchaScript)

    // const recaptchaScript1 = document.createElement('script')
    // recaptchaScript1.setAttribute('src', './js/common.js')
    // document.head.appendChild(recaptchaScript1)

    // window.$script1 = recaptchaScript
    // window.$script2 = recaptchaScript1
  },
  destroyed() {
    // window.$script1.remove()
    // window.$script2.remove()
  },
  methods: {
    handleIframeLoad() {
      var frameWindow = this.$refs.iframe.contentWindow
      // console.log('嵌入的IFRAME页面地址：', frameWindow.location.href)
      frameWindow.scrollTo(0, 0)
    }
  }
}
</script>
<style lang="scss" >

</style>
