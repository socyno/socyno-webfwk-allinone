package org.socyno.webfwk.state.authority;


public class AuthoritySpecialNoopChecker implements AuthoritySpecialChecker {
    public boolean check(Object scopeSource) {
        return false;
    }
}
