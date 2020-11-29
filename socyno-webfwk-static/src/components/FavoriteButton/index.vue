<template>
  <el-button size="small" :type="icononly ? 'text': ''" @click="handleFav">
    {{ icononly ? '' : (value ? '取消收藏' : '收藏') }} <i class="el-icon-star-off el-icon--right" :style="value ? 'color:gold;' : 'color:grey;'" />
  </el-button>
</template>
<script>
import { addToFav, removeFromFav } from '@/apis/application/index'
export default {
  props: {
    isFav: {
      type: Boolean,
      default: false
    },
    appId: {
      type: Number,
      default: null
    },
    icononly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      value: false
    }
  },
  watch: {
    isFav: {
      immediate: true,
      handler: function(newv) {
        this.value = newv
      }
    }
  },
  methods: {
    handleFav() {
      if (this.value) {
        removeFromFav(this.appId).then(res => {
          this.value = false
        })
      } else {
        addToFav(this.appId).then(res => {
          this.value = true
        })
      }
    }
  }
}
</script>
<style scoped>
.el-icon-star-off {
  font-size: 14px;
}
</style>
