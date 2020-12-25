package com.weimob.webfwk.module.vcs.common;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.ClassUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

@Getter
@Setter
@Accessors(chain = true)
public class VcsUnifiedAppInitInfo extends VcsUnifiedSubsystemInfo {

    @Attributes(title = "应用仓库的名称", required = true)
    private String name;

    @Attributes(title = "应用仓库类型", required = true)
    private String vcsType;

    @Attributes(title = "应用仓库地址")
    private String vcsPath;

    @Attributes(title = "应用的描述信息")
    private String description;

    public VcsUnifiedAppInitInfo(String namespace, String permissionGroup) {
        super(namespace, permissionGroup);
    }

    @SchemaIgnore
    public VcsType getVcsTypeEnum() {
        return VcsType.forName(vcsType);
    }
    
    @SchemaIgnore
    public String getPathWithNamespace() {
        String vcsPath;
        ClassUtil.checkFormRequiredAndOpValue(this);
        String pathWithNs = String.format("%s/%s", getNamespace(), getName());
        if (StringUtils.isNotBlank(vcsPath = getVcsPath())) {
            String[] paths;
            if ((paths = CommonUtil.split(vcsPath, "/", CommonUtil.STR_NONBLANK)) != null && paths.length > 2) {
                pathWithNs = String.format("%s/%s", paths[paths.length - 2], paths[paths.length - 1]);
                if (VcsType.Gitlab.equals(getVcsTypeEnum())) {
                    pathWithNs = pathWithNs.replaceAll("\\.git$", "");
                }
            }
        }
        return pathWithNs;
    }
    
    @SchemaIgnore
    public String getSubversionTrunkPath() {
        return HttpUtil.concatUrlPath(getPathWithNamespace(), VcsType.Subversion.getMasterName());
    }
    
    @SchemaIgnore
    public String getSubversionBranchesPath() {
        return HttpUtil.concatUrlPath(getPathWithNamespace(), VcsType.Subversion.getBranchesPrefix());
    }
    
    @SchemaIgnore
    public String getSubversionTagsPath() {
        return HttpUtil.concatUrlPath(getPathWithNamespace(), VcsType.Subversion.getTagsPrefix());
    }
    
    @SchemaIgnore
    public String getSubversionPatchesPath() {
        return HttpUtil.concatUrlPath(getPathWithNamespace(), VcsType.Subversion.getPatchesPrefix());
    }
}
