import tool from '../utils/tools'
import request from '../utils/request'
import { parseFormClass, convertOptionToValue } from '../utils/formUtils'

class FormApi {
  /**
   * 获取所有的已注册通用流程单
   */
  static loadDefinedForms() {
    return request({
      url: `/form/form/list`,
      method: 'get'
    }).then(res => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 注册新的通用流程单
   */
  static addDefinedForm(data) {
    return request({
      url: `/form/form/add`,
      method: 'post',
      data
    })
  }

  /**
   * 修改已注册的通用流程单
   */
  static updateDefinedForm(data) {
    return request({
      url: `/form/form/update`,
      method: 'post',
      data
    })
  }

  // 禁用/启动通用流程单
  static disableDefinedForm(formName) {
    return request({
      url: `/form/form/toggle/${formName}`,
      method: 'post'
    })
  }

  // 删除通用流程单
  static deleteDefinedForm(formName) {
    return request({
      url: `/form/form/delete/${formName}`,
      method: 'post'
    })
  }

  /**
   * 传formname构建请求对象
   * @param {string} formName
   */
  constructor(formName) {
    if (tool.isBlank(formName)) {
      throw new Error('Form name is required')
    }
    this.formName = formName
  }

  /**
   * 获取当前的表单名称
   */
  getFormName() {
    return this.formName
  }

  /**
   * 处理definetions
   * @param {object} res 入参传网络请求的res
   * @return 返回解析好queryclass和resultclass的结果
   */
  static parseDefinetion(definition) {
    if (!definition || tool.isBlank(definition.formClass) || tool.isUndefOrNull(definition.name) || tool.isUndefOrNull(definition.queries)) {
      throw new Error('Invalid form definition data responsed')
    }
    // console.log('流程定义响应数据结构如下:', definition)
    for (var q = 0; q < definition.queries.length; q++) {
      var query = definition.queries[q]
      // console.log('    解析流程查询:', query)
      query.formClass = parseFormClass(query.formClass)
      query.resultClass = parseFormClass(query.resultClass)
      definition.queries[q] = query
    }
    definition.quickActions = []
    if (definition.actions) {
      for (var a = definition.actions.length - 1; a >= 0; a--) {
        definition.actions[a] = FormApi.parseFormAction(definition.actions[a])
        if (tool.isUndef(definition.allownActions) || tool.inArray(definition.actions[a].name, definition.allownActions) >= 0) {
          definition.quickActions.push(definition.actions[a])
        }
      }
    }
    if (definition.otherActions) {
      for (var o = definition.otherActions.length - 1; o >= 0; o--) {
        definition.otherActions[o] = FormApi.parseFormAction(definition.otherActions[o])
      }
    }
    if (tool.isString(definition.properties) && !tool.isBlank(definition.properties)) {
      try {
        definition.properties = JSON.parse(definition.properties)
      } catch (e) {
        console.error('流程属性定义数据解析失败：', e)
      }
    }
    if (!tool.isPlainObject(definition.properties)) {
      definition.properties = {}
    }
    definition.formClass = parseFormClass(definition.formClass)
    // console.log('完成流程响应数据结构解析')
    return definition
  }

  /**
   * 解析并检查请求到的流程事件定义
   */
  static parseFormAction(action) {
    action.formClass = parseFormClass(action.formClass)
    action.resultClass = parseFormClass(action.resultClass)
    action.formClass.eventFormType = action.eventFormType
    action.resultClass.eventFormType = action.eventFormType
    action.styleType = 'primary'
    var lowerName = tool.toLower(action.name)
    if (lowerName.indexOf('cancel') >= 0 || lowerName.indexOf('close') >= 0) {
      action.styleType = 'info'
    } else if (lowerName.indexOf('reject') >= 0 || lowerName.indexOf('delete') >= 0) {
      action.styleType = 'danger'
    }
    return action
  }

  /**
   * 根据fieldTypeKey获取options
   * @param {string} key fieldTypeKey
   * @param {string} keyword 筛选关键词
   */
  loadFormFieldOptionsWithQuery(key, keyword, formId, query) {
    return request({
      url: `form/field/${this.formName}/${key}/options/withQuery`,
      method: 'post',
      data: Object.assign({
        'keyword': keyword,
        'formName': this.formName,
        'formId': formId
      }, convertOptionToValue(query))
    })
  }
  /**
   * 获取流程表单的定义信息
   */
  loadDefinition() {
    return request({
      url: `form/definition/${this.formName}`,
      method: 'get'
    })
      .then(res => {
        // console.log('请求流程定义信息完成，开始处理响应数据:', this.formName)
        return Promise.resolve(FormApi.parseDefinetion(res.data))
      })
      .catch(res => {
        return Promise.reject(res)
      })
  }

  /**
   * 获取指定的界面视图模型
   */
  loadViewAttributes(formViewKey) {
    return request({
      url: `/form/form/${this.formName}/${formViewKey}/viewattrs`,
      method: 'get'
    })
      .then(res => {
        return Promise.resolve(res.data || { revision: -1 })
      })
      .catch(res => {
        return Promise.reject(res)
      })
  }

  /**
   * 保存指定的界面视图模型数据
   */
  saveViewAttributes(formViewKey, formViewData, formViewRevision) {
    return request({
      url: `/form/form/${this.formName}/${formViewKey}/viewattrs/update`,
      method: 'post',
      params: { revision: formViewRevision },
      data: formViewData
    })
      .then(res => {
        return Promise.resolve(res.data)
      })
      .catch(res => {
        return Promise.reject(res)
      })
  }

  /**
   * 保存表单的关联视图模型清单
   */
  loadExtraViewNames() {
    return request({
      url: `/form/form/${this.formName}/extraviews`,
      method: 'get'
    })
      .then(res => {
        if (res.data && !tool.isArray(res.data)) {
          return Promise.reject(res)
        }
        return Promise.resolve(res.data || [])
      })
      .catch(res => {
        return Promise.reject(res)
      })
  }

  /**
   * 保存表单的关联视图模型清单
   */
  saveExtraViewNames(extraViewNames) {
    return request({
      url: `/form/form/${this.formName}/extraviews/update`,
      method: 'post',
      data: extraViewNames
    })
  }

  /**
   * 获取指定名称的视图界面模型数据
   */
  loadViewDefinition(formViewKey) {
    return request({
      url: `/form/form/${this.formName}/${formViewKey}/construction`,
      method: 'get'
    }).then(res => {
      if (!tool.isString(res.data)) {
        return Promise.reject(res)
      }
      return Promise.resolve(parseFormClass(res.data))
    })
  }

  /**
   * 验证界面视图模型数据并响应完整结构
   */
  preViewAttributes(formViewKey, formViewData) {
    return request({
      url: `/form/form/${this.formName}/${formViewKey}/viewattrs/preview`,
      method: 'post',
      data: formViewData
    }).then((res) => {
      if (!tool.isString(res.data)) {
        return Promise.reject(res)
      }
      return Promise.resolve(parseFormClass(res.data))
    })
  }

  /**
   * 查询表单列表数据，结果集中包含查询结果的总条数
   * @param {object} params
   */
  queryFormPagedData(query, params, withTotal) {
    return request({
      url: `form/list/${this.formName}/${query}${withTotal ? '/withTotal' : ''}`,
      method: 'post',
      data: convertOptionToValue(params)
    }).then((res) => {
      if (!res.data || !tool.isNumber(res.data.page) ||
           !tool.isNumber(res.data.limit) ||
           (res.data.list && !tool.isArray(res.data.list))) {
        return Promise.reject(res)
      }
      return Promise.resolve(res.data)
    })
  }

  /**
   * 执行表单一般事件
   * @param {string} formAction 事件的名称
   * @param {object} data       事件的表单数据
   */
  triggerAction(formAction, data) {
    return request({
      url: `form/trigger/${this.formName}/${formAction}`,
      method: 'post',
      data: convertOptionToValue(data)
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 执行表单创建事件
   * @param {string} formName
   */
  loadFlowChart(formId, type) {
    return request({
      url: `/form/form/${this.formName}/flowchart/definition`,
      method: 'get',
      params: { formId: formId, 'unchanged': type === 'unchanged' }
    })
  }

  static parseCommonFormDetail(data) {
    if (data && data.formClass) {
      data['formClass'] = parseFormClass(data.formClass)
    }
    if (data && data.actions) {
      for (var a = data.actions.length - 1; a >= 0; a--) {
        FormApi.parseFormAction(data.actions[a])
      }
    }
    return data
  }

  /**
   * 查询流程表单的详情数据及其定义
   */
  loadDetailWithActions(formId, extendParams) {
    if (!tool.looksLikeInteger(formId)) {
      throw new Error('Invalid form id provided : ' + formId)
    }
    return request({
      url: `/form/get/${this.formName}/${formId}/withActions`,
      method: 'get',
      params: extendParams
    }).then((res) => {
      return Promise.resolve(FormApi.parseCommonFormDetail(res.data))
    })
  }

  /**
   * 加载表单的讨论列表
   * @param {Integer} formId
   * @param {Integer} fromIndex
   */
  loadComments(formId, fromIndex) {
    var params = {}
    if (fromIndex) {
      params['fromCommentId'] = fromIndex
    }
    if (!tool.looksLikeInteger(formId)) {
      throw new Error('IllegalArguments: Invalid form id provided ' + formId)
    }
    return request({
      url: `/form/comments/${this.formName}/${formId}`,
      method: 'get',
      params: params
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }
  /**
   * 加载表单的操作日志
   * @param {Integer} formId
   * @param {Integer} fromIndex
   */
  loadFormActionLogs(formId, fromIndex) {
    var params = {}
    if (fromIndex) {
      params['fromLogIndex'] = fromIndex
    }
    if (!tool.looksLikeInteger(formId)) {
      throw new Error('IllegalArguments: the form id is missing')
    }
    return request({
      url: `/form/logs/${this.formName}/${formId}`,
      method: 'get',
      params: params
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }
  /**
   * 加载表单操作日志的详细数据
   * @param {Integer} formId
   * @param {Integer} detailId
   */
  loadFormActionLogDetail(formId, detailId) {
    if (tool.isBlank(formId) || tool.isBlank(detailId)) {
      throw new Error('IllegalArguments: the form id or log detail id is missing')
    }
    return request({
      url: `/form/logs/${this.formName}/${formId}/${detailId}/detail`,
      method: 'get'
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 加载事件动态数据
   * @param {Integer} formId
   * @param {String} formAction
   */
  loadActionPrepareData(formId, formAction, params) {
    if (!tool.looksLikeInteger(formId) || tool.isBlank(formAction)) {
      throw new Error('IllegalArguments: the form id invalid or action is missing')
    }
    return request({
      url: `/form/prepare/${this.formName}/${formId}/${formAction}`,
      method: 'get',
      params: params
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 获取当前表单的事件
   * @param formId
   * @returns {Promise<unknown>}
   */
  loadFormActionSimple(formId) {
    return request({
      url: `/form/get/${this.formName}/${formId}/actions/simple`,
      method: 'get'
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 创建表单快照
   * @param {String} formId
   * @param {String} title
   * @param {Object} data
   */
  createSnapshotData(formId, title, data) {
    return request({
      url: `form/snapshot/${this.formName}/${formId}/create`,
      method: 'post',
      params: {
        title: title
      },
      data: data
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 加载表单的快照数据
   * @param {String} formId
   * @param {String} snapshotId
   */
  loadSnapshotData(formId, snapshotId) {
    return request({
      url: `form/snapshot/${this.formName}/${formId}/get/${snapshotId}`,
      method: 'get'
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }

  /**
   * 查询表单的快照记录
   * @param {String} formId
   * @param {Object} params
   */
  listSnapshotRecords(formId, params) {
    return request({
      url: `form/snapshot/${this.formName}/${formId}/list`,
      method: 'get',
      params: params
    }).then((res) => {
      return Promise.resolve(res.data)
    })
  }
}
export default FormApi
