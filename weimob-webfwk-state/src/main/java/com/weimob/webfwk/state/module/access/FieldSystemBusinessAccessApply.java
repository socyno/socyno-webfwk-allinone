package com.weimob.webfwk.state.module.access;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.state.field.FieldSystemBusinessAll;
import com.weimob.webfwk.state.field.FilterBasicKeyword;
import com.weimob.webfwk.state.field.OptionSystemBusiness;
import com.weimob.webfwk.state.module.access.SystemAccessApplyFormSimple.AccessType;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
