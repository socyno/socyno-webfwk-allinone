package com.weimob.webfwk.util.remote;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.weimob.webfwk.util.tool.Base64Util;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

@Slf4j
public class SshClient {
	private final static int DEFAULT_ERRCODE=-100;
    private final static int DEFAULT_PORT=22;
    private final static int DEFAULT_TIMEOUT=60000;
    private final static Charset DEFAULT_CHARSET=Charset.forName("UTF-8");
    
    public static CmdResult exec(String host, String user,
             String password, String command) {
        return exec(host, user, password, "", DEFAULT_PORT, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET);
    }
    
    public static void putFile(
            String host,
            String user, 
            String password,
            File command, 
            String destPath, 
            FileMonitor monitor) throws JSchException, SftpException, IOException {
        putFiles(host, DEFAULT_PORT, user, password, "",
                command, DEFAULT_TIMEOUT, destPath, 
                false, monitor);
    }
    
    public static void putFile(
            final String host,
            final String user,
            final String password,
            byte[] data,
            String destPath,
            FileMonitor monitor
    ) throws JSchException, SftpException, IOException {
        putFiles(host, DEFAULT_PORT, user, password, "",
                data, DEFAULT_TIMEOUT, destPath, false, monitor);
    }
    public static int exec2(
            Session session,
            String command,
            CmdMonitor monitor) {
        return exec2(session, command, null, monitor);
    }
    public static int exec2(
            Session session,
            String command,
            Charset chartSet,
            CmdMonitor monitor) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec)session.openChannel("exec");
            return exec2(channel, command, chartSet, monitor);
        } catch ( Exception e ) {
            monitor.errorMessageFetched(e.toString());
        } finally {
            closeChannel(channel);
        }
        return DEFAULT_ERRCODE;
    }
    
    private static int exec2(
            ChannelExec channel,
            String command,
            Charset chartSet,
            CmdMonitor monitor) {

        if ( chartSet == null ) {
            chartSet = DEFAULT_CHARSET;
        }
        int resultStatus = DEFAULT_ERRCODE;
        final StringBuffer resultOutText = new StringBuffer();
        final CharsetDecoder decoder = chartSet.newDecoder();
        try {
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            InputStream error = channel.getErrStream();
            channel.connect();
            
            int bytesLeftErr = 0;
            int bytesLeftOut = 0;
            int fetchedRealen = 0;
            int fetchedExplen = 1024;
            byte[] bufferFetched = new byte[fetchedExplen];
            byte[] bufferBytesOut = new byte[fetchedExplen + 10];
            byte[] bufferBytesErr = new byte[fetchedExplen + 10];
            while( true ) {
                monitor.beforceFetchMessage();
                while ( input.available() > 0 ) {
                    resultOutText.setLength(0);
                    if ( (fetchedRealen=input.read(bufferFetched, 0, fetchedExplen)) < 0 ) {
                        break;
                    }
                    System.arraycopy(bufferFetched, 0, bufferBytesOut, bytesLeftOut, fetchedRealen);
                    if ( (bytesLeftOut=StringUtils.stringBufferAppend(resultOutText, bufferBytesOut, 
                            decoder, 0, bytesLeftOut + fetchedRealen)) > 0 ) {
                        System.arraycopy(bufferFetched, fetchedRealen - bytesLeftOut, bufferBytesOut, 0, bytesLeftOut);
                    }
                    if ( resultOutText.length() > 0 ) {
                        monitor.outputMessageFetched(resultOutText.toString());
                    }
                }
                while ( error.available() > 0  ) {
                    resultOutText.setLength(0);
                    if ( (fetchedRealen=error.read(bufferFetched, 0, fetchedExplen)) < 0 ) {
                        break;
                    }
                    System.arraycopy(bufferFetched, 0, bufferBytesErr, bytesLeftErr, fetchedRealen);
                    if ( (bytesLeftErr=StringUtils.stringBufferAppend(resultOutText, bufferBytesErr, 
                            decoder, 0, bytesLeftErr + fetchedRealen)) > 0 ) {
                        System.arraycopy(bufferFetched, fetchedRealen - bytesLeftErr, bufferBytesErr, 0, bytesLeftErr);
                    }
                    if ( resultOutText.length() > 0 ) {
                        monitor.errorMessageFetched(resultOutText.toString());
                    }
                }
                if( channel.isClosed() ) {
                    if( input.available() > 0 || error.available() > 0 ) {
                        continue;
                    }
                    resultStatus = channel.getExitStatus();
                    break;
                }
                monitor.afterFetchMessage();
                try { Thread.sleep(1000); }
                catch (Exception e) {}
            }
        } catch(Exception e) {
            monitor.errorMessageFetched(e.toString());
        }
        return resultStatus;
    }
    public static CmdResult exec(String host, String user,
             String password, String command, int timeout) {
        return exec(host, user, password, "", DEFAULT_PORT, command, timeout, DEFAULT_CHARSET);
    }
    
    public static CmdResult exec(String host, String user,
             String password, String command, Charset charset) {
        return exec(host, user, password, "", DEFAULT_PORT, command, DEFAULT_TIMEOUT, charset);
    }
    
    public static CmdResult exec(String host, String user,
             String password, String command, int timeout, Charset charset) {
        return exec(host, user, password, "", DEFAULT_PORT, command, timeout, charset);
    }
    
    public static CmdResult exec(String host, String user,
             String password, int port, String command) {
        return exec(host, user, password, "", port, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET);
    }
    
    public static CmdResult exec(String host, String user,
            String password, int port, String command, int timeout ) {
       return exec(host, user, password, "", port, command, timeout, DEFAULT_CHARSET);
   }
    
    public static CmdResult exec(String host, String user,
            String password, int port, String command, Charset charset ) {
       return exec(host, user, password, "", port, command, DEFAULT_TIMEOUT, charset);
   }

    public static CmdResult exec(String host, String user, 
              final String password,
              final String passphrase,
              String command ) {
        return exec(host, user, password, passphrase, DEFAULT_PORT, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET);
    }

    public static CmdResult exec(String host, String user, 
              final String password,
              final String passphrase,
              int port,
              String command ) {
        return exec(host, user, password, passphrase, port, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET);
    }
    
    public static int exec2(String host, String user, 
            final String password,
            final String passphrase,
            int port,
            String command,
            int timeout,
            Charset chartSet,
            CmdMonitor monitor) {
        
        Session session = null;
        ChannelExec channel = null;

        if ( chartSet == null ) {
            chartSet = DEFAULT_CHARSET;
        }
        int resultStatus = -100;
        final StringBuffer consoleBuffer = new StringBuffer();
        final CharsetDecoder decoder = chartSet.newDecoder();
        
        try {
            session = getSession(host, user, password, passphrase, port, timeout);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            InputStream error = channel.getErrStream();
            channel.connect(timeout);
            
            int bytesLeftErr = 0;
            int bytesLeftOut = 0;
            int fetchedRealen = 0;
            int fetchedExplen = 1024;
            byte[] bufferFetched = new byte[fetchedExplen];
            byte[] bufferBytesOut = new byte[fetchedExplen + 10];
            byte[] bufferBytesErr = new byte[fetchedExplen + 10];
            while (true) {
                monitor.beforceFetchMessage();
                while (input.available() > 0) {
                    consoleBuffer.setLength(0);
                    if ((fetchedRealen = input.read(bufferFetched, 0, fetchedExplen)) < 0) {
                        break;
                    }
                    System.arraycopy(bufferFetched, 0, bufferBytesOut, bytesLeftOut, fetchedRealen);
                    if ((bytesLeftOut = stringBufferAppend(consoleBuffer, bufferBytesOut, decoder, 0,
                            bytesLeftOut + fetchedRealen)) > 0) {
                        System.arraycopy(bufferFetched, fetchedRealen - bytesLeftOut, bufferBytesOut, 0, bytesLeftOut);
                    }
                    if (consoleBuffer.length() > 0) {
                        monitor.outputMessageFetched(consoleBuffer.toString());
                    }
                }
                while (error.available() > 0) {
                    consoleBuffer.setLength(0);
                    if ((fetchedRealen = error.read(bufferFetched, 0, fetchedExplen)) < 0) {
                        break;
                    }
                    System.arraycopy(bufferFetched, 0, bufferBytesErr, bytesLeftErr, fetchedRealen);
                    if ((bytesLeftErr = stringBufferAppend(consoleBuffer, bufferBytesErr, decoder, 0,
                            bytesLeftErr + fetchedRealen)) > 0) {
                        System.arraycopy(bufferFetched, fetchedRealen - bytesLeftErr, bufferBytesErr, 0, bytesLeftErr);
                    }
                    if (consoleBuffer.length() > 0) {
                        monitor.errorMessageFetched(consoleBuffer.toString());
                    }
                }
                if (channel.isClosed()) {
                    if (input.available() > 0 || error.available() > 0) {
                        continue;
                    }
                    resultStatus = channel.getExitStatus();
                    break;
                }
                monitor.afterFetchMessage();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
            consoleBuffer.append(e.toString());
            monitor.errorMessageFetched(consoleBuffer.toString());
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
        return resultStatus;
        
    }
    
    public static CmdResult exec(String host, String user, 
              final String password,
              final String passphrase,
              int port,
              String command,
              int timeout,
              Charset chartset) {
        final StringBuffer stdout = new StringBuffer();
        final StringBuffer stderr = new StringBuffer();
        int status = exec2(host, user, password, passphrase, port, command, timeout, chartset, new CmdMonitor() {
            @Override
            public void errorMessageFetched(String error) {
                stderr.append(error);
            }
            @Override
            public void outputMessageFetched(String output) {
                stdout.append(output);                
            }
        } );
        return new CmdResult(status, stdout.toString(), stderr.toString());
    }
    
    public static int exec2(String host, String user,
            final String password,
            String command,
            Charset charset,
            CmdMonitor monitor ) {
        return exec2(host, user, password, "", DEFAULT_PORT, command, DEFAULT_TIMEOUT, charset, monitor);
    }
    
    public static int exec2(String host, String user,
              final String password,
              String command,
              int timeout,
              CmdMonitor monitor ) {
        return exec2(host, user, password, "", DEFAULT_PORT, command, timeout, DEFAULT_CHARSET, monitor);
    }
    
    public static int exec2(String host, String user,
              final String password,
              String command,
              CmdMonitor monitor ) {
        return exec2(host, user, password, "", DEFAULT_PORT, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET, monitor);
    }
    
    public static int exec2(String host, String user,
              final String password,
              final String passphrase,
              String command,
              CmdMonitor monitor ) {
        return exec2(host, user, password, passphrase, DEFAULT_PORT, command,DEFAULT_TIMEOUT, DEFAULT_CHARSET,  monitor);
    }
    
    public static int exec2(String host, String user,
              final String password,
              int port,
              String command,
              CmdMonitor monitor ) {
        return exec2(host, user, password, "", port, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET, monitor);
    }
    
    public static int exec2(String host, String user,
              final String password,
              final String passphrase,
              int port,
              String command,
              CmdMonitor monitor ) {
        return exec2(host, user, password, passphrase, port, command, DEFAULT_TIMEOUT, DEFAULT_CHARSET, monitor);
    }
    
    public static int exec2(String host, String user,
              final String password,
              final String passphrase,
              int port,
              String command,
              int timeout,
              CmdMonitor monitor ) {
        return exec2(host, user, password, passphrase, port, command, timeout, DEFAULT_CHARSET, monitor);
    }
    
    @SuppressWarnings("serial")
    private static Session getSession(String host, String user, final String password, final String passphrase,
            int port, int timeout) throws JSchException {
        JSch jsch = new JSch();
        final String keyStarts = "sshkey:";
        if (StringUtils.startsWith(password, keyStarts)) {
            String[] sshKeys;
            if ((sshKeys = CommonUtil.split(password.substring(keyStarts.length()), "[,:]+",
                    CommonUtil.STR_NONBLANK)) == null || sshKeys.length < 2) {
                throw new JSchException("Invalid ssh keys provided");
            }
            byte[] keyPassphrase = null;
            if (sshKeys.length > 2) {
                keyPassphrase = sshKeys[2].getBytes();
            }
            jsch.addIdentity("kdssh-identity", Base64Util.decode(sshKeys[0]), Base64Util.decode(sshKeys[1]),
                    keyPassphrase);
        }
        Session session = jsch.getSession(user, host, port);
        session.setUserInfo(new UserInfo() {
            @Override
            public String getPassphrase() {
                return passphrase;
            }
            
            @Override
            public String getPassword() {
                return password;
            }
            
            @Override
            public boolean promptPassphrase(String msg) {
                return true;
            }
            
            @Override
            public boolean promptPassword(String msg) {
                return true;
            }
            
            @Override
            public boolean promptYesNo(String msg) {
                return true;
            }
            
            @Override
            public void showMessage(String msg) {
                return;
            }
        });
        session.setConfig(new Properties() {
            {
                put("HashKnownHosts", "yes");
                put("StrictHostKeyChecking", "no");
            }
        });
        session.connect(timeout <= 0 ? DEFAULT_TIMEOUT : timeout);
        return session;
    }
    
    public static List<String> getFiles(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            int timeout,
            Map<String, OutputStream> files
    ) throws JSchException, SftpException  {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSession(host, user, password, passphrase, port, timeout);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            List<String> ok = new ArrayList<String>();
            for ( Map.Entry<String, OutputStream> entry : files.entrySet() ) {
                try {
                    channel.get(entry.getKey(), entry.getValue());
                    ok.add(entry.getKey());
                } catch ( Exception e ) {}
            }
            return ok;
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
    }

    public static boolean getFile(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            int timeout,
            final String file,
            final OutputStream os
    ) throws JSchException, SftpException {
        @SuppressWarnings("serial")
        List<String> copied = getFiles(host, port, user, password, passphrase, timeout,
                new HashMap<String, OutputStream>() { {
            put(file, os);
        } });
        return !copied.isEmpty();
    }

    public static byte[] getFile(
            final String host,
            final String user,
            final String password,
            String file
    ) throws JSchException, SftpException, IOException {
        Map<String, byte[]> files = getFiles(host, DEFAULT_PORT, user, password,
                    null, DEFAULT_TIMEOUT, new String[]{file});
        if ( files != null && files.size() >= 1 ) {
            for ( byte[] data : files.values() ) {
                return data;
            }
        }
        return null;
    }
    
    public static byte[] getFile(
            final String host,
            final int port,
            final String user,
            final String password,
            String file
    ) throws JSchException, SftpException, IOException {
        Map<String, byte[]> files = getFiles(host, port, user, password,
                    null, DEFAULT_TIMEOUT, new String[]{file});
        if ( files != null && files.size() >= 1 ) {
            for ( byte[] data : files.values() ) {
                return data;
            }
        }
        return null;
    }
    
    public static byte[] getFile(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            int timeout,
            String file
    ) throws JSchException, SftpException, IOException {
        Map<String, byte[]> files = getFiles(host, port, user, password,
                    passphrase,timeout,new String[]{file});
        if ( files != null && files.size() >= 1 ) {
            for ( byte[] data : files.values() ) {
                return data;
            }
        }
        return null;
    }
    
    public static long getSize (
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            int timeout,
            String filePath) throws JSchException, SftpException, IOException  {
        SftpATTRS stat = getStat(host, port, user, password, passphrase, timeout, filePath);
        return stat == null ? -1 : stat.getSize();
    }
    
    public static SftpATTRS getStat (
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            int timeout,
            String filePath) throws JSchException, SftpException, IOException  {
        if ( StringUtils.isBlank(filePath) ) {
            return null;
        }
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSession(host, user, password, passphrase, port, timeout);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return channel.lstat(filePath);
        } catch ( SftpException e ) {
            if ( e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
                throw e;
            }
            return null;
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
    }
    
    public static Map<String, byte[]> getFiles(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            int timeout,
            String[] files) throws JSchException, SftpException, IOException  {
        if ( files == null || files.length == 0 ) {
            return null;
        }
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSession(host, user, password, passphrase, port, timeout);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            Map <String, byte[]> bytes = new HashMap<String, byte[]>();
            for ( int i = 0; i < files.length; i++ ) {
                String filepath = files[i];
                if ( bytes.containsKey(filepath) ) {
                    continue;
                }
                try {
                    bytes.put(filepath, IOUtils.toByteArray(channel.get(filepath)));
                } catch ( Exception e ) {}
            }
            return bytes;
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
    }
    
    public static boolean downloadFile(
            final String host,
            final String user,
            final String password,
            String filePath,
            CommonDownloadHandler handler
    		) throws JSchException, SftpException, IOException  {
    	if (StringUtils.isBlank(filePath)) {
            return false;
        }
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSession(host, user, password, null, DEFAULT_PORT, DEFAULT_TIMEOUT);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            InputStream stream = null;
			try {
				stream = channel.get(filePath);
				handler.process(stream);
				return true;
			} finally {
				IOUtils.closeQuietly(stream);
			}
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
    }
    
    public static void getFile(
            final String host,
            final String user,
            final String password,
            final String passphrase,
            final int port,
            String filepath,
            int timeout,
            HttpServletResponse response,
            String filename
    ) throws JSchException, SftpException, IOException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSession(host, user, password, passphrase, port, timeout);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            SftpATTRS stat = channel.stat(filepath);
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-disposition", "attachment;filename="
                    + MimeUtility.encodeText(filename, "UTF-8", "B")) ;
            response.setHeader("Content-Length", String.valueOf(stat.getSize()));
            channel.get(filepath, response.getOutputStream());
        } finally {
            try {
                response.getOutputStream().close();
            } catch ( Exception e ) {
                log.warn("Failed to close http response stream", e);
            }
            closeChannel(channel);
            closeSession(session);
        }
    }

    public static void putFile(
            final String host,
            final String user,
            final String password,
            byte[] data,
            String destPath
    ) throws JSchException, SftpException, IOException {
        putFiles(host, DEFAULT_PORT, user, password, "",
                data, DEFAULT_TIMEOUT, destPath, false, null);
        
    }

    public static void putFile(
            final String host,
            final String user,
            final String password,
            InputStream inputStream,
            String destPath
    ) throws JSchException, SftpException, IOException {
        putFiles(host, DEFAULT_PORT, user, password, "",
                inputStream, DEFAULT_TIMEOUT, destPath, false, null);
    }
    
    public static List<String> putFiles(
            final String host,
            final String user,
            final String password,
            File fileOrDir,
            String destPath
    ) throws JSchException, SftpException, IOException {
        return putFiles(host, DEFAULT_PORT, user, password, "",
                    fileOrDir, DEFAULT_TIMEOUT, destPath, false, null);
    }
    
    public static List<String> putFiles(
            final String host,
            final String user,
            final String password,
            File fileOrDir,
            String destPath,
            boolean destAsDir
    ) throws JSchException, SftpException, IOException {
        return putFiles(host, DEFAULT_PORT, user, password, "",
                    fileOrDir, DEFAULT_TIMEOUT, destPath, destAsDir, null);
    }

    public static void putFile(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            byte[] data,
            int timeout,
            String destPath,
            FileMonitor monitor
    ) throws JSchException, SftpException, IOException {
        putFiles(host, port, user, password, passphrase,
                data, timeout, destPath, false, monitor );
    }
    
    public static void putFile(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            InputStream inputStream,
            int timeout,
            String destPath,
            FileMonitor monitor
    ) throws JSchException, SftpException, IOException {
        putFiles(host, port, user, password, passphrase,
                inputStream, timeout, destPath, false, monitor );
    }
    
    public static List<String> putFiles(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            File fileOrDir,
            int timeout,
            String destPath,
            boolean destAsDir,
            FileMonitor monitor
    ) throws JSchException, SftpException, IOException {
        return putFiles(host, port, user, password, passphrase,
                    (Object)fileOrDir, timeout, destPath, destAsDir, monitor );
    }
    
    private static List<String> putFiles(
            final String host,
            final int port,
            final String user,
            final String password,
            final String passphrase,
            Object fileOrDir,
            int timeout,
            String destPath,
            boolean destAsDir,
            FileMonitor monitor
    ) throws JSchException, SftpException, IOException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSession(host, user, password, passphrase, port, timeout);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return putFiles(channel, fileOrDir, destPath, destAsDir, monitor);
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
    }

    private static List<String> putFiles(ChannelSftp channel, String fileOrDir, String destPath,
            boolean destAsDir, FileMonitor monitor) throws SftpException, IOException {
        return putFiles(channel, new File(fileOrDir), destPath, destAsDir, monitor, false);
    }
    
    private static List<String> putFiles(ChannelSftp channel, Object fileOrDir, String destPath,
            boolean destAsDir, FileMonitor monitor ) throws SftpException, IOException {
        return putFiles(channel, fileOrDir, destPath, destAsDir, monitor, true);
    }
    
    private static List<String> putFiles(ChannelSftp channel, Object fileOrDir, String destPath,
                boolean destAsDir, FileMonitor monitor, boolean isLastFile ) throws SftpException, IOException {
        List<String> files = new ArrayList<String>();
        InputStream inputStream = null;
        String baseSourceName = null;
        if ( fileOrDir instanceof InputStream ) {
            destAsDir = false;
            inputStream = (InputStream)fileOrDir;
        } else if ( fileOrDir instanceof byte[] ) {
            destAsDir = false;
            inputStream = new ByteArrayInputStream((byte[])fileOrDir);
        } else if ( fileOrDir instanceof File ) {
            if ( ((File)fileOrDir).isDirectory() ) {
                for ( String entry : ((File)fileOrDir).list() ) {
                    files.addAll( putFiles(channel,
                        (String)(fileOrDir + File.separator + entry),
                        destPath + "/" + entry, false, monitor) );
                }
                if ( isLastFile && monitor != null ) {
                    try {
                        monitor.allFilesTransfered(channel.getSession(), files);
                    } catch (JSchException e) {
                        throw new SftpException(ChannelSftp.SSH_FX_NO_CONNECTION,
                                    "Failed to get session from channel", e);
                    }
                }
                return files;
            }
            baseSourceName = ((File)fileOrDir).getName();
            inputStream = new FileInputStream((File)fileOrDir);
        } else {
            throw new IOException("Invalid source file.");
        }
        SftpATTRS stat = null;
        OutputStream outputStream = null;
        try {
            try { stat = channel.stat(destPath); }
            catch ( SftpException e ) {
                if ( e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
                    throw e;
                }
            }
            if ( destAsDir || (stat != null && stat.isDir()) ) {
                if ( baseSourceName != null ) {
                    destPath += "/" + baseSourceName;
                }
            }
            if ( stat == null ) {
                List<String> names = new ArrayList<String>();
                if ( !destPath.startsWith("/") ) {
                    destPath = channel.pwd() + "/" + destPath;
                }
                for ( String name : StringUtils.split(destPath, "/") ) {
                    if ( name.isEmpty() || name.equals(".") ) {
                        continue;
                    }
                    if ( name.equals("..")  ) {
                        if ( !names.isEmpty() ) {
                            names.remove(names.size() - 1);
                        }
                        continue;
                    }
                    names.add(names.isEmpty() ? ("/" + name) : (names.get(names.size() - 1) + "/" + name));
                }
                destPath = names.remove(names.size() - 1);
                int existsIndex = names.size() - 1;
                for( ; existsIndex >= 0; existsIndex-- ) {
                    try { stat = channel.stat(names.get(existsIndex)); }
                    catch ( SftpException e ) {
                        if ( e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
                            throw e;
                        }
                    }
                    if ( stat != null ) {
                        if ( !stat.isDir() ) {
                            throw new TargetIsNotDirException(names.get(existsIndex));
                        }
                        break;
                    }
                }
                for ( int i = existsIndex + 1; i < names.size(); i++ ) {
                    channel.mkdir(names.get(i));
                }
            } else {
                if ( !stat.isDir() && destAsDir ) {
                    throw new TargetIsNotDirException(destPath);
                }
            }
            
            if ( monitor != null ) {
                monitor.beforceFileTransfer(destPath);
            }
            outputStream = channel.put(destPath);
            int fetchedRealen = 10240;
            int fetchedExplen = 10240;
            byte[] bufferFetched = new byte[fetchedExplen];
            while ( (fetchedRealen=inputStream.read(bufferFetched, 0, fetchedExplen)) > 0 ) {
                outputStream.write(bufferFetched, 0, fetchedRealen);
                if ( monitor != null ) {
                    monitor.chunkTransfered(destPath, fetchedRealen);
                }
            }
            if ( monitor != null ) {
                monitor.afterFileTransfer(destPath);
            }
            files.add(destPath);
            if ( isLastFile && monitor != null ) {
                try {
                    monitor.allFilesTransfered(channel.getSession(), files);
                } catch (JSchException e) {
                    throw new SftpException(ChannelSftp.SSH_FX_NO_CONNECTION,
                                "Failed to get session from channel", e);
                }
            }
            return files;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
    
    public static class CmdResult {
        
        private int status;
        private String outText;
        private String errText;
        public CmdResult( int s, String o, String e ) {
            status = s;
            outText = o;
            errText = e;
        }
        
        public int getStatus () {
            return status;
        }
        
        public String getOutText () {
            return outText;
        }
        
        public String getErrText () {
            return errText;
        }
    }
    
    public static abstract class CmdMonitor {
        public void afterFetchMessage() {
            
        }
        public void beforceFetchMessage() {
            
        }
        public abstract void errorMessageFetched(String error);
        public abstract void outputMessageFetched(String output);
    }
    
    public static class FileMonitor {
        public void afterFileTransfer(String path) {
            
        }
        public void allFilesTransfered(Session session, List<String> result) {
            
        }
        public void beforceFileTransfer(String path) {
            
        }
        public void chunkTransfered(String destPath, int chunkLength) {

        }
    }
    
    @SuppressWarnings("serial")
    static class TargetIsNotDirException extends IOException {
        private String path;
        private String message;

        public TargetIsNotDirException(String path) {
            this.path = path;
            this.message = String.format("The target path is not directory : %s", path);
        }
        public TargetIsNotDirException(String path, String message) {
            this.path = path;
            this.message = message;
        }
        
        @Override
        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }

    private static int stringBufferAppend( StringBuffer bf,
            byte[] bytes, CharsetDecoder decoder, int offset, int length ) {
        int left = 0;
        if ( decoder == null ) {
            decoder = Charset.defaultCharset().newDecoder();
        }
        while ( left < length ) {
            try {
                bf.append( decoder.decode(
                    ByteBuffer.wrap(bytes,offset, length-left)
                ).toString() );
                break;
            } catch (CharacterCodingException e) {
                left++;
                continue;
            }
        }
        return left;
    }
    
    private static void closeChannel(Channel channel) {
        if ( channel != null ) {
            try {
                channel.disconnect();
            } catch ( Exception e ) {
                log.warn("Failed to close ssh channel", e);
            }
        }
    }

    private static void closeSession(Session session) {
        if ( session != null ) {
            try {
                session.disconnect();
            } catch ( Exception e ) {
                log.warn("Failed to close ssh session", e);
            }
        }
    }
    
//    public static void main(String[] args) throws IOException, JSchException, SftpException {
////        final String tmpcmd = String.format("/tmp/%s", UUID.randomUUID());
////        File command = new File("D:/ivysettings/repository/ivy-cache/xerces");
////        putFile("1x92.1x68.4x7.1x0", "root", "111111", command,
////                tmpcmd, new SshClient.FileMonitor() {
////            @Override
////            public void afterFileTransfer(String destPath) {
////                System.err.println(destPath);
////            }
////            @Override
////            public void allFilesTransfered(Session session, List<String> result) {
////                System.err.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCC");
////            }
////        } );
//        
//        
//        
//        if ( args == null || args.length < 2 ) {
//            System.err.println("No user or host or command provided.");
//            System.err.println("jsshexec user[:password]@hostname command");
//            System.exit(1);
//        }
//        String user = null, password = "";
//        String host = args[0];
//        String command = StringUtils.trimToEmpty(args[1]);
//        int userPosIndex = -1;
//        if ( (userPosIndex=host.indexOf('@')) > 0 ) {
//            user = host.substring(0, userPosIndex);
//            host = StringUtils.trimToEmpty(host.substring(userPosIndex+1));
//            if ( (userPosIndex=user.indexOf(':')) > 0 ) {
//                password = user.substring(userPosIndex+1);
//                user = user.substring(0, userPosIndex);
//            }
//        }
//        user = StringUtils.trimToEmpty(user);
//        host = StringUtils.trimToEmpty(host);
//        if ( user.isEmpty() || host.isEmpty() || command.isEmpty() ) {
//            System.err.println("No user or host or command provided.");
//            System.err.println("jsshexec user:password@hostname command");
//            System.exit(1);
//        }
//        System.err.println(String.format("ssh %s@%s %s", user, host, command));
//        
//        if ( command.startsWith("@") ) {
//            try {
//                String tmpcmd = String.format("/tmp/%s", UUID.randomUUID());
//                putFile(host, user, password, new FileInputStream(command.substring(1)),
//                        tmpcmd );
//                command = String.format("chmod +x '%s' && '%s'", tmpcmd, tmpcmd);
//            } catch ( Exception e ) {
//                e.printStackTrace();
//                System.exit(1);
//            }
//        }
//        System.exit( exec2(host, user, password, command, new CmdMonitor() {
//            @Override
//            public void errorMessageFetched(String message) {
//                System.err.print(message);
//            }
//            @Override
//            public void outputMessageFetched(String message) {
//                System.out.print(message);
//            }
//        } ) );
//    }
}
