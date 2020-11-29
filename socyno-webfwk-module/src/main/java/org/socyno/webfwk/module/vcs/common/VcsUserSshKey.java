package org.socyno.webfwk.module.vcs.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VcsUserSshKey {

    private String id;

    private String title;

    private String keyContent;
}
