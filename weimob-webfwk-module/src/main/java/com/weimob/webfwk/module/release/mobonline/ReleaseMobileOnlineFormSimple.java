package com.weimob.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.module.application.FieldApplicationName;
import com.weimob.webfwk.state.abs.*;
import com.weimob.webfwk.state.field.*;
import com.weimob.webfwk.state.util.StateFormBasicSaved;
import com.weimob.webfwk.util.state.field.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
@Attributes(title = "移动端应用发布申请")
public class ReleaseMobileOnlineFormSimple extends StateFormBasicSaved  implements AbstractStateFormBase, ReleaseMobileOnlineWithAppStore {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return ReleaseMobileOnlineService.getInstance().getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsAppStoreStatus extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                add(FieldSimpleOption.create("pending", "待发布"));
                add(FieldSimpleOption.create("pendingCheck", "待审核"));
                add(FieldSimpleOption.create("uploadedComplete", "已上架"));
                add(FieldSimpleOption.create("approveRejected", "审核拒绝"));
                add(FieldSimpleOption.create("cancel", "取消发布"));
            }
        };
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = ReleaseMobileOnlineFormDetail.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "应用", type = FieldApplicationName.class)
    private String applicationName;
    
    @Attributes(title = "版本")
    private String release;
    
    @Attributes(title = "发布说明", type = FieldText.class)
    private String releaseNote;
    
    @Attributes(title = "平台")
    private String storeType;
    
    @Attributes(title = "发布商店")
    private String appStore;
    
    @Attributes(title = "商店", type = FieldReleaseMobileOnlineAppStore.class)
    private FieldReleaseMobileOnlineAppStore.OptionStore[] store;
    
    @Attributes(title = "审批人", type = FieldSystemUser.class)
    private OptionSystemUser approver;
    
    @Attributes(title = "商店列表", type = FieldReleaseMobbileOnlineStoreSelection.class)
    private List<OptionReleaseMobileOnlineAppStore> itemStore;
    
    @Attributes(title = "发布结果")
    private String endResult;
    
    @Attributes(title = "额外审批人", type = FieldSystemUser.class)
    private OptionSystemUser specialApprover;
    
}
