package org.socyno.webfwk.module.vcs.common;

import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.SchemaIgnore;

import lombok.NonNull;

public enum VcsType {
    Gitlab {
        @Override
        public String getMasterName() {
            return "refs/heads/master";
        }
        
        @Override
        public String getPatchesPrefix() {
            return "refs/heads/patches/";
        }

        @Override
        public String getBranchesPrefix() {
            return "refs/heads/develop/";
        }

        @Override
        public String getTagsPrefix() {
            return "refs/tags/";
        }
    },
    Subversion{
        @Override
        public String getMasterName() {
            return "trunk";
        }
        
        @Override
        public String getPatchesPrefix() {
            return "patches/";
        }

        @Override
        public String getTagsPrefix() {
            return "tags/";
        }

        @Override
        public String getBranchesPrefix() {
            return "branches/";
        }
    };
    
    public static VcsType forName(String vcsType) {
        for (VcsType type : VcsType.values()) {
            if (type.name().equalsIgnoreCase(vcsType)) {
                return type;
            }
        }
        if ("git".equalsIgnoreCase(vcsType)) {
            return Gitlab;
        }
        if ("svn".equalsIgnoreCase(vcsType)) {
            return Subversion;
        }
        throw new NamingFormatInvalidException(String.format("源码仓库类型(%s)不可识别", vcsType));
    }
    
    public VcsRefsType getVcsRefsType(String vcsRefsName) {
        if (StringUtils.equals(vcsRefsName, getMasterName())) {
            return VcsRefsType.Master;
        }
        if (StringUtils.startsWith(vcsRefsName, getTagsPrefix())) {
            return VcsRefsType.Tag;
        }
        if (StringUtils.startsWith(vcsRefsName, getPatchesPrefix())) {
            return VcsRefsType.Patch;
        }
        if (StringUtils.startsWith(vcsRefsName, getBranchesPrefix())) {
            return VcsRefsType.Branch;
        }
        throw new NamingFormatInvalidException(String.format("源码的分支、标签或补丁名称（%s)不可识别", vcsRefsName));
    }
    
    public final String getVcsRefsTypePrefix(@NonNull VcsRefsType vcsRefsType) {
        if (VcsRefsType.Branch.equals(vcsRefsType)) {
            return getBranchesPrefix();
        }
        if (VcsRefsType.Patch.equals(vcsRefsType)) {
            return getPatchesPrefix();
        }
        if (VcsRefsType.Tag.equals(vcsRefsType)) {
            return getTagsPrefix();
        }
        throw new NamingFormatInvalidException(String.format("给定类型(%s)非分支或标签的命名空间", vcsRefsType)); 
    }
    
    public final String getVcsRefsFullName(@NonNull VcsRefsType vcsRefsType, @NonNull String refsName) {
        if (VcsRefsType.Branch.equals(vcsRefsType)) {
            if (!refsName.startsWith(getBranchesPrefix())) {
                return HttpUtil.concatUrlPath(getBranchesPrefix(), refsName);
            }
            return refsName;
        }
        if (VcsRefsType.Patch.equals(vcsRefsType)) {
            if (!refsName.startsWith(getPatchesPrefix())) {
                return HttpUtil.concatUrlPath(getPatchesPrefix(), refsName);
            }
            return refsName;
        }
        if (VcsRefsType.Tag.equals(vcsRefsType)) {
            if (!refsName.startsWith(getTagsPrefix())) {
                return HttpUtil.concatUrlPath(getTagsPrefix(), refsName);
            }
            return refsName;
        }
        throw new NamingFormatInvalidException(String.format("给定类型(%s)非分支或标签的命名空间", vcsRefsType)); 
    }
    
    public abstract String getMasterName();
    
    public abstract String getTagsPrefix();
    
    public abstract String getBranchesPrefix();
    
    public abstract String getPatchesPrefix();
    
    @SchemaIgnore
    public static String getGitRefsSimpleName(@NonNull VcsRefsType refsType, String refsName) {
        final String tagPrefix = "refs/tags/";
        final String branchPrefix = "refs/heads/";
        if (VcsRefsType.Tag.equals(refsType)) {
            if (StringUtils.isNotBlank(refsName) && refsName.startsWith(tagPrefix)) {
                return refsName.substring(tagPrefix.length());
            }
        } else if (StringUtils.isNotBlank(refsName) && refsName.startsWith(branchPrefix)) {
            return refsName.substring(branchPrefix.length());
        }
        return getGitRefsSimpleName(refsType, HttpUtil.concatUrlPath(Gitlab.getVcsRefsTypePrefix(refsType), refsName));
    }
}
