package org.socyno.webfwk.module.sysaccess;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldDateTime;
import org.socyno.webfwk.util.state.field.FieldText;

@Data
@Attributes(title = "权限申请")
public class SystemAccessApplyFormSimple implements AbstractStateForm, SystemAccessApplyWithSubSystems {
    
    @Getter
    public enum AccessType {
        SYSTEM("system", "系统全局"),
        SUBSYS("subsystem", "业务系统");
        private final String value;
        
        private final String display;
        
        AccessType(String value, String display) {
            this.value = value;
            this.display = display;
        }
    }
    
    public static class FieldOptionsState extends FieldType {
        
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemAccessApplyService.getInstance().getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    public static class FieldOptionsAccessType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for (AccessType type : AccessType.values()) {
                    add(FieldSimpleOption.create(type.getValue(), type.getDisplay()));
                }
            }
        };
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }

    @Attributes(title = "编号", readonly = true)
    private Long  id;

    @Attributes(title = "状态", readonly = true, type = SystemAccessApplyFormDetail.FieldOptionsState.class)
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

    @Attributes(title = "申请原因",type = FieldText.class)
    private String reasonForApplication ;

    @Attributes(title = "权限及角色" , type = FieldSystemAccessApplySubSystemEntity.class)
    private List<SystemAccessApplySubSystemEntity> subSystems ;

    @Attributes(title = "角色信息")
    private String title;

}
