package com.weimob.webfwk.state.field;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.state.field.FieldTableView;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

public class FieldSystemBusinessAll extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
        SELECT DISTINCT
            b.*
        FROM
            system_business b
        WHERE
            b.id = ?
     */
    @Multiline
    private final static String SQL_GET_SINGLE_BUSINESS = "X";
    
    /**
     * 获取业务系统基本信息
     */
    public OptionSystemBusiness queryDynamicValue(String businessId) throws Exception {
        return SystemTenantDataSource.getMain().queryAsObject(OptionSystemBusiness.class,
                SQL_GET_SINGLE_BUSINESS, new Object[] { businessId });
    }
    
    /**
        SELECT DISTINCT
            b.*
        FROM
            system_business b
        WHERE
            b.id IN (%s)
     */
    @Multiline
    private final static String SQL_GET_NULTIPLE_BUSINESS = "X";
    
    /**
     * 获取业务系统基本信息
     */
    @Override
    public List<OptionSystemBusiness> queryDynamicValues(Object[] businessIds) throws Exception {
        if (businessIds == null || businessIds.length <= 0) {
            return Collections.emptyList();
        }
        return SystemTenantDataSource.getMain().queryAsList(OptionSystemBusiness.class,
                String.format(SQL_GET_SINGLE_BUSINESS, CommonUtil.join("?", businessIds.length, ",")),
                businessIds);
    }
    
    /**
        SELECT DISTINCT
            b.*
        FROM
            system_business b
        %s
     */
    @Multiline
    private final static String SQL_QUERY_ALL_BUSINESS = "X";
    
    /**
        (
                b.code LIKE CONCAT('%', ?, '%')
            OR
                b.name LIKE CONCAT('%', ?, '%')
            OR
                b.description LIKE CONCAT('%', ?, '%')
        )
     */
    @Multiline
    private final static String SQL_WHERE_BUSINESS_KEYWORD = "X";
    
    @Override
    public List<OptionSystemBusiness> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (keyword != null && StringUtils.isNotBlank(keyword.getKeyword())) {
            sqlargs.add(keyword.getKeyword());
            sqlargs.add(keyword.getKeyword());
            sqlargs.add(keyword.getKeyword());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_WHERE_BUSINESS_KEYWORD);
        }
        return SystemTenantDataSource.getMain().queryAsList(OptionSystemBusiness.class,
                String.format(SQL_QUERY_ALL_BUSINESS, StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString()),
                sqlargs.toArray());
    }
}
