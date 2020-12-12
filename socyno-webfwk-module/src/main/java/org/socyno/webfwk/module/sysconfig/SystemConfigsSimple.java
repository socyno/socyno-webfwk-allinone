package org.socyno.webfwk.module.sysconfig;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldDateTime;
import org.socyno.webfwk.util.state.field.FieldText;

@Data
@Attributes(title = "系统参数配置")
public class SystemConfigsSimple implements AbstractStateForm {

    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemConfigsService.DEFAULT.getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    @Attributes(title = "编号", readonly = true)
    private Long  id;

    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String  state;

    @Attributes(title = "版本", readonly = true)
    private Long  revision;

    @Attributes(title = "创建人编号")
    private Long createdBy ;

    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt ;

    @Attributes(title = "创建人账户")
    private String createdCodeBy;

    @Attributes(title = "创建人姓名")
    private String createdNameBy;

    @Attributes(title = "键", required = true, position = 1001)
    private String name ;

    @Attributes(title = "值", required = true, position = 1002 ,type = FieldText.class)
    private String value ;

    @Attributes(title = "备注", required = true, position = 1003 ,type = FieldText.class)
    private String comment ;

}
