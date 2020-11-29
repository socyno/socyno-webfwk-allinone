package org.socyno.webfwk.util.sql;

public interface AbstractSqlStatement {
    public String getSql();
    public Object[] getValues();
}
