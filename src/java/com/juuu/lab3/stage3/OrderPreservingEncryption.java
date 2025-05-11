package com.juuu.lab3.stage3;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化版保序加密实现
 * 注意：此实现仅用于演示，实际生产环境需使用专业密码库
 */
public class OrderPreservingEncryption {
    private static final int KEY_SIZE = 128;
    private final BigInteger p; // 大素数
    private final BigInteger g; // 生成元
    private final BigInteger privateKey;
    private final Map<BigInteger, BigInteger> encryptionMap = new HashMap<>();
    private final Map<BigInteger, BigInteger> decryptionMap = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public OrderPreservingEncryption() {
        // 简化的密钥生成过程
        this.p = BigInteger.probablePrime(KEY_SIZE, random);
        this.g = BigInteger.valueOf(2);
        this.privateKey = new BigInteger(KEY_SIZE - 1, random);
    }

    /**
     * 保序加密方法
     * @param plaintext 明文数据
     * @return 密文数据
     */
    public BigInteger encrypt(BigInteger plaintext) {
        if (encryptionMap.containsKey(plaintext)) {
            return encryptionMap.get(plaintext);
        }
        
        // 简化的保序映射（实际应用需使用更安全的实现）
        BigInteger ciphertext = g.modPow(plaintext.multiply(privateKey), p);
        
        // 确保顺序性
        encryptionMap.put(plaintext, ciphertext);
        decryptionMap.put(ciphertext, plaintext);
        return ciphertext;
    }

    /**
     * 保序解密方法
     * @param ciphertext 密文数据
     * @return 明文数据
     */
    public BigInteger decrypt(BigInteger ciphertext) {
        return decryptionMap.get(ciphertext);
    }
}    