package org.socyno.webfwk.state.field;


import java.util.*;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;

public abstract class AbstractFieldDynamicStandard extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    public static String getFormTableName() {
        return "system_form_option";
    }
    
    public static String getValuesTableName() {
        return "system_form_option_values";
    }
    
    /**
     * SELECT
     *     o.*,
     *     o.value AS option_value,
     *     o.group AS option_group,
     *     o.display AS option_display
     * FROM
     *     %s o,
     *     %s v
     * WHERE
     *     o.id = v.class_path
     */
    @Multiline
    private final static String SQL_QUERY_CURRENT_OPTIONS = "x";

    /**
     * (
     *     v.group LIKE CONCAT('%', ?, '%')
     *   OR 
     *     v.value LIKE CONCAT('%', ?, '%')
     *   OR
     *     v.display LIKE CONCAT('%', ?, '%')
     * )
     *
     */
    @Multiline
    private final static String SQL_QUERY_OPTIONS_LIKE_TMPL = "x";

    public boolean categoryRequired(){
        return false ;
    }

    public String getCategoryFieldValue(Object form) {
        return "";
    }

    public String getCategoryFieldValue(FilterBasicKeyword filter) {
        return "";
    }
    
    @Override
    public List<OptionDynamicStandard> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        String category = "";
        if (categoryRequired()) {
            if (StringUtils.isBlank(category = getCategoryFieldValue(filter))) {
                return Collections.emptyList();
            }
        }
        StringBuilder sql = new StringBuilder()
                .append(String.format(SQL_QUERY_CURRENT_OPTIONS, getFormTableName(), getValuesTableName()))
                .append("o.class_path = ? AND v.category = ? AND v.disabled = 0");
        List<Object> args = new ArrayList<>();
        args.add(getClass().getName());
        args.add(category);
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        if (filter != null && StringUtils.isNotBlank(keyword.getKeyword())) {
            args.add(keyword.getKeyword());
            args.add(keyword.getKeyword());
            args.add(keyword.getKeyword());
            sql.append(" AND ").append(SQL_QUERY_OPTIONS_LIKE_TMPL);
        }
        return getDao().queryAsList(OptionDynamicStandard.class, sql.toString(), args.toArray());
    }
    
    public static List<OptionDynamicStandard> queryDynamicValues(Collection<OptionDynamicStandard> options) throws Exception {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return queryDynamicValues(options.toArray(new OptionDynamicStandard[0]));
    }
    
    public static List<OptionDynamicStandard> queryDynamicValues(OptionDynamicStandard ...options) throws Exception {
        if (options == null || options.length <= 0) {
            return Collections.emptyList();
        }
        List<Object> args = new LinkedList<>();
        for (OptionDynamicStandard v : options) {
            if (v == null || StringUtils.isBlank(v.getClassPath())) {
                continue;
            }
            args.add(v.getClassPath());
            args.add(v.getCategory());
            args.add(v.getOptionValue());
        }
        return getDao().queryAsList(OptionDynamicStandard.class,
                String.format(SQL_QUERY_CURRENT_OPTIONS.concat(" %s"),
                        getFormTableName(),
                        getValuesTableName(),
                        CommonUtil.join("(o.class_path = ? AND v.category = ? AND v.option_value = ?)",
                                args.size()/3, " OR ")),
                args.toArray());
    }
}
