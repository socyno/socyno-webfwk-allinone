package com.weimob.webfwk.state.annotation.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityParser;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.field.OptionSystemAuth;
import com.weimob.webfwk.state.service.PermissionService;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.exception.MissingUserException;
import com.weimob.webfwk.util.exception.NoAuthorityException;
import com.weimob.webfwk.util.tool.StringUtils;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class AuthorityAspect {
    
    @Around("@annotation(authority)")
    public Object aroundAuthority(ProceedingJoinPoint joinPoint, Authority authority) throws Throwable {
        
        checkAuthority(joinPoint.getArgs(), ((MethodSignature) joinPoint.getSignature()).getMethod());
        return joinPoint.proceed();
    }
    
    /**
     * 校验权限注解
     */
    private void checkAuthority(Object[] args, Method method) throws Exception {
        
        Authority authority;
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        if ((authority = method.getAnnotation(Authority.class)) == null) {
            log.error("未获取请求方法的授权注解实例信息({}::{})。", className, methodName);
            throw new NoAuthorityException("无法获取到授权配置，拒绝访问！");
        }
        OptionSystemAuth systemAuth;
        if ((systemAuth = AuthorityParser.getByMethod(className, methodName)) == null) {
            log.error("未获取请求方法的授权注解缓存数据({}::{})。", className, methodName);
            throw new NoAuthorityException("无法获取到授权配置，拒绝访问！");
        }
        
        /* 匿名接口，允许访问 */
        AuthorityScopeType scopeType = null;
        if ((scopeType = authority.value()).equals(AuthorityScopeType.Guest)) {
            return;
        }
        
        /* 否则，匿名用户拒绝 */
        if ((SessionContext.getUserId()) == null) {
            throw new MissingUserException(String.format("接口禁止匿名访问： %s", systemAuth));
        }
        
        /* 提取访问授权标的 */
        Object scopeId = null;
        if (scopeType.checkScopeId()) {
            int paramIndex = authority.paramIndex();
            if (paramIndex < 0 || paramIndex >= args.length
                    || (scopeId = args[paramIndex]) == null
                    || StringUtils.isBlank((String)(scopeId = authority.parser().newInstance().getAuthorityScopeId(scopeId)))) {
                throw new NoAuthorityException("无法获取到授权标的，拒绝访问！");
            }
        }
        if (PermissionService.hasPermission(systemAuth.getAuth(), scopeType, (String) scopeId)) {
            return;
        }
        throw new NoAuthorityException("授权校验失败，拒绝访问！");
    }
}
