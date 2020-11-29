package org.socyno.webfwk.state.authority;

import org.socyno.webfwk.util.context.SessionContext;

public class AuthorityEveryoneChecker implements AuthoritySpecialChecker {

    @Override
    public boolean check(Object scopeSource) throws Exception {
        return SessionContext.getUserId() != null;
    }
}
