package org.socyno.webfwk.util.vcs.svn;

import org.tmatesoft.svn.core.io.SVNRepository;

public abstract class SubversionProcessor {
    public abstract void run(SVNRepository repo) throws Exception;
}
