package org.socyno.webfwk.module.release.mobstore;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.abs.AbstractStateFormBase;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用市场信息")
public class ReleaseMobileStoreFormSimple extends StateFormBasicSaved  implements AbstractStateFormBase {
    
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
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "商店名", required = true, position = 1001)
    private String storeName;
    
    @Attributes(title = "渠道包名", position = 1002)
    private String channelName;
    
}
