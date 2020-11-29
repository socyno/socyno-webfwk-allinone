import request from '../utils/request'

class MonitorViewApi {
  /**
   * @param {object} data
   */
  createMonitorLabel(data) {
    return request({
      url: `/monitorLabel/label/create`,
      method: 'post',
      data: data
    })
  }

  /**
   * @param {object} data
   */
  editMonitorLabel(data) {
    return request({
      url: `/monitorLabel/label/edit`,
      method: 'post',
      data: data
    })
  }

  /**
   * @param id
   */
  deleteMonitorLabelById(id) {
    return request({
      url: `/monitorLabel/label/delete/${id}`,
      method: 'get'
    })
  }

  /**
   * @param id
   */
  innitAssociationMonitorLabelData(id) {
    return request({
      url: `/monitorLabel/item/label/list/${id}`,
      method: 'get'
    })
  }

  /**
   * @param {object} data
   */
  saveMonitorLabelSelect(data) {
    return request({
      url: `/monitorLabel/item/label/list/save`,
      method: 'post',
      data: data
    })
  }

  /**
   * @param {object} data
   */
  moveMonitorViewGroupItem(data) {
    return request({
      url: `/monitorView/group/item/move`,
      method: 'post',
      data: data
    })
  }

  /**
   * @param id
   */
  innitMonitorViewItemSelect(id, filter, productId) {
    return request({
      url: `/monitorView/group/item/innitSelect`,
      method: 'get',
      params: { id: id, filter: filter, productId: productId }
    })
  }

  /**
   * @param {object} data
   */
  saveMonitorViewItemSelect(data) {
    return request({
      url: `/monitorView/group/item/save`,
      method: 'post',
      data: data
    })
  }

  /**
   * @param id
   */
  loadMonitorViewGroupThree(filterParams) {
    return request({
      url: `/monitorView/groupAndItem/list`,
      method: 'post',
      data: filterParams
    })
  }

  /**
   * 创建目录
   * @param {object} data
   */
  createMonitorViewGroup(data) {
    return request({
      url: `/monitorView/group/add`,
      method: 'post',
      data: data
    })
  }

  /**
   * 编辑目录
   * @param {object} data
   */
  editMonitorViewGroup(data) {
    return request({
      url: `/monitorView/group/edit`,
      method: 'post',
      data: data
    })
  }

  /**
   * 删除目录
   * @param {object} data
   */
  removeMonitorViewItem(data) {
    return request({
      url: `/monitorView/item/remove`,
      method: 'post',
      data: data
    })
  }

  /**
   * 删除目录
   * @param {object} data
   */
  deleteMonitorViewGroup(data) {
    return request({
      url: `/monitorView/group/delete`,
      method: 'post',
      data: data
    })
  }

  /**
   * 删除目录，并移除其及其子目录下的所有监控下
   * @param {object} data
   */
  deleteMonitorViewGroupWithContinue(data) {
    return request({
      url: `/monitorView/group/deleteWithContinue`,
      method: 'post',
      data: data
    })
  }

  loadFormListWithTotal(data) {
    return request({
      url: `/monitorLabel/label/list/withTotal`,
      method: 'post',
      data: data
    })
  }

  /**
   * id
   */
  loadMonitorViewItemDetail(id) {
    return request({
      url: `/monitorView/item/detail/${id}`,
      method: 'get'
    })
  }

  /**
   * id
   */
  deleteMonitorViewItem(id) {
    return request({
      url: `/monitorView/item/one/delete/${id}`,
      method: 'get'
    })
  }

  loadMonitorViewItemApplyHistory(data) {
    return request({
      url: `/monitorView/item/apply/history`,
      method: 'POST',
      data: data
    })
  }

  /**
   * @param reportName
   * @param serverName
   */
  loadItemReport(reportName, serverName) {
    return request({
      url: `/monitorView/item/report/info`,
      method: 'get',
      params: { reportName: reportName, serverName: serverName }
    })
  }

  /**
   * @param {object} data
   */
  loadMonitorDetailExportData(data) {
    return request({
      url: `/monitorView/item/detail/export`,
      method: 'post',
      data: data
    })
  }
}
export default MonitorViewApi
