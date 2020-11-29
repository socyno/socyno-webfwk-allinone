package org.socyno.webfwk.state.basic;

import java.util.List;
import java.util.Map;

import org.socyno.webfwk.util.sql.AbstractSqlStatement;

import com.github.reinert.jjschema.SchemaIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.AccessLevel;

@Data
public abstract class AbstractStateFormQuery {
    @Getter(AccessLevel.NONE)
    private long page = 1;
    
    @Getter(AccessLevel.NONE)
    private int  limit = 20;
    
    public AbstractStateFormQuery() {
        this((Integer)null, (Long)null);
    }
    
    public AbstractStateFormQuery(Integer limit) {
        this(limit, (Long)null);
    }
    
    public AbstractStateFormQuery(Integer limit, Long page) {
        if (page != null && page > 0) { 
            this.page = page;
        }
        if (limit != null && limit > 0) { 
            this.limit = limit;
        }
    }
    
    public AbstractStateFormQuery(Integer limit, Integer page) {
        this(limit, page == null ? null : page.longValue());
    }
    
    public abstract AbstractSqlStatement prepareSqlTotal() throws Exception;
    public abstract AbstractSqlStatement prepareSqlQuery() throws Exception;
    
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
    
    public <T extends AbstractStateForm> List<T> processResultSet(Class<T> itemClazz, List<T> resultSet) throws Exception {
        return resultSet;
    }
}
