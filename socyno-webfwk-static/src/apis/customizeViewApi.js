import request from '../utils/request'
import tool from '@/utils/tools'

class CustomizeViewApi {
  /**
   * @param id
   */
  innitProductionChangeInfo(paramData) {
    return request({
      url: `/project/production/change/info/list`,
      method: 'post',
      data: paramData
    })
  }

  innitDeployItemInfo(paramData) {
    if (!tool.isNullOrUndef(paramData.deployItemType) &&
        paramData.deployItemType.indexOf('deploy_item') >= 0) {
      paramData.deployItemType = []
    }
    return request({
      url: `/project/deploy/item/info/list`,
      method: 'post',
      data: paramData
    })
  }
}
export default CustomizeViewApi
