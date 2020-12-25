<template>
  <div>
    <div class="subsystem-list">
      <el-form size="small" :inline="true" :model="form" style="float: right; padding: 20px 20px;">
        <el-form-item>
          <el-input v-model="form.namelike" placeholder="关键字" class="input-with-select" />
        </el-form-item>

        <el-form-item>
          <el-select v-model="form.subsystemId" filterable remote reserve-keyword placeholder="选择子系统" clearable :remote-method="remoteMethod" :loading="loading">
            <el-option v-for="item in subsystems" :key="`filter-subsystem-${item.id}`" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-select v-model="form.applicationId" filterable remote clearable reserve-keyword placeholder="选择应用" :remote-method="remoteMethodAppSearch" :loading="loading">
            <el-option v-for="item in applist" :key="`filter-application-${item.id}`" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-select v-model="form.ownerId" filterable remote clearable reserve-keyword placeholder="选择负责人" :remote-method="remoteMethodUsers" :loading="loading">
            <el-option v-for="item in users" :key="`filter-owner-${item.id}`" :label="item.display" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button icon="el-icon-search" type="primary" @click="loadData()" />
          <el-button v-if="!expandAll" type="text" size="primary" @click="handleExpandAll">
            展开
          </el-button>
          <el-button v-if="expandAll" type="text" size="primary" @click="handleCloseAll">
            收起
          </el-button>
        </el-form-item>
      </el-form>

      <el-table ref="sys-table" :data="productline" style="width: 100%;margin-bottom: 20px;" row-key="code" border :tree-props="{children: 'subsystems'}">
        <el-table-column prop="code" label="代码" sortable width="280">
          <template slot-scope="scope">
            <a v-if="scope.row.productlines" class="table_link" size="mini" @click="showSubsystemDetails(scope.$index, scope.row)">
              {{ scope.row.code }}
            </a>
            <span v-else>{{ scope.row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" sortable width="280">
          <template slot-scope="scope">
            <a v-if="scope.row.productlines" class="table_link" size="mini" @click="showSubsystemDetails(scope.$index, scope.row)">
              {{ scope.row.name }}
            </a>
            <span v-else>{{ scope.row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="owner.optionDisplay" :formatter="fmtOwners" label="负责人" />
        <el-table-column prop="subsystemSummaries" label="资源概要" width="500">
          <template slot-scope="scope">
            <div v-if="scope.row.appSummary" size="mini">
              <div class="app-summary-line">
                <span class="app-summary-title">应用总数</span>
                : {{ scope.row.appSummary.appTotal }} 个，其中
                <span v-for="typedApp in scope.row.appSummary.typedAppSummary" :key="typedApp.type">
                  {{ typedApp.display }} {{ typedApp.total }} 个/
                </span>
              </div>
              <div v-for="envns in scope.row.appSummary.evnNamespaceSummary" :key="envns.env">
                <div class="app-summary-line">
                  <span class="app-summary-title">{{ envns.envDisplay }}</span>
                  <span style="white-space:nowarp;"> : 部署 {{ envns.appTotal }} 个应用/共 {{ envns.replicas }} 个副本， 涉及 {{ envns.clusterTotal }} 个集群/{{ envns.namespaceTotal }} 个机组</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <BasePagination v-model="page" :options="page" :total="total" class="pagination" @change="loadData" />
  </div>
</template>
<script>
import BasePagination from '@/components/BasePagination'
import {
  getProductlineOptions,
  getSubsystemOptions,
  getAppOptions,
  getUserOptions
} from '@/apis/application/index'
export default {
  components: {
    BasePagination
  },
  data() {
    return {
      page: {
        limit: 10,
        page: 1
      },
      form: {},
      loading: false,
      total: 0,
      productline: [],
      subsystems: [],
      applist: [],
      users: [],
      expandAll: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    handleExpandAll() {
      var sysTable
      if (!(sysTable = this.$refs['sys-table'])) {
        return
      }
      for (const key in this.productline) {
        sysTable.toggleRowExpansion(this.productline[key], true)
      }
      this.expandAll = true
    },
    handleCloseAll() {
      var sysTable
      if (!(sysTable = this.$refs['sys-table'])) {
        return
      }
      for (const key in this.productline) {
        sysTable.toggleRowExpansion(this.productline[key], false)
      }
      this.expandAll = false
    },
    loadData() {
      var params = this.page
      Object.assign(params, this.form)
      getProductlineOptions(this.page).then(res => {
        this.productline = res.data.list
        this.total = res.data.total
      }).then(res => {
        this.handleExpandAll()
      })
    },
    fmtOwners(row, column, cellValue, index) {
      if (row.owner) {
        return row.owner.display
      } else {
        if (row.owners) {
          return row.owners.map(r => r.display).join(' | ')
        } else {
          return ''
        }
      }
    },
    remoteMethod(query) {
      if (query !== '') {
        this.loading = true
        getSubsystemOptions(query).then(res => {
          this.loading = false
          this.subsystems = res.data
        })
      } else {
        this.subsystems = []
      }
    },
    remoteMethodAppSearch(query) {
      if (query !== '') {
        this.loading = true
        getAppOptions(query).then(res => {
          this.loading = false
          this.applist = res.data
        })
      } else {
        this.applist = []
      }
    },
    remoteMethodUsers(query) {
      if (query !== '') {
        this.loading = true
        getUserOptions(query).then(res => {
          this.loading = false
          this.users = res.data
        })
      } else {
        this.users = []
      }
    },
    showSubsystemDetails(idx, row) {
      window.open('#/form/detail?formName=subsystem&formId=' + row.id)
    }
  }
}
</script>
<style lang="scss">
  .el-table th,
  .el-table tr {
    background: #ffffff !important;
  }

  .subsystem-list .el-table__row--level-1 .el-icon-arrow-right {
    display: none;
  }

  .subsystem-list {
    .el-table .cell {
      text-align: left;
      padding-left: 50px !important;
    }

    .table_link {
      cursor: pointer;
    }

    .table_link:hover {
      text-decoration: underline;
    }
    .app-summary-line {
      display: block;
      white-space: nowarp;
    }
    .app-summary-title {
      display: inline-block;
      white-space: nowarp;
      width : 80px;
    }
  }
</style>
