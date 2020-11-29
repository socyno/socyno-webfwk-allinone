<template>
  <div>
    <div>
      <el-select
        v-model="type"
        filterable
        reserve-keyword
        placeholder="请求类型"
      >
        <el-option label="GET" value="get" />
        <el-option label="POST" value="post" />
      </el-select>
      <el-input v-model="url" placeholder="请求地址" />
      <el-input v-model="params" :rows="5" type="textarea" placeholder="请求参数(JSON)" />
      <el-button :loading="loading" @click="handleRequest">
        发起请求
      </el-button>
    </div>
    <el-divider />
    <div> {{ responseText }} </div>
  </div>
</template>
<script>
import request from '@/utils/request'
export default {
  data() {
    return {
      type: 'get',
      url: '',
      params: '',
      responseText: '',
      loading: false
    }
  },
  mounted() {
  },
  methods: {
    handleRequest() {
      this.loading = true
      this.responseText = ''
      request({
        url: this.url,
        method: this.type,
        params: JSON.parse(this.params || '{}')
      }).then(res => {
        this.responseText = JSON.stringify(res, null, 4)
      }).finally(res => {
        this.loading = false
      })
    }
  }
}
</script>
<style lang="scss">

</style>
