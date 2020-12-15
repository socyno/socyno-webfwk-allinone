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
    
    private static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    /**
     * SELECT
     *     o.*
     * FROM
     *     system_field_option o
     * WHERE
     */
    @Multiline
    private final static String SQL_QUERY_CURRENT_OPTIONS = "x";

    /**
     * (
     *     option_group LIKE CONCAT('%', ?, '%')
     *   OR 
     *     option_value LIKE CONCAT('%', ?, '%')
     *   OR
     *     option_display LIKE CONCAT('%', ?, '%')
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
        StringBuilder sql = new StringBuilder(SQL_QUERY_CURRENT_OPTIONS)
            .append("class_path = ? AND category = ? AND disabled = 0");
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
                String.format("%s %s", SQL_QUERY_CURRENT_OPTIONS,
                        CommonUtil.join("(class_path = ? AND category = ? AND option_value = ?)",
                                args.size()/3, " OR ")),
                args.toArray());
    }
}
