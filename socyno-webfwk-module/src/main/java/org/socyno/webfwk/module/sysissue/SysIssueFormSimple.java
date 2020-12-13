package org.socyno.webfwk.module.sysissue;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.util.state.field.*;

@Getter
@Setter
@ToString
public class SysIssueFormSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SysIssueService.getInstance().getStates();
        }
    }
    
    public static class FieldOptionsCategory extends AbstractFieldDynamicStandard {
        
    }
    
    public static class FieldOptionsCloseResult extends AbstractFieldDynamicStandard {
        
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "分类", type = FieldOptionsCategory.class,
            visibleTags= {"event_create", "event_edit"},
            editableTags= {"event_create", "event_edit"},
            requiredTags= {"event_create", "event_edit"})
    private OptionDynamicStandard category;
    
    @Attributes(title = "标题", type = FieldTextLine.class,
                visibleTags= {"event_assign", "event_create", "event_close", "event_edit"},
                editableTags= {"event_assign", "event_create", "event_edit"},
                requiredTags= {"event_assign", "event_create", "event_edit"})
    private String title;
    
    @Attributes(title = "描述", type = FieldText.class,
            visibleTags= {"event_assign", "event_create", "event_close", "event_edit"},
            editableTags= {"event_assign", "event_create", "event_edit"},
            requiredTags= {"event_assign", "event_create", "event_edit"})
    private String description;
    
    @Attributes(title = "处理人", type = FieldSystemUser.class,
                    visibleTags= {"event_assign"},
                    requiredTags= {"event_assign"},
                    editableTags= {"event_assign"})
    private OptionSystemUser assignee;
    
    @Attributes(title = "计划完成日期", type = FieldDateOnly.class,
            visibleTags= {"event_assign"},
            requiredTags= {"event_assign"},
            editableTags= {"event_assign"})
    private Date assignDate;
    
    @Attributes(title = "处理结果", type = FieldOptionsCloseResult.class,
                    visibleTags= {"event_close"},
                    requiredTags= {"event_close"},
                    editableTags= {"event_close"})
    private OptionDynamicStandard result;
    
    @Attributes(title = "解决方案", type = FieldText.class,
                    visibleTags= {"event_close"},
                    requiredTags= {"event_close"},
                    editableTags= {"event_close"})
    private String resolution;
    
    @Attributes(title = "创建人", type = FieldSystemUser.class)
    private OptionSystemUser createdBy;
    
    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt;
}
