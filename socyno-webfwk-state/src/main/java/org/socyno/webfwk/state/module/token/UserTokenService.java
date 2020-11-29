package org.socyno.webfwk.state.module.token;

import java.util.Date;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.LoginTokenUtil;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ChecksumUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import com.auth0.jwt.interfaces.DecodedJWT;

public class UserTokenService {
    
    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    public static String getTokenHeader () {
        return SessionContextInterceptor.DEFAULT.getTokenHeader();
    }
    
    /**
     * SELECT * FROM system_token_discard WHERE discard_token =?
     */
    @Multiline
    private final static String SQL_QUERY_SYSTEM_TOKEN_DISCARD_BY_TOKEN = "X";
    
    /**
     * 检查令牌是否被标记为失效
     */
    public static boolean checkTokenDiscard(String token) throws Exception {
        if (StringUtils.isBlank(token)) {
            return true;
        }
        return getDao().queryAsMap(SQL_QUERY_SYSTEM_TOKEN_DISCARD_BY_TOKEN,
                new Object[] { ChecksumUtil.getSHA256(token.getBytes()) }) != null;
    }
    
    /**
     * 添加令牌
     */
    public static void markTokenDiscard(String token) throws Exception {
        if (StringUtils.isBlank(token)) {
            return;
        }
        DecodedJWT jwt;
        if ((jwt = LoginTokenUtil.getToken(token)) == null) {
            return;
        }
        Date expires = jwt.getExpiresAt();
        getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                "system_token_discard", new ObjectMap()
                       .put("discard_token", ChecksumUtil.getSHA256(token.getBytes()))
                       .put("expiration_time", CommonUtil.ifNull(expires, 0, expires.getTime()))
        ));
        /* 添加完成后，对已过期的令牌进行清除 */
        cleanExpiredToken();
    }
    
    /**
     * DELETE FROM system_token_discard WHERE expiration_time > 0 AND expiration_time < ?
     */
    @Multiline
    private final static String SQL_DALETE_SYSTEM_TOKEN_DISCARD = "X";
    
    /**
     * 清楚全部已经过期的废除令牌
     */
    public static void cleanExpiredToken() throws Exception {
        getDao().executeUpdate(SQL_DALETE_SYSTEM_TOKEN_DISCARD, new Object[] { new Date().getTime() });
    }
}

