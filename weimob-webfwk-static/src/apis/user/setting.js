import request from '@/utils/request'

// ssh list
export function getSSHKeyList() {
  return request({
    url: '/vcs/unified/key/ssh/get',
    method: 'get'
  })
}

// ssh list
export function addSSHKey(form) {
  return request({
    url: '/vcs/unified/key/ssh/add',
    data: form,
    method: 'post'
  })
}

// ssh list
export function deleteSSHKey(sshid) {
  return request({
    url: '/vcs/unified/key/ssh/delete/' + sshid,
    method: 'post'
  })
}

// svn pwd reset
export function svpPwdReset(form) {
  return request({
    url: '/vcs/unified/password/reset',
    data: form,
    method: 'post'
  })
}
