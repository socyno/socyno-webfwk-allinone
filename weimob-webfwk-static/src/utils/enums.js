var AllEnumMappers = {
  ApplicationType: {
    'application': '服务端',
    'client': '客户端',
    'library': '组件库',
    'static': '静态资源'
  },
  ApplicationState: {
    created: '新创建',
    online: '服务中',
    offlining: '下线中',
    offlined: '已下线'
  },
  ApplicationCodeLevel: {
    '30:blue': '蓝',
    '20:yellow': '黄',
    '10:red': '红'
  }
}

var enums = {

  getDisplay: function(e, v) {
    return AllEnumMappers[e][v]
  },

  getDisplayOrValue: function(e, v) {
    return AllEnumMappers[e][v] || v
  },

  get: function(e) {
    return AllEnumMappers[e]
  }
}

export default enums
