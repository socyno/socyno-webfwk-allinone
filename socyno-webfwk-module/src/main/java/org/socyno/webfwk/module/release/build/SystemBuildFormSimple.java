package org.socyno.webfwk.module.release.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

@Getter
@Setter
@ToString
@Attributes(title = "构建服务清单")
public class SystemBuildFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldSysBuildOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemBuildService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    @Attributes(title = "名称")
    private String code;
    
    @Attributes(title = "类型", type = FieldOptionsApplicationType.class)
    private String type;

    @Attributes(title = "标题")
    private String title;

    @Attributes(title = "描述")
    private String description;

    @Attributes(title = "状态", readonly = true, type = FieldSysBuildOptionsState.class)
    private String state;
}
