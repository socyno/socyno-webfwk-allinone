package org.socyno.webfwk.state.authority;


public class AuthorityScopeIdNoopMultipleCleaner implements AuthorityScopeIdMultipleCleaner {
    @Override
    public String[] getEventsToClean() {
        return null;
    }
}
