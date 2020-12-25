package com.weimob.webfwk.module.vcs.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VcsPatchEntry {
    private String name;
    private String branchName;
    private String prefixName;
}
