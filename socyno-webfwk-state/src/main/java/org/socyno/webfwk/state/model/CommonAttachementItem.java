package org.socyno.webfwk.state.model;

import java.util.Date;

import org.socyno.webfwk.util.state.field.FieldDateTime;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;

@Data
public class CommonAttachementItem {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "分类", readonly = true)
    private String type;
    
    @Attributes(title = "字段", readonly = true)
    private String field;
    
    @Attributes(title = "表单号", readonly = true)
    private Long formId;
    
    @Attributes(title = "名称", position = 10, readonly = true)
    private String name;
    
    @Attributes(title = "大小", position = 20, readonly = true)
    private Long size;
    
    @Attributes(title = "添加者", readonly = true)
    private Long createdId;
    
    @Attributes(title = "添加者", readonly = true)
    private String createdBy;
    
    @Attributes(title = "添加者", position = 30, readonly = true)
    private String createdName;
    
    @Attributes(title = "添加时间", position = 40, readonly = true, type= FieldDateTime.class)
    private Date createdAt;
}
