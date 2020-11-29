<template>
  <div class="app-detail-count">
    <BaseFormContent
      v-if="columns && columns.formClass"
      form-name="application"
      :form-model="columns.formClass"
      :default-data="columns.form"
    />
  </div>
</template>
<script>
import { getAppContainerConfigs } from '@/apis/application/index'
import BaseFormContent from '@/components/BaseFormContent'
export default {
  components: {
    BaseFormContent
  },
  data() {
    return {
      columns: {}
    }
  },
  mounted() {
    getAppContainerConfigs(this.$route.params.app_id).then(res => {
      this.columns = res.data
    })
  }
}
</script>
<style lang="scss">

</style>
