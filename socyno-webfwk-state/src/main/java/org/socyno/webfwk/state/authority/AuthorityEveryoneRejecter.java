package org.socyno.webfwk.state.authority;

public class AuthorityEveryoneRejecter implements AuthoritySpecialRejecter {

    @Override
    public boolean check(Object scopeSource) throws Exception {
        return true;
    }
}
