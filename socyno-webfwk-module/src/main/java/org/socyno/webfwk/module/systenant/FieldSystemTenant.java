package org.socyno.webfwk.module.systenant;

import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;

import lombok.Data;

public class FieldSystemTenant extends FieldTableView {

    @Data
    @Attributes(title = "租户选项")
    public static class OptionSystemTenant implements FieldOption {
        
        @Attributes(title = "租户编号")
        private Long id;
        
        @Attributes(title = "租户代码")
        private String code;
        
        @Attributes(title = "租户名称")
        private String name;
        
        @Override
        public String getOptionDisplay() {
            return String.format("%s : %s", getCode(), getName());
        }
        
        @Override
        public String getOptionValue() {
            return "" + getId();
        }
        
        @Override
        public void setOptionValue(String value) {
            setId(Long.valueOf(value));
        }
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     *  SELECT
     *     t.id,
     *     t.code,
     *     t.name
     *  FROM
     *      system_tenant t
     * 
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_OPTIONS = "X";
    
    /**
     *  ORDER BY
     *      t.code ASC
     * 
     */
    @Multiline
    private static final String SQL_ORDER_TENANT_OPTIONS = "X";
    
    /**
     * 覆盖父类的方法，根据关键字检索系统租户
     */
    @Override
    public List<OptionSystemTenant> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        if (filter == null || StringUtils.isBlank(keyword.getKeyword())) {
            return SystemTenantService.getInstance().queryFormWithStateRevision(OptionSystemTenant.class,
                    String.format("%s WHERE t.%s = 'enabled' %s LIMIT 50", SQL_QUERY_TENANT_OPTIONS,
                            SystemTenantService.getInstance().getFormStateField(), SQL_ORDER_TENANT_OPTIONS));
        }
        return SystemTenantService.getInstance().queryFormWithStateRevision(OptionSystemTenant.class, String.format(
                "%s WHERE t.%s = 'enabled' AND (t.code LIKE CONCAT('%%', ?, '%%') OR t.name LIKE CONCAT('%%', ?, '%%')) %s LIMIT 50",
                SQL_QUERY_TENANT_OPTIONS, SystemTenantService.getInstance().getFormStateField(), SQL_ORDER_TENANT_OPTIONS),
                new Object[] { keyword.getKeyword(), keyword.getKeyword() });
    }
    
    /**
     * 覆盖父类的方法，根据选项值检索系统租户
     */
    @Override
    public List<OptionSystemTenant> queryDynamicValues(Object[] values) throws Exception {
        Long[] ids;
        if (values == null || values.length <= 0
                || (ids = ConvertUtil.asNonNullUniqueLongArray((Object[]) values)).length <= 0) {
            return Collections.emptyList();
        }
        return SystemTenantService.getInstance().queryFormWithStateRevision(OptionSystemTenant.class,
                String.format("%s WHERE t.id IN (%s) %s", SQL_QUERY_TENANT_OPTIONS,
                        CommonUtil.join("?", ids.length, ","), SQL_ORDER_TENANT_OPTIONS),
                ids);
    }
}
