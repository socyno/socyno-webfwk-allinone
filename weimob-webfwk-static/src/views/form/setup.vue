<template>
  <div class="form-setup">
    <el-page-header class="common-page-header" title="关闭" :content="'设置 “'+formTitle+'”'" @back="onGoBack" />
    <div style="margin-bottom: 20px;">
      <el-select
        :key="'form-extras-selector'"
        v-model="formExtraViewAttrs.currentFormClass"
        placeholder="请选择界面模型"
        :filterable="true"
        @change="loadExtraViewAttributes"
      >
        <el-option
          v-for="(info, view, index) in formAllViewNames"
          :key="`form-view-name-${index}`"
          :label="`${view} - ${info.names.join(', ').substr(0, 80)}`"
          :value="view"
        />
      </el-select>
      <el-button type="primary" size="small" style="margin-left: 20px;" @click="showExtraViewCreationForm()">
        添加关联界面模型
      </el-button>
      <el-button type="primary" size="small" style="margin-left: 20px;" @click="removeSelectedExtraView()">
        删除关联界面模型
      </el-button>
    </div>
    <SetupViewConfigPane
      :form-name="formName"
      :form-model="formExtraViewAttrs.currentFormModel || {}"
      @change="onFormViewAttributesChanged"
    />

    <el-dialog
      title="添加关联界面模型"
      :append-to-body="true"
      :modal-append-to-body="true"
      :close-on-click-modal="false"
      :visible.sync="formViewCreation.visible"
      width="800px"
    >
      <el-form
        ref="viewCreationForm"
        :rules="formViewCreation.rules"
        :model="formViewCreation.data"
        class="demo-form-inline"
      >
        <el-form-item label="请输入界面模型名称：" prop="view">
          <el-input v-model="formViewCreation.data.view" class="width600" :placeholder="'请输入界面模型名称（ClassPath）'" />
        </el-form-item>
        <el-form-item label>
          <el-button type="primary" size="small" @click="closeExtraViewCreationForm(true)">
            保存
          </el-button>
          <el-button size="small" @click="closeExtraViewCreationForm()">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>
import tool from '@/utils/tools'
import { Loading } from 'element-ui'
import FormApi from '@/apis/formApi'
import SetupViewConfigPane from './config'
export default {
  components: {
    SetupViewConfigPane
  },
  data() {
    return {
      formName: this.$route.params.formName,
      formTitle: '',
      /**
       * 当前流程涉及的所有界面模型清单，数据结构如下：
       * <form-key> : {
       *   // 表单的显示名称，当适用多种类型时，同时显示
       *   names: '显示名称，如：事件-编辑, 事件-创建'
       *   // 表单适用类型, 参考 availableTypes 定义
       *   types: []
       * }
       */
      availableTypes: {
        action: '事件',
        actrst: '事件结果',
        query: '查询',
        qryrst: '查询结果',
        detail: '详情页面',
        extra: '关联界面模型'
      },
      formAllViewNames: {

      },
      /**
       * 当前界面模型数据
       */
      formExtraViewAttrs: {
        currentFormClass: '',
        currentFormModel: null
      },
      /**
       * 新增关联界面模型
       */
      formViewCreation: {
        data: {
          view: ''
        },
        target: {

        },
        rules: {
          view: [
            { required: true, message: '内容不能为空', trigger: 'blur' }
          ]
        },
        visible: false
      }
    }
  },
  created() {
    this.formApi = new FormApi(this.formName)
  },
  mounted() {
    /**
     * 初始化流程定义数据
     */
    this.formAllViewNames = {}
    this.loadExtraViewAttributes()
    this.formApi.loadDefinition().then(definition => {
      this.formTitle = definition.title
      var allQueries = []
      if (tool.isArray(definition.queries)) {
        allQueries = definition.queries
      }
      if (tool.isArray(definition.otherQueries)) {
        allQueries = allQueries.concat(definition.otherQueries)
      }
      for (var query of allQueries) {
        this.addToFormAllViewNames(
          query.formClass.classPath,
          'query',
          `查询:${query.display || query.name}`
        )
        this.addToFormAllViewNames(
          query.resultClass.classPath,
          'qryrst',
          `查询结果:${query.display || query.name}`
        )
        // 此处调用目的是确保关联界面模型能被自动添加
        this.findAndMergeViewNames(query.formClass)
        this.findAndMergeViewNames(query.resultClass)
      }
      var allActions = []
      if (tool.isArray(definition.actions)) {
        allActions = definition.actions
      }
      if (tool.isArray(definition.otherActions)) {
        allActions = allActions.concat(definition.otherActions)
      }
      for (var action of allActions) {
        this.addToFormAllViewNames(
          action.formClass.classPath,
          'action',
          `事件:${action.display || action.name}`
        )
        if (action.resultClass && action.resultClass.classPath) {
          this.addToFormAllViewNames(
            action.resultClass.classPath,
            'actrst',
            `事件结果:${action.display || action.name}`
          )
        }
        // 此处调用目的是确保关联界面模型能被自动添加
        this.findAndMergeViewNames(action.formClass)
        this.findAndMergeViewNames(action.resultClass)
      }
      this.addToFormAllViewNames(
        definition.formClass.classPath,
        'detail',
        `详情页面`
      )
      // 此处调用目的是确保关联界面模型能被自动添加
      this.findAndMergeViewNames(definition.formClass)
      this.formApi.loadExtraViewNames().then(names => {
        // console.log('获取到的表单关联界面模型清单如下：')
        // console.log(names)
        this.addExtraViewNames(names)
      })
    })
  },
  methods: {
    onGoBack() {
      window.close()
    },

    /**
     * 添加界面模型信息
     */
    addToFormAllViewNames(path, type, name) {
      if (tool.isBlank(path)) {
        return
      }
      if (tool.isBlank(type) || !this.availableTypes.hasOwnProperty(type)) {
        type = 'extra'
      }
      var formViewInfo = this.formAllViewNames[path]
      if (!tool.isPlainObject(formViewInfo)) {
        formViewInfo = {}
        this.formAllViewNames[path] = formViewInfo
      } else if (type === 'extra') {
        // 已经存在的关联界面模型，不在重复添加
        return
      }
      var formViewTypes = formViewInfo.types
      if (tool.isString(formViewTypes)) {
        formViewTypes = [formViewTypes]
      }
      if (!tool.isArray(formViewTypes)) {
        formViewTypes = []
      }
      if (tool.inArray(type, formViewTypes) < 0) {
        formViewTypes.push(type)
      }
      formViewInfo.types = formViewTypes
      var formViewNames = formViewInfo.names
      if (tool.isString(formViewNames)) {
        formViewNames = [formViewNames]
      }
      if (!tool.isArray(formViewNames)) {
        formViewNames = []
      }
      if (tool.inArray(name, formViewNames) < 0) {
        formViewNames.push(name)
      }
      formViewInfo.names = formViewNames
      // 为确保可选项被及时的刷洗，执行数据的复制并重新赋值
      this.formAllViewNames = Object.assign(tool.jsonCopy(this.formAllViewNames))
    },

    /**
     * 添加关联界面模型信息
     */
    addExtraViewNames(extras) {
      // console.log('添加关联界面模型：', extras)
      if (tool.isBlank(extras)) {
        return
      }
      if (!tool.isArray(extras)) {
        extras = [extras]
      }
      for (var i = 0; i < extras.length; i++) {
        this.addToFormAllViewNames(
          extras[i],
          'extra',
          `关联界面模型`
        )
      }
    },

    /**
     * 从清单中移除关联界面模型
     */
    removeSelectedExtraView() {
      const that = this
      const formViewKey = that.formExtraViewAttrs.currentFormClass
      const formViewInfo = that.formAllViewNames[formViewKey]
      if (tool.isBlank(formViewKey) || !formViewInfo) {
        return
      }
      if (formViewInfo.types.length !== 1 || formViewInfo.types[0] !== 'extra') {
        this.$notify.error('只允许移除关联界面模型')
        return
      }
      this.$confirm(`是否确认移当前的关联界面模型 : ${formViewKey} - ${formViewInfo.types.join(', ')} ?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        var removed = []
        var formAllViewNames = {}
        for (var view in that.formAllViewNames) {
          if (view === formViewKey) {
            continue
          }
          removed.push(view)
          formAllViewNames[view] = that.formAllViewNames[view]
        }
        that.formApi.saveExtraViewNames(removed).then(res => {
          that.formAllViewNames = formAllViewNames
          that.formExtraViewAttrs.currentFormClass = ''
          that.formExtraViewAttrs.currentFormModel = null
        })
      })
    },

    /**
     * 加载界面模型数据
     */
    loadExtraViewAttributes(formViewKey) {
      this.formExtraViewAttrs.currentFormClass = formViewKey
      if (tool.isBlank(formViewKey)) {
        return
      }
      this.formExtraViewAttrs['currentFormModel'] = null
      this.formApi.loadViewDefinition(formViewKey).then((formViewData) => {
        this.findAndMergeViewNames(formViewData)
        this.formApi.loadViewAttributes(formViewKey).then((formViewConfig) => {
          formViewData.revision = formViewConfig.revision
          this.formExtraViewAttrs['currentFormModel'] = formViewData
        })
      })
    },

    findAndMergeViewNames(origin) {
      if (!origin || !tool.isPlainObject(origin.properties)) {
        return
      }
      /**
       * 所有发现的关联界面模型，都将被自动添加到可选列表中
       */
      for (var key in origin.properties) {
        if (origin.properties[key].dynamicFilterFormClass &&
          !tool.isBlank(origin.properties[key].dynamicFilterFormClass.classPath)) {
          this.addExtraViewNames(origin.properties[key].dynamicFilterFormClass.classPath)
        }
        if (origin.properties[key].items &&
          !tool.isBlank(origin.properties[key].items.classPath)) {
          this.addExtraViewNames(origin.properties[key].items.classPath)
        } else if (!tool.isBlank(origin.properties[key].classPath)) {
          this.addExtraViewNames(origin.properties[key].classPath)
        }
      }
    },

    /**
     * 界面模型数据更新后的回调
     */
    onFormViewAttributesChanged(attributes, formName, classPath) {
      // this.loadExtraViewAttributes(classPath)
    },

    /**
     * 根据字段定义的位置数据，进行自动的排序
     */
    sortFieldByPosition(formViewData) {
      if (!tool.isPlainObject(formViewData)) {
        return
      }
      var sortArray = []
      var noSortObj = {}
      for (var key in formViewData) {
        if (formViewData[key]['position'] && formViewData[key]['position'] > 0) {
          sortArray.push({
            key: key,
            order: formViewData[key]['position']
          })
        } else {
          noSortObj[key] = formViewData[key]
        }
      }
      var result = {}
      sortArray.sort((n, o) => n.order - o.order)
      sortArray.forEach(t => {
        result[t.key] = formViewData[t.key]
      })
      return Object.assign(result, noSortObj)
    },

    /**
     * 打开关联界面模型添加窗口
     */
    showExtraViewCreationForm() {
      this.formViewCreation.data = {}
      this.formViewCreation.visible = true
    },

    /**
     * 关闭关联界面模型添加窗口
     */
    closeExtraViewCreationForm(saveView) {
      if (!saveView) {
        this.formViewCreation.visible = false
        return
      }
      // console.log('当前添加的视关联界面模型如下：')
      // console.log(this.formViewCreation)
      this.$refs['viewCreationForm'].validate(valid => {
        if (!valid) {
          return
        }
        var formViewKey = this.formViewCreation.data.view
        if (this.formAllViewNames[formViewKey]) {
          this.$notify.error('要添加的关联界面模型已经存在，请勿重复添加！')
          return
        }
        var loading = Loading.service({
          fullscreen: true,
          text: '请求中…',
          background: 'rgba(0, 0, 0, 0.1)'
        })
        var extraViewNames = [formViewKey]
        for (var view in this.formAllViewNames) {
          var formViewInfo = this.formAllViewNames[view]
          if (formViewInfo.types === 1 && formViewInfo.types[0] === 'extra') {
            extraViewNames.push(view)
          }
        }
        this.formApi.saveExtraViewNames(extraViewNames).then(res => {
          this.closeExtraViewCreationForm()
          this.addExtraViewNames(formViewKey)
          this.loadExtraViewAttributes(formViewKey)
        }).finally(e => {
          loading.close()
        })
      })
    }
  }
}
</script>
<style lang='scss'>
.form-setup {
  .el-dialog__header {
    border-bottom: 1px solid #ddd;
  }
  .el-input {
    display: initial !important;
  }
  .width600 .el-input__inner{
    width: 600px !important;
  }
}
</style>
