package org.socyno.webfwk.state.module.log;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

@Getter
@Setter
@ToString
public class SystemLogFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {

    @Attributes(title = "状态", readonly = true)
    private String state;

    @Attributes(title = "操作类型")
    private String operateType;
    
    @Attributes(title = "操作对象")
    private String objectId;
    
    @Attributes(title = "操作事件")
    private String objectType;

    @Attributes(title = "操作说明")
    private String operateDesc;

    @Attributes(title = "日志详情")
    private Long operateDetailId;

    @Attributes(title = "操作代理人账户")
    private String operateProxyName;

    @Attributes(title = "操作代理人姓名")
    private String operateProxyDisplay;

}
