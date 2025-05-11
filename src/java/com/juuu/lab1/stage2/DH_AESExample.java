package com.juuu.lab1.stage2;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class DH_AESExample {

    // 加密方法
    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        // 生成随机的初始化向量
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // 获取 Cipher 实例，指定算法、模式和填充方式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // 初始化 Cipher 为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        // 执行加密操作
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 将初始化向量和加密后的字节数组拼接在一起
        byte[] encryptedWithIv = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedWithIv, iv.length, encryptedBytes.length);

        // 使用 Base64 编码将加密后的字节数组转换为字符串
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    // 解密方法
    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        // 使用 Base64 解码将加密字符串转换为字节数组
        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

        // 提取初始化向量
        byte[] iv = new byte[16];
        System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // 提取加密后的字节数组
        byte[] encryptedBytes = new byte[encryptedWithIv.length - iv.length];
        System.arraycopy(encryptedWithIv, iv.length, encryptedBytes, 0, encryptedBytes.length);

        // 获取 Cipher 实例，指定算法、模式和填充方式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // 初始化 Cipher 为解密模式
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        // 执行解密操作
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        // 将解密后的字节数组转换为字符串
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            // 要加密的明文
            String plainText = "Hello, AES Encryption!";

            // 初始化 DH 密钥对生成器
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(2048);

            // 生成 Alice 的密钥对
            KeyPair aliceKeyPair = keyPairGenerator.generateKeyPair();
            PublicKey alicePublicKey = aliceKeyPair.getPublic();
            PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();

            // 生成 Bob 的密钥对
            KeyPair bobKeyPair = keyPairGenerator.generateKeyPair();
            PublicKey bobPublicKey = bobKeyPair.getPublic();
            PrivateKey bobPrivateKey = bobKeyPair.getPrivate();

            // Alice 计算共享密钥
            KeyAgreement aliceKeyAgreement = KeyAgreement.getInstance("DH");
            aliceKeyAgreement.init(alicePrivateKey);
            aliceKeyAgreement.doPhase(bobPublicKey, true);
            byte[] aliceSharedSecret = aliceKeyAgreement.generateSecret();

            // Bob 计算共享密钥
            KeyAgreement bobKeyAgreement = KeyAgreement.getInstance("DH");
            bobKeyAgreement.init(bobPrivateKey);
            bobKeyAgreement.doPhase(alicePublicKey, true);
            byte[] bobSharedSecret = bobKeyAgreement.generateSecret();

            // 从共享密钥中提取 AES 密钥
            SecretKey aliceAesKey = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");
            SecretKey bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");

            System.out.println("加密前的文本: " + plainText);

            // Alice 加密操作
            String encryptedText = encrypt(plainText, aliceAesKey);
            System.out.println("加密后的文本: " + encryptedText);

            // Bob 解密操作
            String decryptedText = decrypt(encryptedText, bobAesKey);
            System.out.println("解密后的文本: " + decryptedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}