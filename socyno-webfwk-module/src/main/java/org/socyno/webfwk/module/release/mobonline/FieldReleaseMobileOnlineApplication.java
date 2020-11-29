package org.socyno.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.v1.FieldOption;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.module.user.SystemUserSimple;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldReleaseMobileOnlineApplication extends FieldTableView {
    
    public static class OptionStore extends OptionReleaseMobileOnlineApplication implements FieldOption {
        @Override
        public void setOptionValue(String s) {
            setApplicationName(s);
        }
        
        @Override
        public String getOptionValue() {
            return "" + getApplicationName();
        }
        
        @Override
        public String getOptionDisplay() {
            return getApplicationName();
        }
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public static List<OptionStore> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        List<OptionStore> optionStoreList = new ArrayList<>();
        List<OptionReleaseMobileOnlineApplication> itemApplications;
        if (StringUtils.isBlank(filter.getKeyword())) {
            itemApplications = ReleaseMobileOnlineService.DEFAULT.getFormBaseDao().queryAsList(
                    OptionReleaseMobileOnlineApplication.class,
                    " SELECT * FROM `release_app_config` WHERE state_form_status = 'enabled' ");
        } else {
            itemApplications = ReleaseMobileOnlineService.DEFAULT.getFormBaseDao().queryAsList(
                    OptionReleaseMobileOnlineApplication.class,
                    " SELECT * FROM `release_app_config` WHERE state_form_status = 'enabled' AND application_name like CONCAT('%', ?, '%') ",
                    new Object[] { filter.getKeyword() });
        }
        for (OptionReleaseMobileOnlineApplication itemApplication : itemApplications) {
            OptionStore optionStore = new OptionStore();
            optionStore.setApplicationName(itemApplication.getApplicationName());
            SystemUserSimple systemUserSimple = SystemUserService.DEFAULT.getSimple(itemApplication.getApprover());
            if (systemUserSimple == null) {
                throw new MessageException("应用对应审批人无法获取详情");
            }
            optionStore.setApproverName(systemUserSimple.getDisplay());
            optionStoreList.add(optionStore);
        }
        return optionStoreList;
    }
}
