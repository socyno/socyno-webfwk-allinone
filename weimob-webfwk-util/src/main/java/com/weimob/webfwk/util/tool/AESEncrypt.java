package com.weimob.webfwk.util.tool;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.weimob.webfwk.util.context.ContextUtil;

public class AESEncrypt {
    
    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecretKey secretKey = keyGen.generateKey();
        return Base64Util.encode(secretKey.getEncoded());
    }
    
    private static byte[] getKey() throws Exception {
        String defaultKey = "Qw3FvwrFFAGMCHoC38NPAw==";
        if (ContextUtil.configAccessable()) {
            return Base64Util.decode(CommonUtil.ifBlank(ContextUtil.getConfigTrimed("system.encrypt.tool.security.key"),
                    defaultKey));
        }
        return Base64Util.decode(defaultKey);
    }
    
    public static byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(getKey(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    
    public static byte[] decryptAsBase64(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(getKey(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(Base64Util.decode(data));
    }
    
    public static String encryptAsBase64(String data) throws Exception {
        byte[] bytes = null;;
        if (data != null) {
            bytes = data.getBytes();
        }
        return encryptAsBase64(bytes);
    }
    
    public static String encryptAsBase64(byte[] data) throws Exception {
        return Base64Util.encode(encrypt(data));
    }
    
    public static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(getKey(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
//    
//    public static void main(String[] args) throws Exception  {
//        String data = "houasnenlsdfnlkasdf";
//        String aesData = encryptAsBase64(data);
//        System.out.println(aesData);
//        System.out.println(new String(Base64Util.decode(aesData)));
//        System.out.println(new String(decryptAsBase64(aesData)));
//    }
}
