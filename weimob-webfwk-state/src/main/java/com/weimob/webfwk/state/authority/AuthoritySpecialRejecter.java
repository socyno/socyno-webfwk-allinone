package com.weimob.webfwk.state.authority;


public interface AuthoritySpecialRejecter {
    public boolean check(Object scopeSource) throws Exception;
}
