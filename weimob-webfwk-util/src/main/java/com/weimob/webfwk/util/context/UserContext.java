package com.weimob.webfwk.util.context;

import com.weimob.webfwk.util.model.AbstractUser;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserContext {
    /* 用户实体 */
    private AbstractUser sysUser;

    /* 是否为管理员 */
    private boolean admin = false;

    /* 用户令牌 */
    private String token;
    
    /* 用户请求头 */
    private String tokenHead;

    /* 租户代码 */
    private String tenant;
    
    /* 令牌账户 */
    private Long tokenUserId;
    
    /* 令牌账户 */
    private String tokenUsername;
    
    /* 令牌账户 */
    private String tokenDisplay;
    
    /* 代理人账户 */
    private String proxyUsername;

    /* 代理人姓名 */
    private String proxyDisplay;
}
