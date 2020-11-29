<template>
  <el-pagination
    class="pagesbar"
    :current-page="pageData.page"
    :page-sizes="[10, 20, 30, 50]"
    :page-size="pageData.limit"
    layout="total, sizes, prev, pager, next, jumper"
    :total="total"
    @size-change="onPageSizeChange"
    @current-change="onPageChange"
  />
</template>
<script>
export default {
  model: {
    props: 'options',
    event: 'change'
  },
  props: {
    options: {
      type: Object,
      default: null,
      required: true
    },
    total: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      pageData: {}
    }
  },
  watch: {
    options: {
      immediate: true,
      handler: function(v, o) {
        this.pageData = v
      }
    },
    total: {
      immediate: true,
      handler: function(v, o) {
        this.total = v
      } }
  },
  mounted() {

  },
  methods: {
    onPageSizeChange(e) {
      this.pageData.limit = e
      this.$emit('change', this.pageData)
    },
    onPageChange(e) {
      this.pageData.page = e
      this.$emit('change', this.pageData)
    }
  }
}
</script>
<style>

</style>
