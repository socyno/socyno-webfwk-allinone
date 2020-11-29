module.exports = {
  deploySetting: {
    auto: '自动部署',
    manual: '手动部署',
    soc: 'SOC部署',
    dba: 'DBA部署'
  },
  deployItemStatus: {
    not_deployed: '等待部署',
    deploy_processing: '部署中',
    deploy_crashed: '部署启动失败',
    deploy_failed: '部署失败',
    deploy_success: '部署完成',
    rollback_processing: '回滚中',
    rollback_crashed: '回滚启动失败',
    rollback_failed: '回滚失败',
    rollback_success: '回滚成功',
    deploy_a_success: 'A机组部署成功',
    deploy_a_failed: 'A机组部署失败',
    rollback_b_success: 'B机组回滚成功',
    rollback_b_failed: 'B机组回滚失败'
  },
  planDeployStatus: {
    not_audited: '等待审批',
    audit_failed: '审批拒绝',
    deploy_ready: '等待部署',
    deploy_suspend: '暂停部署',
    deploy_success: '部署完成',
    deploy_failed: '部署失败',
    deploy_processing: '部署中',
    rollback_processing: '回滚中',
    verify_failed: '验证失败',
    online_rollbacking: '发布回滚中',
    online_success: '发布成功',
    online_failure: '发布失败',
    deploy_stopped: '部署终止',
    deleted: '已删除',
    cr_ck_mq_deploy_processing: 'CR/CK/MQ部署中',
    cr_ck_mq_deploy_success: 'CR/CK/MQ部署完成',
    unit_A_gray_into: 'A机组进入灰度',
    unit_A_gray_done: 'A机组已进入灰度',
    unit_A_deploy_ready: 'A机组等待部署',
    unit_A_deploy_processing: 'A机组部署中',
    unit_A_deploy_success: 'A机组部署完成',
    unit_A_verify_ready: 'A机组待验证',
    unit_A_verify_success: 'A机组发布完成',
    unit_A_online_rollbacking: 'A机组发布回滚中',
    unit_A_verify_failed: 'A机组验证失败',
    unit_A_deploy_failed: 'A机组部署失败',
    unit_A_rollback_processing: 'A机组部署回滚中',
    unit_A_deploy_suspend: 'A机组部署暂停',
    unit_A_changeweight_ready: 'A机组待切量',
    unit_A_changeweight: 'A机组切量中',
    unit_A_changeweight_success: 'A机组切量完成',
    unit_A_changeweight_error: 'A机组切量异常',
    unit_B_gray_into: 'B机组进入灰度',
    unit_B_gray_done: 'B机组已进入灰度',
    unit_B_deploy_ready: 'B机组待部署',
    unit_B_deploy_processing: 'B机组部署中',
    unit_B_deploy_success: 'B机组部署完成',
    unit_B_deploy_failed: 'B机组部署失败',
    unit_B_deploy_suspend: 'B机组部署暂停',
    unit_B_rollback_processing: 'B机组部署回滚中',
    unit_B_verify_ready: 'B机组待验证',
    unit_B_verify_success: 'B机组发布完成',
    unit_B_online_rollbacking: 'B机组发布回滚中',
    unit_B_verify_failed: 'B机组验证失败',
    unit_B_changeweight: 'B机组切量中',
    unit_B_changeweight_ready: 'B机组待切量',
    unit_B_changeweight_success: 'B机组切量完成',
    unit_B_changeweight_error: 'B机组切量异常'
  },
  reportDimension: {
    week: '周',
    month: '月',
    quarter: '季度',
    year: '年'
  },
  buildStateList: {
    '1': '待构建',
    '2': '正在构建',
    '3': '构建成功',
    '4': '构建失败',
    '5': '部署成功',
    '6': '已经上线',
    '7': '部署失败',
    '8': '取消部署',
    '9': '部署成功',
    '-1': '已经删除'
  },
  objectTypeList: {
    '0': 'CR',
    '1': 'CK',
    '2': 'MQ',
    '3': 'App',
    '4': '三方应用'
  },
  operateTypeList: {
    '0': '废弃',
    '1': '提交',
    '2': '部署',
    '3': '上传',
    '4': '构建',
    '5': '新增',
    '6': '审核',
    '9': '审核不通过',
    '10': '取消部署',
    '11': '应用废除',
    '12': '修改'
  },
  scopeList: {
    '1': '测试和生产环境',
    '2': '测试环境',
    '3': '生产环境'
  },
  qaConfirmStateList: {
    '0': '未确认',
    '1': '通过',
    '2': '不通过',
    '3': '待验证'
  },
  qaST2ConfirmStateList: {
    '0': '未确认',
    '1': '通过',
    '2': '不通过'
  },
  containerList: {

  },
  envList: {
    'stage1': 'stage1',
    'stage2': 'stage2',
    'stage3': 'stage3',
    'sandbox': 'sandbox',
    'dev-sh': 'dev-sh',
    'dev-nj': 'dev-nj'
  },
  deployStateList: {
    '0': '未部署',
    '1': '部署成功',
    '2': '部署失败',
    '3': '已经回滚'
  },
  applySvnStatusList: {
    '0': '待审批',
    '1': '审批通过',
    '2': '审批拒绝'
  },
  releaseBookingStatusList: {
    '1': '待审批',
    '2': '通过',
    '3': '拒绝'
  },
  packageTypeReleaseMapping: {
    aar: 'no',
    apk: 'auto',
    apkjar: 'auto',
    ear: 'yes',
    framework: 'auto',
    ipa: 'auto',
    jar: 'no',
    js: 'yes',
    php: 'yes',
    python: 'yes',
    rar: 'no',
    war: 'yes',
    stormjar: 'yes',
    sparkjar: 'yes',
    appjar: 'yes',
    'static': 'yes'
  }, DeployTypeStatusList: {
    '1': 'jmx探测失败',
    '2': '第一个节点部署失败，第二个节点未部署',
    '3': '生产版本和出包名中包含的版本不一致',
    '4': '任务执行失败',
    '5': '达到最大重试次数任务执行仍失败',
    '256': '输入参数错误',
    '512': '非Tomcat应用',
    '768': '同步包错误'
  }, RollbackTypeStatusList: {
    '3': 'JMX探测失败',
    '4': '任务执行失败',
    '5': '达到最大重试次数任务执行仍失败',
    '256': '输入参数错误',
    '512': '远程备份文件或实例路径错误'
  }, configTypeList: {
    base_image: '基础镜像',
    file: '文件',
    dir: '目录',
    mount: '挂载',
    port: '暴露端口'
  }
}
