package org.socyno.webfwk.util.tool;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.socyno.webfwk.util.exception.MessageException;

import lombok.NonNull;

public abstract class FileTailer {
    
    public static class FileSavedContentChangedException extends MessageException {
        private static final long serialVersionUID = 1L;
        
        public FileSavedContentChangedException() {
            super("读取的文件被变更,无法继续读取内容.");
        }
    }
    
    public static class FileNewDataTimeoutOutException extends MessageException {
        private static final long serialVersionUID = 1L;
        
        public FileNewDataTimeoutOutException() {
            super("等待文件内容更新超时,终止内容的读取.");
        }
    }
    
    private final String file;
    private final int timeoutMs;
    private final Charset charset;
    private final int maxLines;
    private final int checkInterval = 2000;
    
    public FileTailer(String file) {
        this(file, 0, 0, null);
    }
    
    public FileTailer(String file, int timeoutMs) {
        this(file, 0, timeoutMs, null);
    }
    
    public FileTailer(String file, Charset charset) {
        this(file, 0, 0, charset);
    }
    
    public FileTailer(String file, int maxLines, int timeoutMs) {
        this(file, maxLines, timeoutMs, null);
    }

    public FileTailer(String file, int maxLines, Charset charset) {
        this(file, maxLines, 0, charset);
    }
    
    public FileTailer(String file, int maxLines, int timeoutMs, Charset charset) {
        this.file = file;
        this.maxLines = maxLines <= 0 ? 500 : maxLines;
        this.timeoutMs = timeoutMs <= 0 ? 30000 : timeoutMs;
        this.charset = CommonUtil.ifNull(charset, Charset.forName("UTF-8"));
    }
    
    public static String getOnce(String path, int maxLines) throws Exception {
        return getOnce(path, maxLines, null);
    }
    
    public static String getOnce(String path, int maxLines, Charset charset) throws Exception {
        final StringBuffer buffer = new StringBuffer();
        new FileTailer(path, maxLines, 10, charset) {
            
            @Override
            protected void onLineRead(String line) throws IOException {
                buffer.append(line);
            }
            
            @Override
            protected boolean onChunkEnd() throws IOException {
                return true;
            }
            
            @Override
            protected void onChunkStarted() throws IOException {
                
            }
            
        }.start();
        return buffer.toString();
    }
    
    
    private RandomAccessFile getFileHandle() throws InterruptedException {
        int timeoutCounter = timeoutMs;
        while (true) {
            try {
                RandomAccessFile handle = new RandomAccessFile(file, "r");
                try {
                    while (handle.length() <= 0) {
                        Thread.sleep(checkInterval);
                        if ((timeoutCounter -= checkInterval) <= 0) {
                            return null;
                        }
                    }
                    return handle;
                } finally {
                    if (handle.length() <= 0) {
                        IOUtils.closeQuietly(handle);
                    }
                }
            } catch (IOException e) {
                Thread.sleep(checkInterval);
                if ((timeoutCounter -= checkInterval) <= 0) {
                    return null;
                }
            }
        }
    }
    
    public void start() throws Exception {
        long fileLength = 0L;
        int timeoutCounter = 0;
        RandomAccessFile handle = getFileHandle();
        try {
            onChunkStarted();
            final List<String> readLines = new ArrayList<>();
            reverseReadlines(handle, new ReadlineStopped() {
                @Override
                public boolean check(String string) throws Exception {
                    readLines.add(0, string);
                    return readLines.size() >= maxLines;
                }
            }, charset);
            handle.seek(handle.length());
            while (readLines.size() > 0) {
                onLineRead(readLines.remove(0));
            }
            while (true) {
                /* 执行片段结束回调，并判断是否完成 */
                if (onChunkEnd()) {
                    break;
                }
                /* 等待片刻后再次读取 */
                Thread.sleep(checkInterval);
                
                /* 获取文件新的长度 */
                fileLength = handle.length();
                
                /* 当文件被重置，则从头开始读取 */
                if (fileLength < handle.getFilePointer()) {
                    throw new FileSavedContentChangedException();
                }
                
                /* 超时没有内容变更，退出 */
                if (fileLength == handle.getFilePointer()) {
                    Thread.sleep(checkInterval);
                    if ((timeoutCounter += checkInterval) > timeoutMs) {
                        throw new FileNewDataTimeoutOutException();
                    }
                    continue;
                }
                /* 重置超时时间， 并读取文件的新内容 */
                timeoutCounter = 0;
                String line;
                onChunkStarted();
                while ((line = handle.readLine()) != null) {
                    onLineRead(new String(line.getBytes("ISO8859-1"),
                                        charset));
                }
          }
        } finally {
            IOUtils.closeQuietly(handle);
        }
    }
    public static abstract class ReadlineStopped {
        public abstract boolean check(String string) throws Exception;
    }
    
    private void reverseReadlines(@NonNull RandomAccessFile fileAccessor, @NonNull ReadlineStopped processor,
            Charset charset) throws Exception {
        char positionChar;
        Character positionPrevChar;
        StringBuffer lineBuffer = new StringBuffer();
        MappedByteBuffer mappedByteBuffer = fileAccessor.getChannel().map(FileChannel.MapMode.READ_ONLY, 0,
                fileAccessor.length());
        CharBuffer charBuffer = charset.decode(mappedByteBuffer.asReadOnlyBuffer());
        positionPrevChar = null;
        for (int i = charBuffer.length() - 1; i >= 0; i--) {
            positionChar = charBuffer.charAt(i);
            if ((positionChar == '\n' || (positionChar == '\r' && positionPrevChar != null && positionPrevChar != '\n'))
                    && lineBuffer.length() > 0) {
                if (processor.check(lineBuffer.toString())) {
                    return;
                }
                lineBuffer.setLength(0);
            }
            lineBuffer.insert(0, positionChar);
            positionPrevChar = positionChar;
        }
        if (lineBuffer.length() > 0) {
            processor.check(lineBuffer.toString());
        }
    }
    
    protected abstract void onLineRead(String line) throws IOException;
    protected abstract boolean onChunkEnd() throws IOException;
    protected abstract void onChunkStarted() throws IOException;
}
