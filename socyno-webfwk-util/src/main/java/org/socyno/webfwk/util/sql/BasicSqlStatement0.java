package org.socyno.webfwk.util.sql;

import org.apache.commons.lang3.ArrayUtils;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BasicSqlStatement0<T extends AbstractSqlStatement> implements AbstractSqlStatement {
    private String sql;
    private Object[] values;
    
    @SuppressWarnings("unchecked")
    public T setSql(String sql) {
        this.sql = sql;
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T setValues(Object[] values) {
        this.values = ArrayUtils.clone(values);
        return (T)this;
    }
}
