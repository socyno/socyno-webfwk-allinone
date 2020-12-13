package org.socyno.webfwk.module.syslog;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

import org.socyno.webfwk.state.basic.AbstractStateForm;

@Getter
@Setter
@ToString
public class SystemLogFormSimple implements AbstractStateForm {

    @Attributes(title = "编号")
    private Long id;

    @Attributes(title = "版本")
    private Long revision;

    @Attributes(title = "状态")
    private String state;
    
    @Attributes(title = "表单类型")
    private String objectType;

    @Attributes(title = "表单ID")
    private String objectId;

    @Attributes(title = "操作用户编号")
    private String operateUserId;

    @Attributes(title = "操作用户账号")
    private String operateUserName;
    
    @Attributes(title = "操作用户姓名")
    private String operateUserDisplay;

    @Attributes(title = "操作时间")
    private Date operateTime;

    @Attributes(title = "操作事件")
    private String operateType;

    @Attributes(title = "描述")
    private String operateDesc;

    @Attributes(title = "日志详情ID")
    private Long operateDetailId;

    @Attributes(title = "操作代理人账户")
    private String operateProxyName;

    @Attributes(title = "操作代理人姓名")
    private String operateProxyDisplay;











}
