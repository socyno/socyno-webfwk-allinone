package org.socyno.webfwk.module.vcs.change;

import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsVcsType;
import org.socyno.webfwk.state.basic.BasicStateForm;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class VcsChangeInfoFormCreation extends BasicStateForm {
    
    @Attributes(title = "源码仓库类型", required = true, type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "源码仓库地址", required = true)
    private String vcsPath;
    
    @Attributes(title = "仓库变更版本", required = true)
    private String vcsRevision;
    
    @Attributes(title = "仓库变更分支")
    private String vcsRefsName;
    
    @Attributes(title = "仓库变更前版本")
    private String vcsOldRevision;
    
    @Attributes(title = "仓库变更用户")
    private String vcsCommiter;
}
