package org.socyno.webfwk.state.module.access;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.field.FieldSystemBusinessAll;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.field.OptionSystemBusiness;
import org.socyno.webfwk.state.module.access.SystemAccessApplyFormSimple.AccessType;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldSystemBusinessAccessApply extends FieldSystemBusinessAll {
    @Override
    public List<OptionSystemBusiness> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        JsonElement formData = CommonUtil.fromJson(keyword.getFormJson(), JsonElement.class);
        String accessType = CommonUtil.getJstring((JsonObject) formData, "accessType");
        if (StringUtils.isBlank(accessType)) {
            return Collections.emptyList();
        }
        if (AccessType.SYSTEM.getValue().equals(accessType)) {
            List<OptionSystemBusiness> options = new ArrayList<>(1);
            options.add(genSystemBusinessOption());
            return options;
        }
        return super.queryDynamicOptions(filter);
    }
    
    public static OptionSystemBusiness genSystemBusinessOption() {
        OptionSystemBusiness option = new OptionSystemBusiness();
        option.setOptionValue(AccessType.SYSTEM.getValue());
        option.setName(AccessType.SYSTEM.getDisplay());
        return option;
    }
}
