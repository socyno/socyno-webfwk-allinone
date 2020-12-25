package com.weimob.webfwk.util.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.weimob.webfwk.util.remote.CommonDownloadHandler;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.remote.SshClient;

public abstract class AbstractSftpService {
    
    protected abstract String getHostname();
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
    
    protected abstract String getHttpUrl();
    
    protected abstract String toHttpUrl(String path);
    
    protected abstract String getRootDir();
    
    private String getStoragePath(String remoteFile) {
        return HttpUtil.concatUrlPath(getRootDir(), remoteFile);
    }
    
    @SuppressWarnings("unused")
    private void put(String localFile, String remoteFile)
            throws FileNotFoundException, JSchException,
                        SftpException, IOException {
        SshClient.putFile(
                getHostname(),
                getUsername(),
                getPassword(),
                new FileInputStream(localFile),
                getStoragePath(remoteFile));
    }
    
    public void put(byte[] data, String remoteFile)
            throws FileNotFoundException, JSchException,
                        SftpException, IOException {
        SshClient.putFile(
                getHostname(),
                getUsername(),
                getPassword(),
                data,
                getStoragePath(remoteFile));
    }
    
    public void put(InputStream fileStream, String remoteFile)
            throws FileNotFoundException, JSchException,
                        SftpException, IOException {
        SshClient.putFile(
                getHostname(),
                getUsername(),
                getPassword(),
                fileStream,
                getStoragePath(remoteFile));
    }
    
    public byte[] get(String remoteFile) throws Exception {
        return SshClient.getFile(getHostname(), getUsername(), getPassword(), getStoragePath(remoteFile));
    }
    
    public void download(String remoteFile, final OutputStream outputStream) throws Exception {
        SshClient.downloadFile(
                getHostname(),
                getUsername(),
                getPassword(),
                getStoragePath(remoteFile),
                new CommonDownloadHandler() {
                    @Override
                    public void process(InputStream stream) throws IOException {
                        IOUtils.copy(stream, outputStream);
                    }
                });
    }
}
