package org.socyno.webfwk.module.release.mobstore;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.util.state.field.FieldDateTime;

@Data
@Attributes(title = "移动端应用市场信息")
public class ReleaseMobileStoreFormSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return ReleaseMobileStoreService.getInstance().getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "单号", readonly = true)
    private Long id;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "创建人编号")
    private Long createdBy;
    
    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt;
    
    @Attributes(title = "创建人账户")
    private String createdCodeBy;
    
    @Attributes(title = "创建人姓名")
    private String createdNameBy;
    
    @Attributes(title = "商店名", required = true, position = 1001)
    private String storeName;
    
    @Attributes(title = "渠道包名", position = 1002)
    private String channelName;
    
}
