package org.socyno.webfwk.module.vcs.common;

import lombok.Data;

@Data
public class VcsRevisionDiffEntry {
    private String  newPath;
    private String  oldPath;
    private String  diff;
    private boolean newFile = false;
    private boolean renamedFile = false;
    private boolean deletedFile = false;
}
