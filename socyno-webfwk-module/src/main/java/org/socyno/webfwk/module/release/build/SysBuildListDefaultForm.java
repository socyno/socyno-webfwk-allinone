package org.socyno.webfwk.module.release.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsApplicationType;
import org.socyno.webfwk.module.release.build.SysBuildFormDetail.FieldSysBuildOptionsState;
import org.socyno.webfwk.state.basic.AbstractStateForm;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
@Attributes(title = "构建服务清单")
public class SysBuildListDefaultForm implements AbstractStateForm {

    @Attributes(title = "编号")
    private Long id;

    @Attributes(title = "名称")
    private String code;
    
    @Attributes(title = "类型", type = FieldOptionsApplicationType.class)
    private String type;

    @Attributes(title = "标题")
    private String title;

    @Attributes(title = "描述")
    private String description;

    @Attributes(title = "状态", type = FieldSysBuildOptionsState.class)
    private String state;

    @Attributes(title = "版本")
    private Long revision;
}
