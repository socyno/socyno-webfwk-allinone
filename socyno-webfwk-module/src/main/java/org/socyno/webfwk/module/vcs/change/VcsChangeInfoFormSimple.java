package org.socyno.webfwk.module.vcs.change;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.module.app.form.FieldApplication;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsVcsType;
import org.socyno.webfwk.module.app.form.FieldApplication.OptionApplication;
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
public class VcsChangeInfoFormSimple implements VcsChangeInfoWithApplication {
    
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
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "应用")
    private Long applicationId;
    
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
    
    @Attributes(title = "创建时间")
    private Date createdAt;
    
    @Attributes(title = "创建人编号")
    private Long createdBy;
    
    @Attributes(title = "创建人账户")
    private String createdCodeBy;
    
    @Attributes(title = "创建人姓名")
    private String createdNameBy;
}
