package com.weimob.webfwk.module.vcs.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class VcsTagEntry {
    private String name;
    private String target;
    private String message;
    private String refOrCommit;
}
