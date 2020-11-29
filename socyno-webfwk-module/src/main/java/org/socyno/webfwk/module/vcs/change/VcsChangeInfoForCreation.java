package org.socyno.webfwk.module.vcs.change;

import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsVcsType;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.util.state.field.FieldText;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VcsChangeInfoForCreation extends BasicStateForm {
    
    @Attributes(title = "源码仓库类型", required = true, type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "源码仓库地址", required = true)
    private String vcsPath;
    
    @Attributes(title = "仓库变更版本", required = true)
    private String vcsRevision;
    
    @Attributes(title = "仓库变更分支")
    private String vcsRefsName;
    
    @Attributes(title = "变更描述信息", type = FieldText.class)
    private String vcsMessage;
    
    @Attributes(title = "仓库变更前版本")
    private String vcsOldRevision;
    
    @Attributes(title = "是否覆盖更新")
    private String forceUpdate;
    
    @Attributes(title = "仓库变更用户")
    private String vcsCommiter;
}
