package com.weimob.webfwk.state.authority;

import com.weimob.webfwk.util.context.SessionContext;

public class AuthorityEveryoneChecker implements AuthoritySpecialChecker {

    @Override
    public boolean check(Object scopeSource) throws Exception {
        return SessionContext.getUserId() != null;
    }
}
