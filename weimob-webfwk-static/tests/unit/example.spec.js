import { shallowMount, createLocalVue } from '@vue/test-utils'
import VueRouter from 'vue-router'
import Element from 'element-ui'
import HelloWorld from '@/views/errorPage/404.vue'
const localVue = createLocalVue()
localVue.use(VueRouter)
localVue.use(Element)

describe('404.vue', () => {
  it('is vue instance', () => {
    const wrapper = shallowMount(HelloWorld, {
      localVue
    })
    expect(wrapper.isVueInstance()).toBeTruthy()
  })
})
