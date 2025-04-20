package com.juuu.lab1;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESExample {

    // 加密方法
    public static String encrypt(String plainText, String key) throws Exception {
        // 创建 AES 密钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        // 获取 Cipher 实例，指定算法、模式和填充方式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // 初始化 Cipher 为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        // 执行加密操作
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        // 使用 Base64 编码将加密后的字节数组转换为字符串
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密方法
    public static String decrypt(String encryptedText, String key) throws Exception {
        // 创建 AES 密钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        // 获取 Cipher 实例，指定算法、模式和填充方式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // 初始化 Cipher 为解密模式
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        // 使用 Base64 解码将加密字符串转换为字节数组
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        // 执行解密操作
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        // 将解密后的字节数组转换为字符串
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            // 要加密的明文
            String plainText = "Hello, AES Encryption!";
            // 密钥，AES 密钥长度可以是 16 字节（128 位）、24 字节（192 位）或 32 字节（256 位）
            String key = "0123456789abcdef";

            System.out.println("加密前的文本: " + plainText);

            // 加密操作
            String encryptedText = encrypt(plainText, key);
            System.out.println("加密后的文本: " + encryptedText);

            // 解密操作
            String decryptedText = decrypt(encryptedText, key);
            System.out.println("解密后的文本: " + decryptedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}