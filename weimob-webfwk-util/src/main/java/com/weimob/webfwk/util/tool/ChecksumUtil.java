package com.weimob.webfwk.util.tool;

import java.security.MessageDigest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChecksumUtil {
    private static MessageDigest sha256 = null;
    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }
    
    public static String getSHA256(byte[] data) {
        StringBuilder s = new StringBuilder(40);
        for (byte b : sha256.digest(data)) {
            if ((b & 0xff) >> 4 == 0) {
                s.append("0").append(Integer.toHexString(b & 0xff));
            } else {
                s.append(Integer.toHexString(b & 0xff));
            }
        }
        return s.toString();
    }
}
