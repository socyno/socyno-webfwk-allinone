import { shallowMount } from '@vue/test-utils'
import Component from '@/views/dashboard'
// TODO: https://vue-test-utils.vuejs.org/zh/guides/#%E5%9C%A8%E7%BB%84%E4%BB%B6%E4%B8%AD%E6%B5%8B%E8%AF%95-vuex
describe(Component.name, () => {
  it('is vue instance', () => {
    const wrapper = shallowMount(Component)
    expect(wrapper.isVueInstance()).toBeTruthy()
  })
})
