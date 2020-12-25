<template>
  <div class="base-info-row" @click="handleClick">
    <div class="base-info-row-item">
      <span class="base-info-row-title">{{ title }}</span>
      <span class="base-info-row-grextext">{{ subtitle }}</span>
    </div>
    <div class="base-info-row-item flex-between">
      <span class="base-info-row-grextext">{{ detail }}</span>
      <span class="base-info-row-grextext">{{ time }}</span>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    title: {
      type: String,
      default: '',
      required: true
    },
    subtitle: {
      type: String,
      default: ''
    },
    detail: {
      type: String,
      default: ''
    },
    time: {
      type: String,
      default: ''
    }
  },
  methods: {
    handleClick(e) {
      this.$emit('click', e)
    }
  }
}
</script>
<style lang="scss">
.base-info-row {
  padding: 5px 10px;
  border-bottom: 1px $bordercolor solid;
  cursor: pointer;
  .base-info-row-item {
    padding: 5px 0;
    .base-info-row-title {
      color: $maintext;
      font-size: 14px;
      padding-right: 10px;
      width: 100%;
      height: 19px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      display: inline-block;
    }
    .base-info-row-grextext {
      color: $greytext;
      font-size: 12px;
    }
  }
  .flex-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
