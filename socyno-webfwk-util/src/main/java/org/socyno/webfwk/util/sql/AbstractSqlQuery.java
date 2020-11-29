package org.socyno.webfwk.util.sql;

import java.util.Map;

import com.github.reinert.jjschema.SchemaIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.AccessLevel;

@Data
public abstract class AbstractSqlQuery {
    @Getter(AccessLevel.NONE)
    private long page = 1;
    
    @Getter(AccessLevel.NONE)
    private int  limit = 20;
    
    public abstract AbstractSqlStatement getSqlTotal();
    public abstract AbstractSqlStatement getSqlQuery();
    
    public final int getLimit() {
        return limit <= 0 ? 20 : limit;
    }
    
    public final long getPage() {
        return (page <= 0 ? 1 : page);
    }
    
    @SchemaIgnore
    public final long getOffset() {
        return (getPage()  - 1) * getLimit();
    }
    
    @SchemaIgnore
    public Map<String, String> getFieldMapper() {
        return null;
    }
}
