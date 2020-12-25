import request from '@/utils/request'

export function getMenuList() {
  return request({
    url: '/user/menus',
    method: 'get'
  })
}

