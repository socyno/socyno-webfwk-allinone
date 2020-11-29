package org.socyno.webfwk.state.authority;


public class AuthorityScopeIdNoopMultipleParser implements AuthorityScopeIdMultipleParser {
    @Override
    public long[] getAuthorityScopeIds(Object scopeSources) {
        return null;
    }
}
