<template>
  <div class="app-detail-status">
    <el-radio-group v-model="switchSelected" size="small" style="width:320px;" @change="switchChanged">
      <el-radio-button v-for="(item, index) in switches" :key="index" :label="item.optionValue">
        {{ item.optionDisplay }}
      </el-radio-button>
    </el-radio-group>
    <BaseFormContent
      v-if="columns"
      form-name="application"
      :form-model="columns.formClass"
      :default-data="columns.form"
    />
  </div>
</template>
<script>
import { getAppEnv, getAppState } from '@/apis/application/index'
import BaseFormContent from '@/components/BaseFormContent'
export default {
  components: {
    BaseFormContent
  },
  props: {
    app: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      switchSelected: '',
      switches: [],
      columns: null
    }
  },
  mounted() {
    getAppEnv(this.app.id).then(res => {
      if (!res.data.length) {
        return
      }
      this.switches = res.data
      this.switchSelected = res.data[0].optionValue
      getAppState(this.app.id, this.switchSelected).then(res => {
        this.columns = res.data
      })
    })
  },
  methods: {
    switchChanged() {
      this.columns = null
      getAppState(this.app.id, this.switchSelected).then(res => {
        this.columns = res.data
      })
    }
  }
}
</script>
<style lang='scss'>
.app-detail-status {
    .el-radio-group {
        margin-bottom: 10px;
  }
}

</style>
