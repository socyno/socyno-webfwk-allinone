import Layout from '@/views/appLayout/main.vue'

const formRouter = {
  path: '/form',
  name: 'form',
  component: Layout,
  children: [
    {
      path: 'flowchart/:formName',
      name: 'FlowChart',
      meta: { title: '通用表单流程图' },
      component: () => import('@/views/form/flowchart.vue')
    },
    {
      path: 'list/:formName',
      name: 'List',
      meta: { title: '表单列表' },
      component: () => import('@/views/form/list.vue')
    },
    {
      path: 'detail',
      name: 'Detail',
      meta: { title: '表单详情' },
      component: () => import('@/views/form/detail.vue')
    },
    {
      path: 'create/:formName/:formAction',
      name: 'Create',
      meta: { title: '表单创建' },
      component: () => import('@/views/form/create.vue')
    },
    {
      path: 'setup/list',
      name: 'SetupList',
      meta: { title: '表单列表' },
      component: () => import('@/views/form/setupList.vue')
    },
    {
      path: 'setup/:formName',
      name: 'Setup',
      meta: { title: '表单设置' },
      component: () => import('@/views/form/setup.vue')
    },
    {
      path: 'result/:resultType',
      name: 'Result',
      meta: { title: '操作结果' },
      component: () => import('@/views/form/result.vue')
    }
  ]
}
export default formRouter
