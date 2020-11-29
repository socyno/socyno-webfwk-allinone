package org.socyno.webfwk.state.service;

import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.service.AbstractSftpService;

public class CommonSftpService {
    
    public final static AbstractSftpService DEFAULT = new AbstractSftpService() {
        
        @Override
        protected String getHostname() {
            return ContextUtil.getConfigTrimed("system.sftp.common.host.name");
        }
        
        @Override
        protected String getUsername() {
            return ContextUtil.getConfigTrimed("system.sftp.common.host.user");
        }
        
        @Override
        protected String getPassword() {
            return ContextUtil.getConfigTrimed("system.sftp.common.host.password");
        }
        
        @Override
        protected String getHttpUrl() {
            return ContextUtil.getConfigTrimed("system.sftp.common.host.httpurl");
        }
        
        @Override
        protected String toHttpUrl(String path) {
            if (path == null) {
                path = "";
            }
            if (path.startsWith(getRootDir())) {
                path = path.substring(getRootDir().length());
            }
            return String.format("%s/%s", getHttpUrl(), path).replace("\\", "/");
        }
        
        @Override
        protected String getRootDir() {
            return "/data/";
        }
    };
}
