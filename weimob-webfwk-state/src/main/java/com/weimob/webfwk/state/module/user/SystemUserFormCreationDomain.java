package com.weimob.webfwk.state.module.user;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加域用户")
public class SystemUserFormCreationDomain extends StateFormBasicInput {

    @Attributes(title = "用户", required = true)
    private String  username;
}
