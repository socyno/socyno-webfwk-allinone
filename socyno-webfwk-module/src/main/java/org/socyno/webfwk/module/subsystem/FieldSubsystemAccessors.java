package org.socyno.webfwk.module.subsystem;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldSubsystemAccessors extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 获取业务系统基本信息
     */
    public SubsystemBasicForm queryDynamicValue(Long subsystemId) throws Exception {
        return SubsystemService.DEFAULT.get(SubsystemBasicForm.class, subsystemId);
    }
    
    /**
     * 获取业务系统基本信息
     */
    @Override
    public List<SubsystemBasicForm> queryDynamicValues(Object[] subsystemIds) throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0) {
            return Collections.emptyList();
        }
        return SubsystemService.DEFAULT.list(SubsystemBasicForm.class,
                new SubsystemListAllQuery(50, 1L).setIdsIn(StringUtils.join(subsystemIds, ','))).getList();
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索业务系统
     */
    public List<SubsystemBasicForm> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        if(filter.getFormName().equals("access_request")){
            JsonElement obj = CommonUtil.fromJson(filter.getFormJson(),JsonElement.class);
            String accessType = CommonUtil.getJstring((JsonObject) obj,"accessType");
            if(StringUtils.isBlank(accessType)){
                return null;
            }
            if(accessType.equals("overallSituation")){
                SubsystemBasicForm subsystemBasicForm = new SubsystemBasicForm();
                subsystemBasicForm.setName("全局");
                subsystemBasicForm.setOptionValue("0");
                subsystemBasicForm.setCode("全局");
                List<SubsystemBasicForm> subsystemBasicForms = new ArrayList<>();
                subsystemBasicForms.add(subsystemBasicForm);
                return subsystemBasicForms;
            }else{
                return SubsystemService.DEFAULT
                        .list(SubsystemBasicForm.class, new SubsystemListAllQuery(50, 1L).setKeyword(filter.getKeyword()))
                        .getList();
            }
        }

        return SubsystemService.DEFAULT
                .list(SubsystemBasicForm.class, new SubsystemListAccessorQuery(filter.getKeyword(), 50, 1L)).getList();
    }
}
