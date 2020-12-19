package org.socyno.webfwk.module.vcs.change;

import java.util.List;

import org.socyno.webfwk.module.application.FieldApplication;
import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsVcsType;
import org.socyno.webfwk.module.application.FieldApplication.OptionApplication;
import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VcsChangeInfoFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return VcsChangeInfoService.getInstance().getStates();
        }
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "源码仓库类型", type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "应用", type = FieldApplication.class)
    private OptionApplication application;
    
    @Attributes(title = "源码仓库地址")
    private String vcsPath;
    
    @Attributes(title = "仓库变更分支")
    private String vcsRefsName;
    
    @Attributes(title = "仓库变更版本")
    private String vcsRevision;
    
    @Attributes(title = "仓库变更前版本")
    private String vcsOldRevision;
    
    @Attributes(title = "变更描述标题")
    private String vcsSummary;
    
    @Attributes(title = "变更描述详情", type = FieldText.class)
    private String vcsMessage;
}
