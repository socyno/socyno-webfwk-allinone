package org.socyno.webfwk.module.deploy.cluster;

/**
 * 集群的租户命名空间字段类型定义，用于集群管理通用流程中注册租户的命名空间
 *
 */
public class FieldDeployNamespaceWithAdd extends FieldDeployNamespace {
    
    @Override
    public Class<?> getListItemCreationFormClass() {
        return CreationDeployClusterNamespace.class;
    }
    
}
