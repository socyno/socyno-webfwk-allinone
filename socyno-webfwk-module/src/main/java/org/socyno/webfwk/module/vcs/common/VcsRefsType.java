package org.socyno.webfwk.module.vcs.common;

import org.socyno.webfwk.util.exception.NamingFormatInvalidException;

import lombok.Getter;

@Getter
public enum VcsRefsType {
    Master("主干")
    , Patch("补丁")
    , Tag("标签")
    , Branch("分支");
    
    private final String display;
    
    VcsRefsType(String display) {
        this.display = display;
    }
    
    public static VcsRefsType forName(String name) {
        return forName(name, false);
    }
    
    public static VcsRefsType forName(String name, boolean nullIfNotFound) {
        for (VcsRefsType v : VcsRefsType.values()) {
            if (v.name().equalsIgnoreCase(name)) {
                return v;
            }
        }
        if (nullIfNotFound) {
            return null;
        }
        throw new NamingFormatInvalidException(String.format("源码引用类型(%s)不可识别", name));
    }
}
