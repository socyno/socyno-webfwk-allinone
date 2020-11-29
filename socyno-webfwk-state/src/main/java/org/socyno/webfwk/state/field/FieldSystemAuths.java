package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldSystemAuths extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    private static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    /**
    SELECT
        x.*
    FROM (
        SELECT
            'interface' AS type,
            i.scope_type AS scopeType,
            i.auth AS auth 
        FROM
            system_interfaze i 
        WHERE
            i.deleted_at IS NULL UNION
        SELECT
            'form_event' AS type,
            a.authority_type AS scopeType,
            a.action_key AS auth 
        FROM
            system_form_actions a,
            system_form_defined f 
        WHERE
            f.form_name = a.form_name
    ) x
    %s
    ORDER BY
        x.type,
        x.scopeType,
        x.auth     
     */
    @Multiline
    private final static String SQL_QUERY_AUTH_OPTIONS = "X";
    
    public List<OptionSystemAuth> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        String placeHolder = "";
        List<String> sqlArgs = new ArrayList<>();
        if (StringUtils.isNotBlank(filter.getKeyword())) {
            sqlArgs.add(filter.getKeyword());
            placeHolder = " WHERE auth LIKE CONCAT('%', ?, '%') ";
        }
        return getDao().queryAsList(OptionSystemAuth.class, String.format(SQL_QUERY_AUTH_OPTIONS, placeHolder),
                sqlArgs.toArray());
    }
    
    @Override
    public List<OptionSystemAuth> queryDynamicValues(Object[] values) throws Exception {
        if (values == null || values.length <= 0) {
            return Collections.emptyList();
        }
        
        String placeHolder = String.format(" WHERE auth IN (%s) ", CommonUtil.join("?", values.length, ","));
        return getDao().queryAsList(OptionSystemAuth.class, String.format(SQL_QUERY_AUTH_OPTIONS, placeHolder), values);
    }
}
