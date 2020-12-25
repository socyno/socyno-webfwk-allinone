package com.weimob.webfwk.state.util;

import org.apache.commons.lang3.StringUtils;

import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.exec.StateFormNamedQueryNameInvalidException;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class StateFormNamedQuery<R extends AbstractStateFormBase> {
    
    /**
     * 查询名称, 在同一个表单中必须确保其唯一性
     **/
    public final String name;
    
    /**
     * 查询结果实体类, 查询结果将直接转换为该实体的实例列表
     */
    public final Class<R> resultClass;
    
    /**
     * 查询语句构造类，通过可创建表单的查询语句
     */
    public final Class<? extends AbstractStateFormQuery> queryClass;
    
    public StateFormNamedQuery(String name,
                @NonNull Class<R> resultClass,
                @NonNull Class<? extends AbstractStateFormQuery> queryClass) {
        if (StringUtils.isBlank(name)) {
            throw new StateFormNamedQueryNameInvalidException(name);
        }
        this.name = name;
        this.queryClass = queryClass;
        this.resultClass = resultClass;
    }
}