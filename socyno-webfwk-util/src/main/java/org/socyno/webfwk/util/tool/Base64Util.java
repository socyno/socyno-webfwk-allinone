package org.socyno.webfwk.util.tool;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Base64Util {

    public static byte[] decode(byte[] bytes) {
        return new Base64().decode(bytes);
    }

    public static byte[] decode(String str) {
        try {
            return new Base64().decode(str.getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static String decode(String str, String encoding)
            throws UnsupportedEncodingException {
        return new String(new Base64().decode(str.getBytes()), encoding);
    }
    
    public static String encode(byte[] bytes) {
        try {
            return new String(new Base64().encode(bytes), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String encode(String str, String encoding)
            throws UnsupportedEncodingException{
        return encode(str.getBytes(encoding));
    }
}
