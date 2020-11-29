<template>
  <div class="applicaiton-detail-panel">
    <el-card v-if="app">
      <div class="panel-top">
        <div class="row-top">
          <div class="left">
            <div class="titles">
              <span class="t1" @click="handlePath">{{ app.subsystemName }}</span>
              <span class="t2"> / </span>
              <span class="t3" @click="handlePath">{{ app.name }}</span>
            </div>
          </div>
          <div>
            <el-button size="small">
              {{ $enums.getDisplayOrValue('ApplicationState', app.state) }}
            </el-button>
            <FavoriteButton :is-fav="app.bookmarked" :app-id="app.id" />
          </div>
        </div>
        <div class="row-info">
          {{ app.description }}
        </div>
        <div class="row-acts">
          <el-input id="src-addr-input" v-model="app.vcsPath" readonly size="small" placeholder="请输入内容" class="input-with-select common-code">
            <el-select slot="prepend" value="0" placeholder="请选择">
              <el-option :label="app.vcsType" value="0" />
            </el-select>
            <el-button slot="append" size="small" icon="el-icon-document-copy" @click="handleCopy" />
          </el-input>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script>
import FavoriteButton from '@/components/FavoriteButton'

export default {
  components: {
    FavoriteButton
  },
  props: {
    app: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      value: ''
    }
  },
  mounted() {

  },
  methods: {
    handleCopy() {
      const ele = document.getElementById('src-addr-input')
      ele.select()
      document.execCommand('copy')
      this.$notify.success('复制成功')
    },
    handlePath() {

    }
  }
}
</script>
<style lang="scss">
.applicaiton-detail-panel {
  .panel-top {
    .row-top {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .left {
        display: flex;
        align-items: center;

        .icon {
          background: #389389;
          width: 44px;
          height: 44px;
          line-height: 44px;
          text-align: center;
          margin-right: 10px;
        }

        .titles {
          font-size: 16px;
          font-weight: 600;

          .t1 {
            color: #666;
            font-size: 15px;
            cursor: pointer;
          }

          .t2 {
            color: #586069;
            padding: 0 3px;

          }

          .t3 {
            cursor: pointer;
            color: #24292e;
            //  font-weight: bold;
          }
        }
      }
    }
    .row-info {
      color: $greytext;
      margin-top: 10px;
      font-size: 14px;
    }
  }

  .row-acts {
    margin-top: 40px;
    .el-input--suffix .el-input__inner {
      width: 120px;

    }
    .input-with-select {
      width: 500px;
      .el-input-group__prepend {
        background-color: #fff;
      }
    }
  }
}
</style>
