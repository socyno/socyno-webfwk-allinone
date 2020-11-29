package org.socyno.webfwk.module.app.bookmark;

import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

@Attributes(title = "应用收藏管理")
public class ApplicationBookmarkService {
    
    @Getter
    private static final ApplicationBookmarkService instance = new ApplicationBookmarkService();
    
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
