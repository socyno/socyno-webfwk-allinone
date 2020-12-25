package com.weimob.webfwk.module.vcs.common;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.util.exception.NamingFormatInvalidException;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class VcsUnifiedSubsystemInfo {
    
    @Attributes(title = "应用的命名空间", required = true)
    private final String namespace;
    
    @Attributes(title = "应用仓库权限组", required = true)
    private final String permissionGroup;
    
    public VcsUnifiedSubsystemInfo(String namespace, String permissionGroup) {
        this.namespace = namespace;
        this.permissionGroup = permissionGroup;
    }
    
    public String getPermissionGroupWithNamespace(@NonNull VcsType vcsType) {
        return String.format("%s%s", getPermissionGroupNamespacePrefix(vcsType), getPermissionGroup());
    }
    
    public String getPermissionGroupNamespacePrefix(@NonNull VcsType vcsType) {
        if (VcsType.Gitlab.equals(vcsType)) {
            return String.format("%s/", namespace).toLowerCase();
        }
        if (VcsType.Subversion.equals(vcsType)) {
            return String.format("%s-", namespace).toLowerCase();
        }
        throw new NamingFormatInvalidException(String.format("源码仓库系统(%s)未定义授权组规则", vcsType));
    }
}
