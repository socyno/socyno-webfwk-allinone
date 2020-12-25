package com.weimob.webfwk.util.tool;

import java.util.UUID;

public class DataUtil {

    public static String randomGuid() {
        return UUID.randomUUID().toString();
    }
    
    public static String compress(String data) {
        byte[] bytes;
        if (data == null || data.length() < 50 || (bytes = GZipUtil.StringCompress(data)) == null) {
            return data;
        }
        String compressed = "gz:" + Base64Util.encode(bytes);
        return compressed.length() > data.length() ? data : compressed;
    }

    public static String uncompress(String data) {
        if (data == null || !data.startsWith("gz:")) {
            return data;
        }
        try {
            return GZipUtil.StringUncompress(Base64Util.decode(data.substring(3).getBytes("ISO-8859-1")));
        } catch (Exception e) {
            return data;
        }
    }
}
