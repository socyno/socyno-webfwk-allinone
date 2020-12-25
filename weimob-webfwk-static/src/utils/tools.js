const tool = {
  /**
   * 移除已知的URL前缀
   * @param {Object} url
   */
  baseUrlReplace(url) {
    url = url.replace('http://devops.weimob.com', '')
      .replace('https://devops.weimob.com', '')
    return url
  },

  /**
   * 确认是否为 Firefox 浏览器
   */
  isFirefox() {
    return navigator.userAgent.toLowerCase().indexOf('firefox') > -1
  },

  /**
   * 解析 JSON 字串为数据. 当传入两个参数时，如果将第一个参数作为JSON字串解析时失败，则返回第二个参数的值。
   */
  fromJson(json, dflt) {
    if (arguments.length > 1) {
      try {
        return JSON.parse(json)
      } catch (e) {
        return dflt
      }
    }
    return JSON.parse(json)
  },

  /**
   * 序列化数据为 JSON 字串
   */
  toJson(data) {
    return JSON.stringify(data)
  },

  /**
   * 基于 JSON 的数据复制
   * @param {Object} data
   */
  jsonCopy(data) {
    if (this.isUndef(data) || this.isNull()) {
      return data
    }
    return JSON.parse(JSON.stringify(data))
  },

  /**
   * 获取并设置页面标题
   * @param {String} subtitle
   */
  title(subtitle, apply) {
    var title = (window.$title || '微盟效能后端开发框架') + ' - ' + this.stringify(subtitle)
    if (apply === true) {
      document.title = title
    }
    return title
  },

  /**
   * 关闭当前窗口
   */
  close() {
    window.location.href = 'about:blank'
    window.close()
  },

  /**
   * 检查页面是否嵌套在 IFRAME 中
   */
  inIframe() {
    try {
      return window.self !== window.top
    } catch (e) {
      return true
    }
  },

  /**
   * 设置地址栏的HASH
   * @param {Object} params
   */
  setUrlHash(path, params) {
    var hasUrl = path
    if (params && this.isPlainObject(params)) {
      hasUrl += '?' + Object.keys(params).map(key => {
        return (
          tool.encodeURI(key) + '=' + tool.encodeURI(params[key])
        )
      }).join('&')
    }
    history.replaceState({}, null, '#' + this.remove(/^#+/, hasUrl))
  },

  /**
   * 检测给定对象是否为字符串
   * @param {Object} str
   */
  isString(str) {
    return typeof str === 'string'
  },

  /* 确认对象是否为数字 */
  isNumber(data) {
    return typeof (data) === 'number' && !isNaN(data)
  },

  /**
   * 确认对象是否为 undefined
   * @param {Object} obj
   */
  isUndef(obj) {
    return typeof obj === 'undefined'
  },

  /**
   * 确认对象是否为 null
   * @param {Object} obj
   */
  isNull(obj) {
    if (obj === null) {
      return true
    }
    return typeof obj === 'object' && !obj
  },

  /**
   * 确认对象是否为 null 或 undefined
   * @param {Object} v
   */
  isUndefOrNull(v) {
    return this.isUndef(v) || this.isNull(v)
  },

  /**
   * 确认对象是否为 null 或 undefined，等同于 isUndefOrNull
   * @param {Object} v
   */
  isNullOrUndef(v) {
    return this.isUndefOrNull(v)
  },

  /**
   * 对象强制字串化
   * @param {Object} str
   */
  stringify(str) {
    if (this.isUndefOrNull(str)) {
      return ''
    }
    return str + ''
  },

  /**
   * 强转小写字串
   * @param {String} str
   */
  toLower(str) {
    return this.stringify(str).toLowerCase()
  },

  /**
   * 强转大写字串
   * @param {String} str
   */
  toUpper(str) {
    return this.stringify(str).toUpperCase()
  },

  /**
   * 对象强制字串化并去除首位空白
   */
  trim2str(str) {
    return this.trim(str)
  },

  /**
   * 对象强制字串化并去除首位空白
   */
  trim(str) {
    return this.remove(/^\s+|\s+$/, str)
  },

  /* 将匹配值移除，支持正则表达式 */
  remove(regex, str) {
    return this.stringify(str).replace(regex, '')
  },

  /**
   * 移除匹配的内容，如果移除成功则返回移除后的值，否则返回null
   */
  removeOrNull(regex, str) {
    str = this.stringify(str)
    var rmstr = str.replace(regex, '')
    return rmstr === str ? null : rmstr
  },

  /**
   * 检查是否以指定字串结尾
   */
  endsWith(str, endsWith) {
    if ((endsWith = this.stringify(endsWith)).length <= 0) {
      return false
    }
    if ((str = this.stringify(str)).length <= 0 || endsWith.length > str.length) {
      return false
    }
    return str.substr(0 - endsWith.length, endsWith.length) === endsWith
  },

  /* 检测是否为空字符串(undefined/null/empty) */
  isEmpty(v) {
    return this.stringify(v).length === 0
  },

  /**
   * 检测是否为空白字符串
   */
  isBlank(str) {
    return this.trim2str(str).length === 0
  },

  /**
   * 当字串为空白时，返回默认值，否则返回输入字串
   */
  ifBlank(val, def) {
    return this.isBlank(val) ? def : val
  },

  /**
   * 检测给定对象是否为 Boolean 值
   */
  isBoolean(obj) {
    return typeof obj === 'boolean'
  },

  /**
   * 检测给定对象似整形数字
   */
  looksLikeInteger(data) {
    return !!(((this.isNumber(data) || this.isString(data)) &&
      (data + '').match(/^[\-|\+]?\d+$/)))
  },

  /**
   * 确认对象是否为函数
   * @param {Object} func
   */
  isFunction(func) {
    return typeof func === 'function'
  },

  /**
   * 判断是否为标准数组，参考 jQuery 的实现
   */
  isArray(data) {
    if (this.isUndefOrNull(data)) {
      return false
    }
    if (Array.isArray) {
      return Array.isArray(data)
    }
    return this.toLower(data) === '[object array]'
  },

  /**
   * 确认是否被数组包含
   * @param {Object} val 检测值
   * @param {Array} array 检测数组
   * @param {Function} checker 比较函数，默认为  ===
   * @return 首个匹配的数组下标，否则 -1
   */
  inArray(val, arr, checker) {
    if (!this.isFunction(checker)) {
      checker = function(a, b) {
        return a === b
      }
    }
    if (this.isArray(arr) && arr.length > 0) {
      for (var i = 0; i < arr.length; i++) {
        if (checker.call(this, arr[i], val)) {
          return i
        }
      }
    }
    return -1
  },

  /**
   * 判断是否为纯粹对象，参考 jQuery 的实现
   */
  isPlainObject(obj) {
    // 先确认是有效的对象
    if (!obj || this.toLower(obj) !== '[object object]') {
      return false
    }

    // 获取对象原型
    var protoType = Object.getPrototypeOf(obj)

    // 如果对象没有原型，那也算纯粹的对象，比如用 Object.create(null) 这种方式创建的对象
    if (!protoType) {
      return true
    }

    // 最后判断是不是通过 "{}" 或 "new Object" 方式创建的对象：
    //       判断给定对象的构造函数是否与 Object 的相同
    var constructor = Object.hasOwnProperty.call(protoType, 'constructor') && protoType.constructor

    // 通过调用 Function.prototype.toString 方法来确认构造器函数是否返回相同的字串值：类似 - "function Object() { [native code] }"
    var fun2String = Object.hasOwnProperty.toString
    return typeof constructor === 'function' && fun2String.call(constructor) === fun2String.call(Object)
  },

  /**
   * default if a === b
   * @param {Object} a
   * @param {Object} b
   * @param {Object} deflt
   * @return a === b ? default : a
   */
  defaultIfEquals(a, b, deflt) {
    return a === b ? deflt : a
  },

  /* check own property */
  hasOwnProperty(obj, pro) {
    return this.isUndef(obj) ? false : Object.hasOwnProperty.call(obj, pro)
  },

  /**
   * 强制转数字，否则返回 NaN, 或给定的默认值
   * @param {Object} number
   * @param {Object} default
   */
  parseInteger(number, dflt) {
    var result = NaN
    try {
      result = parseInt(number)
    } catch (err) {
      // No Warn
    }
    return isNaN(result) && !this.isUndefOrNull(dflt) ? dflt : result
  },

  /**
   * 比较字串是否相同(可支持大小写忽略及去除首位空白)
   */
  stringEquals(a, b, ignoreCase, trimed) {
    a = this.stringify(a)
    b = this.stringify(b)
    if (trimed) {
      a = this.trim(a)
      b = this.trim(b)
    }
    if (ignoreCase) {
      a = a.toLowerCase()
      b = b.toLowerCase()
    }
    return a === b
  },

  /**
   * 比较字串是否相同(强制大小写忽略)
   */
  stringEqualsIgnoreCase(a, b) {
    return this.stringEquals(a, b, true, false)
  },

  /**
   * 比较字串是否相同(强制去除首位空白)
   */
  stringEqualsTrimed(a, b) {
    return this.stringEquals(a, b, false, true)
  },

  /**
   * 比较字串是否相同(强制大小写忽略并去除首位空白)
   */
  stringEqualsTrimedIgnoreCase(a, b) {
    return this.stringEquals(a, b, true, true)
  },

  /**
   * 换行转 <br/>
   */
  htmlLineBreak(str) {
    return this.stringify(str).replace(/\r?\n/g, '<br/>')
  },

  /**
   * HTML 转义（ & " < >）
   */
  escape(data, space) {
    var text = this.stringify(data).replace(/&/g, '&amp;')
      .replace(/>/g, '&gt;')
      .replace(/</g, '&lt;')
      .replace(/\"/g, '&quot;')
    /* " */
    if (space) {
      // eslint-disable-next-line
      text = text.replace(/  /g, ' &nbsp;')
    }
    return text
  },

  /**
   * XML 转义(, ")
   */
  escapeXML(data, space) {
    return this.escape(data, space).replace(/'/g, '&apos;')
  },

  /**
   * CSV 转义(, ")
   */
  escapeCSV(str) {
    str = this.stringify(str).replace(/"/g, '""')
    return str.match(/["\,\s+]/) ? ('"' + str + '"') : str
  },

  /**
   * URI 编码（包括 / ? : & = 等特殊符号）
   * @param {String} str
   */
  encodeURI(str) {
    return encodeURIComponent(str)
  },

  /**
   * URI 解码（包括 / ? : & = 等特殊符号）
   * @param {String} str
   */
  decodeURI(str) {
    return decodeURIComponent(str)
  },

  /**
   * URL 编码
   * @param {String} str
   */
  encodeURL(str) {
    return encodeURI(str)
  },

  /**
   * URL 解码
   * @param {String} str
   */
  decodeURL(str) {
    return decodeURI(str)
  },

  /**
   * 正则表达式转义（* . ? + $ ^ [ ] ( ) { } | \ /）
   */
  escapeREG(str) {
    return this.stringify(str).replace(
      /([\*\.\+\$\^\[\]\(\)\{\}\\\|\/])/g,
      '\\$1'
    )
  },

  /**
   * 用指定字符进行固定长度的前补全
   */
  leftPad(val, length, chr) {
    val = this.stringify(val)
    chr = this.stringify(chr)
    if (chr.length <= 0) {
      chr = ' '
    }
    while (val.length < length) {
      val = chr + val
    }
    return val
  },

  /**
   * 数组排序并去重
   * @param {Array} arr
   * @param {Function} checker
   * @param {Boolean} nocopy
   */
  arraySortAndUnique(arr, checker, nocopy) {
    if (!this.isArray(arr)) {
      return
    }
    if (this.isBoolean(checker)) {
      nocopy = checker
    }
    var uniqued = arr
    if (!nocopy) {
      uniqued = arr.concat([])
    }
    uniqued.sort()
    if (!this.isFunction(checker)) {
      checker = function(a, b) {
        return a === b
      }
    }
    for (var i = uniqued.length - 1; i >= 0; i--) {
      if (i > 0 && checker.call(this, uniqued[i], uniqued[i - 1])) {
        uniqued.splice(i, 1)
      }
    }
    return uniqued
  },

  /* 给数字中添加逗号 */
  formatNumber(num) {
    if (!/^(\+|\-)?(\d+)(\.\d+)?$/.test(num)) {
      return num
    }
    var a = RegExp.$1
    var b = RegExp.$2
    var c = RegExp.$3
    var re = /(\d)(\d{3})(,|$)/
    while (re.test(b)) {
      b = b.replace(re, '$1,$2$3')
    }
    return a + b + c
  },

  /**
   * 产生唯一串
   */
  genUuid() {
    var s = []
    var hexDigits = '0123456789abcdef'
    for (var i = 0; i < 36; i++) {
      s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1)
    }
    s[14] = '4' // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1) // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = '-'
    return s.join('')
  },

  /**
   * Set url's parameter.
   * If no url provided, location.href will be used.
   * @param {String} key
   * @param {String} value
   * @param {String} url
   */
  setUrlParam(param, value, url) {
    if ((url = this.trim(this.stringify(url))).length <= 0) {
      url = location.href
    }
    var flagidx = url.indexOf('?')
    var replstr = this.encodeURI(param) + '=' + this.encodeURI(value)
    if (flagidx < 0) {
      return url + '?' + replstr
    }
    if (flagidx === url.length - 1) {
      return url + replstr
    }
    var matched
    var pattern = new RegExp(
      '([?&])' +
        this.escapeREG(param) +
        '(?:=[^&]*)?(&|$)',
      'g')
    var replaced = false
    while ((matched = pattern.exec(url))) {
      replaced = true
      url = url.substr(0, pattern.lastIndex - matched[0].length) +
            matched[1] + replstr + matched[2] +
            url.substr(pattern.lastIndex)
      pattern.lastIndex -= matched[0].length - replstr.length - matched[2].length
    }
    if (replaced) {
      return url
    }
    if (!this.endsWith(url, '&')) {
      url += '&'
    }
    return url + replstr
  },

  /**
   * 设置URL参数
   * @param {String} url
   * @param {Object} params
   */
  setUrlParams(url, params) {
    url = this.trim(url)
    if (!this.isPlainObject(params)) {
      return url
    }
    if (url.length <= 0) {
      url = '?'
    }
    for (var key in params) {
      url = this.setUrlParam(key, params[key], url)
    }
    return url
  },

  /**
   * 打开新的窗口
   * @param {String} linkUrl
   */
  open(linkUrl) {
    var link = document.createElement('a')
    link.target = '_blank'
    document.body.appendChild(link)
    try {
      link.href = linkUrl
      link.click()
    } finally {
      document.body.removeChild(link)
    }
  },

  /**
   *  解决IE，Edge浏览器不兼容before函数
   * @param item
   */
  propBefore(item) {
    if (item.hasOwnProperty('before')) {
      return item
    }
    Object.defineProperty(item, 'before', {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function before() {
        var argArr = Array.prototype.slice.call(arguments)
        var docFrag = document.createDocumentFragment()

        argArr.forEach(function(argItem) {
          var isNode = argItem instanceof Node
          docFrag.appendChild(isNode ? argItem : document.createTextNode(String(argItem)))
        })

        this.parentNode.insertBefore(docFrag, this)
      }
    })
    return item
  },

  /**
   * 解决IE，Edge浏览器不兼容after函数
   * @param item
   */
  propAfter(item) {
    if (item.hasOwnProperty('after')) {
      return item
    }
    Object.defineProperty(item, 'after', {
      configurable: true,
      enumerable: true,
      writable: true,
      value: function after() {
        var argArr = Array.prototype.slice.call(arguments)
        var docFrag = document.createDocumentFragment()

        argArr.forEach(function(argItem) {
          var isNode = argItem instanceof Node
          docFrag.appendChild(isNode ? argItem : document.createTextNode(String(argItem)))
        })

        this.parentNode.insertBefore(docFrag, this.nextSibling)
      }
    })
    return item
  },

  /**
   * 时间的格式化
   * @param {String} utcDateTime 2020-09-20T17:57:00Z
   * @param {String} type date|time|datetime
   */
  formatUtcDateTime(utcDateTime, type) {
    if (tool.isBlank(utcDateTime = tool.trim(utcDateTime))) {
      return ''
    }
    if (utcDateTime.match(/^\d{4}\-\d{1,2}\-\d{1,2}$/)) {
      return utcDateTime
    }
    if (utcDateTime.match(/^\d{1,2}:\d{1,2}(:\d{1,2})?$/)) {
      return utcDateTime
    }
    if (utcDateTime.match(/^\d{4}\-\d{1,2}\-\d{1,2}\s+\d{1,2}:\d{1,2}/)) {
      return utcDateTime
    }
    if (utcDateTime.match(/^\d{1,2}:\d{1,2}:\d{1,2}(\.\d+)?Z$/)) {
      utcDateTime = '1970-01-01T' + utcDateTime
    }
    if (!utcDateTime.match(/^\d{4}\-\d{1,2}\-\d{1,2}T\d{1,2}:\d{1,2}:\d{1,2}(\.\d+)Z$/)) {
      throw new Error('UTC datetime format error: ' + utcDateTime)
    }
    // console.log('utcDateTime = ', utcDateTime)
    // 转为正常的时间格式 年-月-日 时:分:秒
    var tIndexOf = utcDateTime.indexOf('T')
    var zIndexOf = utcDateTime.indexOf('Z')
    var yearMonthDay = utcDateTime.substr(0, tIndexOf)
    var hourMinuteSecond = utcDateTime.substr(tIndexOf + 1, zIndexOf - tIndexOf - 1)
    var fmtDataTime = yearMonthDay + ' ' + hourMinuteSecond
    // 增加8个小时，北京时间比utc时间多八个时区
    var cstDateTime = new Date(Date.parse(fmtDataTime) + 8 * 60 * 60000)
    var cstYear = cstDateTime.getFullYear()
    var cstMonth = cstDateTime.getMonth() + 1
    var cstDate = cstDateTime.getDate()
    var cstHours = cstDateTime.getHours()
    var cstMinutes = cstDateTime.getMinutes()
    var cstSeconds = cstDateTime.getSeconds()
    cstMonth = cstMonth < 10 ? ('0' + cstMonth) : cstMonth
    cstDate = cstDate < 10 ? ('0' + cstDate) : cstDate
    cstHours = cstHours < 10 ? ('0' + cstHours) : cstHours
    cstMinutes = cstMinutes < 10 ? ('0' + cstMinutes) : cstMinutes
    cstSeconds = cstSeconds < 10 ? ('0' + cstSeconds) : cstSeconds
    if ((type = this.toLower(type)) === 'date') {
      return cstYear + '-' + cstMonth + '-' + cstDate
    } else if (type === 'time') {
      return cstHours + ':' + cstMinutes + ':' + cstSeconds
    }
    return cstYear + '-' + cstMonth + '-' + cstDate +
            ' ' + cstHours + ':' + cstMinutes + ':' + cstSeconds
  },

  /**
   * 检测流程字段的标签中，是否包含指定值
   * @param {String | Array} tags
    * @param {String} searchTag
   */
  formScriptTagsContains(tags, searchTag) {
    if (this.isBlank(searchTag)) {
      return false
    }
    if (!this.isArray(tags)) {
      tags = this.trim(tags).split(/[,;\s]+/)
    }
    return tags.length > 0 && this.inArray(searchTag, tags) >= 0
  }
}

export default tool
