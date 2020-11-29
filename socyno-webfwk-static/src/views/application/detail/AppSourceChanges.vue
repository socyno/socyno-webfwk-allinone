<template>
  <div>
    <div v-for="(group,keys) in history" :key="keys" class="content">
      <div class="group">
        {{ group.time }}
      </div>
      <div v-for="(item,key) in group.content" :key="key" class="item">
        <div class="l name">
          <div class="cname Summary">
            <span>{{ item.vcsSummary }}</span>
          </div>
          <div class="pname" />
          <div class="uname">
            <span style="margin-left:4px;">{{ item.createdNameBy }}</span>
            <span v-if="!$route.params.app_id" style="margin-left:10px;" class="link">
              <router-link target="_blank" :to="{path:'app/' + item.applicationId, params: {app_id: item.applicationId}}">{{ item.application.name }}</router-link>
            </span>
            <span style="margin-left:10px;">{{ item.createdAt.substr(0, 19) }}</span>

            <i class="branch">{{ item.vcsRefsName }}</i>
          </div>
        </div>
        <div class="r option">
          <div class="version">
            <el-input :id="'src-addr-input-' + item.id" :value="item.vcsRevision.substr(0, 8)" readonly size="small" placeholder="请输入内容" style="width:90px;margin-right:10px;" />
            <el-button type="text" size="small" icon="el-icon-document-copy" @click="handleCopy(item.id)" />
          </div>
        </div>
      </div>
    </div>
    <BasePagination v-model="page" :options="page" :total="total" @change="loadData" />
  </div>
</template>
<script>
import { getApplicationCommitHistory, getChangeMine } from '@/apis/application/index'

import BasePagination from '@/components/BasePagination'
export default {
  components: {
    BasePagination
  },
  props: {
    branch: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      history: [],
      page: {
        page: 1,
        limit: 10
      },
      total: 0
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      if (this.$route.params.app_id) {
        getApplicationCommitHistory(this.$route.params.app_id, this.page, this.branch).then(res => {
          this.history = this.sortDataByDate(res.data.list)
          this.total = res.data.total
        })
      } else {
        getChangeMine(this.page).then(res => {
          this.history = this.sortDataByDate(res.data.list)
          this.total = res.data.total
        })
      }
    },
    sortDataByDate(list) {
      var zlist = []
      list.forEach(item => {
        var d = zlist.filter(t => t.time === item.createdAt.substring(0, 10))
        if (d && d.length > 0) {
          d[0].content.push(item)
        } else {
          zlist.push({ time: item.createdAt.substring(0, 10), content: [item] })
        }
      })
      return zlist
    },
    handleCopy(key) {
      const ele = document.getElementById('src-addr-input-' + key)
      ele.select()
      document.execCommand('copy')
      this.$notify.success('复制成功')
    }
  }
}
</script>
<style lang='scss'>
.el-pagination {
  padding: 20px 30px 0;
}
.content{
        position: relative;
        .group{
            padding: 10px 30px;
            background-color: #fafafa;
            border-bottom: 1px solid #eee;
            border-top: 1px solid #eee;
            font-size: 14px;
        }
        .item{
            position: relative;
            overflow: hidden;
            padding: 15px 20px;
            color: #212529;
            border-bottom: 1px solid #eee;
            .level{
                width: 35px;
                height: 35px;
                border-radius: 20px;
                text-align: center;
                line-height: 35px;
                color: #fff;
            }
            .bgred{
                color: red;
            }
            .bgblue{
                color: #409EFF;
            }
            .bgyellow{
                color: #FFD700;
            }
            .urgent{
                border: 1px solid red !important;
                color: indianred !important;
            }
            .name{
                margin-left: 20px;

                .branch{
                        border: 1px solid #e5e5e5;
                        border-radius: 10px;
                        display: inline-block;
                        padding: 2px 8px;
                        color: #707070;
                        margin-left: 10px;
                    }
                .pname{
                    color: #2e2e2e;
                    font-size: 14px;

                    .link{
                        cursor: pointer;
                        :hover{
                            text-decoration: underline;
                        }
                    }

                    .atype{
                        font-weight: bold;
                        display: inline-block;
                        background-color: #ddd;
                        margin-right: 6px;
                        color: #909399;
                        padding: 1px 8px;
                    }
                }
                .cname{
                    font-size: 14px;
                    max-width: 900px;
                    margin: 10px 5px 0;
                }
                .Summary{
                    color: #2e2e2e;
                    font-weight: bold;
                    padding-bottom: 10px;
                    cursor: pointer;
                    margin-right: 10px;
                    :hover{
                        text-decoration: underline;
                    }
                }
                .uname{
                    color:#212529;
                    font-size: 14px
                }
                .owner{
                    font-size: 14px;
                    margin-top: 4px;
                    .us{
                        margin-right: 10px;
                    }
                }
            }
            .option{
                text-align: right;
                position: absolute;
                top: 50%;
                right: 0;
                transform: translate(0, -50%);
                margin-right: 30px;
                .state{
                    text-align: right;
                    font-size: 14px;
                    color: #707070;
                }
                ::-webkit-scrollbar {display:none;}
                .version{
                    width: 180px;
                    height: 20px;
                    position: relative;
                    .vys {
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        overflow-x: scroll;
                        scrollbar-width: none;
                    }
                }
                .collect{
                    cursor: pointer;
                    font-size: 12px;
                }

            }

        }
    }
 .content{
        position: relative;

        .item{

            .name{
                margin-left: 10px;
                .pname{

                    .link{
                      padding-left: 4px;
                        cursor: pointer;
                        :hover{
                            text-decoration: underline;
                        }
                    }
                }
                .cname{
                    .branch{
                        all: unset;
                        border: 1px solid #e5e5e5;
                        border-radius: 10px;
                        display: inline-block;
                        padding: 2px 8px;
                        color: #707070;
                        margin-left: 10px;

                        font-weight: normal;
                    }
                    .branch:hover {
                      text-decoration: none;
                    }
                }

                .uname{
                    padding-top: 5px;
                }

            }
            .option{
                text-align: right;
                position: absolute;
                top: 50%;
                right: 0;
                transform: translate(0, -50%);
                margin-right: 30px;

                ::-webkit-scrollbar {display:none;}
                .version{
                    width: 180px;
                    height: 20px;
                    position: relative;

                }

            }

        }
    }
</style>
