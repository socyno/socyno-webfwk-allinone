package com.weimob.webfwk.state.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonAttachementPath extends CommonAttachementItem {
    private String path;
    private String contentType;
}
