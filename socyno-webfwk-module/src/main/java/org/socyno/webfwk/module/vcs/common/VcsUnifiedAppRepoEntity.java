package org.socyno.webfwk.module.vcs.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VcsUnifiedAppRepoEntity {
    
    private String namedId;
    
    private String pathToRepo;
}
