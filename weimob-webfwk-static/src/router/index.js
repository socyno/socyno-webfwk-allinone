import Vue from 'vue'
import Router from 'vue-router'
import formRouter from './modules/form'
import Layout from '@/views/appLayout/main.vue'
import userRouter from './modules/user'
import appRouter from './modules/project'
import store from '@/store'
import tool from '@/utils/tools'

Vue.use(Router)

export const defaultRouters = [{
  path: '/',
  component: Layout,
  redirect: '/dashboard',
  children: [
    {
      path: 'dashboard',
      component: () => import('@/views/index/index'),
      name: 'Dashboard',
      meta: { title: '首页' }
    }
  ]
},
{
  path: '/index',
  component: Layout,
  children: [
    {
      path: '/',
      component: () => import('@/views/index/index'),
      name: 'Index',
      meta: { title: '首页' }
    },
    {
      path: '/dev',
      component: () => import('@/views/dev'),
      name: 'Dev',
      meta: { title: 'Dev' }
    }
  ]
},
{
  path: '/login',
  component: () => import('@/views/login/index'),
  name: 'Login',
  meta: { title: '登录' }
},
{
  path: '*',
  component: Layout,
  redirect: '/404',
  children: [{
    path: '404',
    component: () => import('@/views/errorPage/404'),
    name: '404',
    props: true,
    meta: { title: '404' }
  }]
}]

const RouterConfig = {
  routes: [...defaultRouters, formRouter, userRouter, appRouter]
}

const router = new Router(RouterConfig)

export default router

const prefixTitle = window.$title + ' - '
router.beforeEach((to, from, next) => {
  if (to.name === '404') {
    next()
    return
  }
  // 携带 ticket 访问，或者未获取到用户的 token 数据时，跳转到登陆页面
  var ssoTicket = ''
  if (tool.isNotBlank(ssoTicket = tool.getUrlFirstParamByName('ticket')) ||
      (to.name !== 'Login' && !store.getters['user/getToken']())) {
    var localUser = tool.getUrlFirstParamByName('localUser')
    console.log(location.href)
    next({ path: '/Login', query: {
      redirect: to.fullPath,
      ticket: ssoTicket,
      service: tool.getUrlFirstParamByName('service'),
      localUser: localUser
    }})
    return
  }
  if (to.name === 'Login' && from.name !== 'Login' && !to.query.redirect) { // 跳登录把redirect页面带过去
    next({ path: '/Login', query: { redirect: from.fullPath }})
    return
  }
  if (to.name === 'Dashboard') {
    store.commit('uiControl/setCurrentMenuInit')
  }
  document.title = prefixTitle + (to.meta.title || to.query.name)
  // eslint-disable-next-line
  try {
    var appWrapper = document.body
    var wrapperChildren = appWrapper.children
    for (var c = wrapperChildren.length - 1; c >= 0; c--) {
      var dom = wrapperChildren[c]
      if (tool.toLower(dom.tagName) === 'div') {
        if (dom.id === 'app' || dom.className.indexOf('el-menu') >= 0) {
          continue
        }
        appWrapper.removeChild(dom)
      }
    }
  } catch (error) {
    // no console
  }

  next()
})
