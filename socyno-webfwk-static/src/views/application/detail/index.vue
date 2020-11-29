<template>
  <div class="applicaiton-detail fit-container">
    <Panel :app="appData" />
    <Files :app="appData" />
  </div>
</template>
<script>
import Files from './ApplicationCategoryPanel'
import Panel from './ApplicationDetailPanel'
import { getAppSummary } from '@/apis/application'
export default {
  components: {
    Files,
    Panel
  },
  data() {
    return {
      appData: {}
    }
  },
  mounted() {
    getAppSummary(this.$route.params.app_id).then(res => {
      this.appData = res.data
    })
  }
}
</script>
<style scoped>

</style>
