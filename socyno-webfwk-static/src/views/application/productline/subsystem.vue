<template>
  <div class="subsystem-detail">
    <div class="subsystem-info">
      <el-card>
        <table>
          <tr>
            <td class="info-td">
              <span class="info-item-label">名称:</span>
              <span class="info-item-content"> {{ info.name }}</span>
            </td>
            <td class="info-td">
              <span class="info-item-label">代码:</span>
              <span class="info-item-content"> {{ info.code }}</span>
            </td>
            <td class="info-td">
              <span class="info-item-label">编号:</span>
              <span class="info-item-content"> {{ info.id }}</span>
            </td>
          </tr>
          <tr>
            <td>
              <span class="info-item-label">产品线:</span>
              <span class="info-item-content"> {{ productline }}</span>
            </td>
            <td>
              <span class="info-item-label">状态:</span>
              <span class="info-item-content" :style="info.state === 'enabled' ? 'color: green;' : 'color: red;'"> {{ info.state }}</span>
            </td>
          </tr>
        </table>
        <div class="info-item-row">
          <span class="info-item-label">负责人:</span>
          <span class="info-item-content"> {{ owners }}</span>
        </div>
        <div class="info-item-row">
          <span class="info-item-label">描述:</span>
          <span class="info-item-content"> {{ info.description }}</span>
        </div>
      </el-card>
    </div>
    <div class="subsystem-apps">
      <el-card>
        <div slot="header" class="clearfix">
          <span>相关应用</span>
          <!-- <el-button style="float: right; padding: 3px 0" type="text">
            更多
          </el-button> -->
        </div>
        <el-form size="small" :inline="true" :model="form">
          <el-form-item>
            <el-input v-model="form.namelike" placeholder="关键字" class="input-with-select" />
          </el-form-item>

          <el-form-item>
            <el-select v-model="form.type" placeholder="应用类型" clearable>
              <el-option v-for="(key,value) in applicationType" :key="key" :label="key" :value="value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="form.codeLevel" placeholder="代码等级" clearable>
              <el-option v-for="(key,value) in applicationCodeLevel" :key="key" :label="key" :value="value" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button icon="el-icon-search" type="primary" @click="loadData()" />
          </el-form-item>
        </el-form>
        <el-table :data="apps">
          <el-table-column
            prop="id"
            label="代码"
            sortable
            width="140"
          />
          <el-table-column
            prop="name"
            label="名称"
            sortable
            width="140"
          />
          <el-table-column
            prop="description"
            label="描述"
          />
          <el-table-column
            align="right"
            width="300"
          >
            <template slot-scope="scope">
              <router-link :to="{path:'/app/' + scope.row.id, params: {app_id: scope.row.id}}">
                <el-button
                  size="mini"
                  type="text"
                >
                  查看详细
                </el-button>
              </router-link>
            </template>
          </el-table-column>
        </el-table>
        <br>
        <BasePagination
          v-model="page"
          :options="page"
          :total="total"
          class="pagination"
          @change="loadData"
        />
      </el-card>
    </div>
  </div>
</template>
<script>
import BasePagination from '@/components/BasePagination'
import { getSubsystemInfo, getAllAppList } from '@/apis/application/index'
export default {
  components: {
    BasePagination
  },
  data() {
    return {
      info: {},
      form: {},
      apps: [],
      owners: '',
      productline: '',
      page: {
        limit: 10,
        page: 1
      },
      applicationType: {
        application: '服务端',
        client: '客户端',
        library: '组件库',
        static: '静态资源'
      },
      applicationState: {
        created: '新创建',
        online: '服务中',
        offlining: '下线中',
        offlined: '已下线'
      },
      applicationCodeLevel: {
        '30:blue': '蓝',
        '20:yellow': '黄',
        '10:red': '红'
      }
    }
  },
  mounted() {
    getSubsystemInfo(this.$route.params.sys_id).then(res => {
      this.info = res.data
      const ownersArr = this.info.owners.map(r => r.display)
      this.owners = ownersArr.join(' | ')
      this.productline = this.info.productlines.map(r => r.name).join(' | ')
    })

    this.loadData()
  },
  methods: {
    loadData() {
      var params = this.page
      params.subsystemId = this.$route.params.sys_id
      Object.assign(params, this.form)
      getAllAppList(params).then(res => {
        this.apps = res.data.list
      })
    },
    handDetail(idx, row) {

    }
  }
}
</script>
<style lang="scss">
.subsystem-info {
    margin-bottom: 10px;

    .info-td {
        width: 33.3%;
    }

    .info-item-label {
        color: rgba(0,0,0,.85);
    font-weight: 400;
    font-size: 14px;
    line-height: 1.5715;
    }
    .info-item-content {
        color: rgba(0,0,0,.65);
    font-size: 14px;
    line-height: 1.5715;
    }
    .info-item-row {
    margin-left: 3px;
    margin-bottom: 2px;
    }

}
</style>
