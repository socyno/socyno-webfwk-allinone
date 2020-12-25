package com.weimob.webfwk.state.module.user;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.state.field.OptionSystemUser;
import com.weimob.webfwk.state.util.StateFormBasicSaved;
import com.weimob.webfwk.util.model.AbstractUser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统用户基本信息")
public class SystemUserFormSimple extends StateFormBasicSaved implements AbstractUser, AbstractStateFormBase {
    
    @Attributes(title = "状态", type = SystemUserFormDetail.FieldOptionsState.class)
    private String  state;
    
    @Attributes(title = "用户")
    private String  username;
    
    @Attributes(title = "姓名")
    private String  display;
    
    @Attributes(title = "职务")
    private String  title;
    
    @Attributes(title = "部门")
    private String  department;
    
    @Attributes(title = "邮箱")
    private String  mailAddress;
    
    @Attributes(title = "直属领导", type = FieldSystemUser.class)
    private OptionSystemUser manager;
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemUserService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
}
