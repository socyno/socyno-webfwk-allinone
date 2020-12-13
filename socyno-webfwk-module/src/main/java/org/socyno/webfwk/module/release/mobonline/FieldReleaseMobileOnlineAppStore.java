package org.socyno.webfwk.module.release.mobonline;

import com.github.reinert.jjschema.v1.FieldOption;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldReleaseMobileOnlineAppStore extends FieldTableView {
    
    public static class OptionStore extends ReleaseOptionStoreEntity implements FieldOption {
        @Override
        public void setOptionValue(String s) {
            setStoreName(s);
        }
        
        @Override
        public String getOptionValue() {
            return "" + getStoreName();
        }
        
        @Override
        public String getOptionDisplay() {
            return getStoreName();
        }
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public static List<OptionStore> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        JsonElement obj = CommonUtil.fromJson(filter.getFormJson(), JsonElement.class);
        String applicationName = CommonUtil.getJstring((JsonObject) obj, "applicationName");
        if (StringUtils.isNotBlank(applicationName)) {
            OptionReleaseMobileOnlineApplication app = ReleaseMobileOnlineService.getInstance()
                    .queryConfigurationInformation(applicationName);
            if (app != null && "ios".equals(app.getStoreType())) {
                List<OptionStore> list = new ArrayList<>();
                OptionStore option = new OptionStore();
                option.setStoreName("ios");
                list.add(option);
                return list;
            }
        } else if (!filter.getFormId().equals(-1L)) {
            ReleaseMobileOnlineFormSimple simple = ReleaseMobileOnlineService.getInstance().getForm(filter.getFormId());
            if ("ios".equals(simple.getStoreType())) {
                List<OptionStore> list = new ArrayList<>();
                OptionStore option = new OptionStore();
                option.setStoreName("ios");
                list.add(option);
                return list;
            }
        }
        
        List<OptionStore> optionStoreList = new ArrayList<>();
        List<ReleaseOptionStoreEntity> itemStore;
        if (StringUtils.isBlank(filter.getKeyword())) {
            itemStore = ReleaseMobileOnlineService.getInstance().getFormBaseDao().queryAsList(
                    ReleaseOptionStoreEntity.class,
                    " SELECT * FROM `release_app_store` WHERE state_form_status = 'enabled' ");
        } else {
            itemStore = ReleaseMobileOnlineService.getInstance().getFormBaseDao().queryAsList(
                    ReleaseOptionStoreEntity.class,
                    " SELECT * FROM `release_app_store` WHERE state_form_status = 'enabled' AND store_name like CONCAT('%', ?, '%') ",
                    new Object[] { filter.getKeyword() });
        }
        for (ReleaseOptionStoreEntity store : itemStore) {
            OptionStore optionStore = new OptionStore();
            optionStore.setStoreName(store.getStoreName());
            optionStoreList.add(optionStore);
        }
        return optionStoreList;
    }
}
