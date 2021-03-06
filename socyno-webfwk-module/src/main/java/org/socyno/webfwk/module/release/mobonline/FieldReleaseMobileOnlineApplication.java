package org.socyno.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.module.user.SystemUserFormSimple;
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
    
    @Override
    public List<OptionStore> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        List<OptionStore> optionStoreList = new ArrayList<>();
        List<OptionReleaseMobileOnlineApplication> itemApplications;
        if (StringUtils.isBlank(keyword.getKeyword())) {
            itemApplications = ReleaseMobileOnlineService.getInstance().getFormBaseDao().queryAsList(
                    OptionReleaseMobileOnlineApplication.class,
                    " SELECT * FROM `release_app_config` WHERE state_form_status = 'enabled' ");
        } else {
            itemApplications = ReleaseMobileOnlineService.getInstance().getFormBaseDao().queryAsList(
                    OptionReleaseMobileOnlineApplication.class,
                    " SELECT * FROM `release_app_config` WHERE state_form_status = 'enabled' AND application_name like CONCAT('%', ?, '%') ",
                    new Object[] { keyword.getKeyword() });
        }
        for (OptionReleaseMobileOnlineApplication itemApplication : itemApplications) {
            OptionStore optionStore = new OptionStore();
            optionStore.setApplicationName(itemApplication.getApplicationName());
            SystemUserFormSimple systemUserSimple = SystemUserService.getInstance()
                    .getSimple(itemApplication.getApprover());
            if (systemUserSimple == null) {
                throw new MessageException("应用对应审批人无法获取详情");
            }
            optionStore.setApproverName(systemUserSimple.getDisplay());
            optionStoreList.add(optionStore);
        }
        return optionStoreList;
    }
}
