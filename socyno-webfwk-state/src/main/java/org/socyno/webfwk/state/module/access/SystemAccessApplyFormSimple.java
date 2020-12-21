package org.socyno.webfwk.state.module.access;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;
import org.socyno.webfwk.util.state.field.FieldText;

@Getter
@Setter
@ToString
@Attributes(title = "系统权限申请表单")
public class SystemAccessApplyFormSimple extends StateFormBasicSaved implements AbstractStateFormBase, SystemAccessApplyWithDetails {
    
    @Getter
    public enum AccessType {
        SYSTEM   ("system",   "全局"), 
        BUSINESS ("business", "业务");
        
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
    
    @Attributes(title = "状态", readonly = true, type = SystemAccessApplyFormDetail.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "申请标题")
    private String title;
    
    @Attributes(title = "申请说明", type = FieldText.class)
    private String description;
    
    @Attributes(title = "权限及角色", type = FieldSystemAccessApplyBusinessEntity.class)
    private List<SystemAccessApplyBusinessEntity> businessEntities;
    
}
