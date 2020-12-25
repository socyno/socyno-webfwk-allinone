package com.weimob.webfwk.module.vcs.common;

import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VcsRevisionEntry {
    private String  revision;
    private String  message;
    private String  createdBy;
    private Date    createdAt;
}
