package org.socyno.webfwk.module.sysaccess;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.module.subsystem.FieldSubsystemNoAnyLimited;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.module.sysaccess.SystemAccessApplyFormSimple.AccessType;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldSubsystemAccessApply extends FieldSubsystemNoAnyLimited {
    @Override
    public List<SubsystemFormSimple> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        JsonElement formData = CommonUtil.fromJson(keyword.getFormJson(), JsonElement.class);
        String accessType = CommonUtil.getJstring((JsonObject) formData, "accessType");
        if (StringUtils.isBlank(accessType)) {
            return Collections.emptyList();
        }
        if (AccessType.SYSTEM.getValue().equals(accessType)) {
            SubsystemFormSimple option = new SubsystemFormSimple();
            option.setOptionValue("0");
            option.setCode(AccessType.SYSTEM.getValue());
            option.setName(AccessType.SYSTEM.getDisplay());
            List<SubsystemFormSimple> options = new ArrayList<>(1);
            options.add(option);
            return options;
        }
        return super.queryDynamicOptions(filter);
    }
}
