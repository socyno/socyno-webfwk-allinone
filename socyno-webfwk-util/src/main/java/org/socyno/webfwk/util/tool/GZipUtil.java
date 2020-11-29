package org.socyno.webfwk.util.tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {

    /**
     * 字符串压缩为GZIP字节数组
     * 
     * @param str
     * @param encoding
     * @return byte[]
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public static byte[] compress(String str, String encoding) 
            throws UnsupportedEncodingException, IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        GZIPOutputStream gzip = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(encoding));
        } finally {
            if ( gzip != null ) {
                gzip.close();
            }
        }
        return out.toByteArray();
    }

    /**
     * GZIP解压缩
     * 
     * @param bytes
     * @return byte[]
     * @throws IOException 
     */
    public static byte[] uncompress(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        GZIPInputStream ungzip = null;
        ByteArrayInputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(bytes);
            ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            if ( ungzip != null ) {
                ungzip.close();
            }
            if ( out != null ) {
                out.close();
            }
            if ( in != null ) {
                in.close();
            }
        }
    }

    /**
     * 
     * @param data
     * @param encoding
     * @return string
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public static String uncompress(byte[] data, String encoding)
            throws UnsupportedEncodingException, IOException {
        byte[] ungziped = uncompress(data);
        if (ungziped == null || ungziped.length == 0) {
            return null;
        }
        return new String(ungziped, encoding);
    }

    /**
     * 
     * @param data
     * @return string
     */
    public static byte[] StringCompress(String str) {
        try { return compress(str, Charset.defaultCharset().name()); }
        catch (IOException e) {}
        try { return compress(str, "UTF-8"); }
        catch (IOException e) {}
        try { return compress(str, "ISO-8859-1"); }
        catch (IOException e) {}
        try { return compress(str, "ISO-8859-16"); }
        catch (IOException e) {}
        return null;
    }
    
    /**
     * 
     * @param data
     * @return string
     */
    public static String StringUncompress(byte[] bytes) {
        try { return uncompress(bytes, Charset.defaultCharset().name()); }
        catch (IOException e) {}
        try { return uncompress(bytes, "UTF-8"); }
        catch (IOException e) {}
        try { return uncompress(bytes, "ISO-8859-1"); }
        catch (IOException e) {}
        try { return uncompress(bytes, "ISO-8859-16"); }
        catch (IOException e) {}
        return null;
    }  
}
