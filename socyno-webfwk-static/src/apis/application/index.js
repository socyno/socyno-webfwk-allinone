import request from '@/utils/request'

export function getApplication(data) {
  return request({
    url: '/form/list/application/withTotal',
    method: 'post',
    data
  })
}

export function getChangeMine(data) {
  return request({
    url: '/vcs/changes/mine',
    method: 'get',
    data
  })
}

export function getSubsystem(data) {
  return request({
    url: '/form/list/subsystem/withTotal',
    method: 'post',
    data
  })
}

// 收藏
export function addToFav(appId) {
  return request({
    url: '/user/app/bookmark/add/' + appId,
    method: 'post'
  })
}

export function removeFromFav(appId) {
  return request({
    url: '/user/app/bookmark/delete/' + appId,
    method: 'post'
  })
}

export function getAllBranches(appId, page) {
  return request({
    url: `/vcs/unified/branch/list/${appId}`,
    method: 'get',
    params: page
  })
}

export function getAllTags(appId, page) {
  return request({
    url: `/vcs/unified/tag/list/${appId}`,
    method: 'get',
    params: page
  })
}

export function getAllPatches(appId, page) {
  return request({
    url: `/vcs/unified/patch/list/${appId}`,
    method: 'get',
    params: page
  })
}

export function removeBranch(appId, name) {
  return request({
    url: `/vcs/unified/ref/delete/${appId}`,
    method: 'post',
    data: { name }
  })
}

export function getDirs(appId, dirPath, revision) {
  return request({
    url: `/vcs/unified/file/dirs/get/${appId}`,
    method: 'get',
    params: { dirPath, revision }
  })
}

export function getFileContent(appId, filePath, revision) {
  return request({
    url: `/vcs/unified/file/content/get/${appId}`,
    method: 'get',
    params: { filePath, revision }
  })
}

export function getApplicationCommitHistory(appId, page, vcsRefsName) {
  return request({
    url: `/vcs/changes/application/${appId}`,
    method: 'get',
    params: Object.assign(page, { vcsRefsName })
  })
}

export function getAllAppList(data) {
  return request({
    url: '/sys/applications/list',
    method: 'get',
    params: data
  })
}

export function getVisibleAppList(data) {
  return request({
    url: '/sys/applications/visible',
    method: 'get',
    params: data
  })
}

export function getAppSummary(appid) {
  return request({
    url: `/sys/applications/${appid}/simple`,
    method: 'get'
  })
}

export function getAppContainerConfigs(appid) {
  return request({
    url: `/sys/applications/${appid}/container/configs`,
    method: 'get'
  })
}

export function getAppDeployNamespaces(appid) {
  return request({
    url: `/sys/applications/${appid}/deploy/namespaces`,
    method: 'get'
  })
}

export function getAppOptions(keyword) {
  return request({
    url: `/sys/options/application`,
    method: 'get',
    params: { namelike: keyword }
  })
}

export function getSubsystemOptions(keyword) {
  return request({
    url: `/sys/options/subsystem`,
    method: 'get',
    params: { namelike: keyword }
  })
}

export function getUserOptions(keyword) {
  return request({
    url: `/sys/options/user`,
    method: 'get',
    params: { namelike: keyword }
  })
}

export function getAppEnv(applicationId) {
  return request({
    url: `/sys/options/appenv/${applicationId}`,
    method: 'get'
  })
}

export function getAppState(applicationId, environment) {
  return request({
    url: `/sys/applications/${applicationId}/status/${environment}`,
    method: 'get'
  })
}

export function getProductlineOptions(data) {
  return request({
    url: `/sys/productlines/list`,
    method: 'get',
    params: data
  })
}

export function getSubsystemInfo(subsystemId) {
  return request({
    url: `/sys/subsystems/${subsystemId}/simple`,
    method: 'get'
  })
}

export function createBranch(type, appId, data) {
  return request({
    url: `/vcs/unified/${type}/create/${appId}`,
    method: 'post',
    data: data
  })
}
