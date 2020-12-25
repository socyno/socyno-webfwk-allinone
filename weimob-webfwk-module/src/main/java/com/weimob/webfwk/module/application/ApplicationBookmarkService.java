package com.weimob.webfwk.module.application;

import org.apache.commons.lang3.ArrayUtils;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;

@Attributes(title = "应用收藏管理")
public class ApplicationBookmarkService {
    
    private ApplicationBookmarkService() {
        
    }
    
    @Getter
    private static final ApplicationBookmarkService Instance = new ApplicationBookmarkService();
    
    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    protected String getFormTable() {
        return "application_bookmark";
    }
    
    public void add(long applicationId) throws Exception {
        getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(),
                new ObjectMap().put("app_id", applicationId).put("=user_id", SessionContext.getUserId())));
    }
    
    public void delete(long applicationId) throws Exception {
        getDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(getFormTable(),
                new ObjectMap().put("=app_id", applicationId).put("=user_id", SessionContext.getUserId())));
    }
    
    public Long[] check(Long[] applicationIds) throws Exception {
        if (applicationIds == null) {
            return new Long[0];
        }
        return getDao()
                .queryAsList(Long.class,
                        String.format("SELECT app_id FROM %s WHERE user_id = ? and app_id IN (%s)",
                                getFormTable(), CommonUtil.join("?", applicationIds.length, ",")),
                        ArrayUtils.addAll(new Object[] { SessionContext.getUserId() }, (Object[]) applicationIds))
                .toArray(new Long[0]);
    }
}
