package com.weimob.webfwk.state.authority;


public class AuthoritySpecialNoopRejecter implements AuthoritySpecialRejecter {
    public boolean check(Object scopeSource) {
        return false;
    }
}
