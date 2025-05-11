<<<<<<<< HEAD:src/java/com/juuu/lab1/Server.java
package com.juuu.lab1;
========
package java.com.juuu.lab1.stage3;
>>>>>>>> origin/master:src/java/com/juuu/lab1/stage3/Server.java

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            // 初始化 DH 密钥对生成器
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(2048);

            // 生成服务端的密钥对
            KeyPair serverKeyPair = keyPairGenerator.generateKeyPair();
            // 公钥B
            PublicKey serverPublicKey = serverKeyPair.getPublic();
            // 私钥b
            PrivateKey serverPrivateKey = serverKeyPair.getPrivate();

            // 发送服务端的公钥B给客户端
            System.out.println("1. server发送公钥B");
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(serverPublicKey.getEncoded());

            // 接收客户端的公钥A
            System.out.println("4. server接收公钥A");
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] clientPublicKeyBytes = (byte[]) inputStream.readObject();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(clientPublicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("DH");
            PublicKey clientPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            // 服务端计算共享密钥K
            KeyAgreement serverKeyAgreement = KeyAgreement.getInstance("DH");
            serverKeyAgreement.init(serverPrivateKey);
            serverKeyAgreement.doPhase(clientPublicKey, true);
            byte[] serverSharedSecret = serverKeyAgreement.generateSecret();

            // 从共享密钥中提取 AES 密钥
            SecretKey serverAesKey = new SecretKeySpec(serverSharedSecret, 0, 16, "AES");
            System.out.println("server计算共享密钥K："+ Arrays.toString(serverAesKey.getEncoded()));

            // 接收加密后的文件内容
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int encryptedLength = dataInputStream.readInt();
            byte[] encryptedData = new byte[encryptedLength];
            dataInputStream.readFully(encryptedData);
            System.out.println("接收文件内容："+new String(encryptedData));

            // 解密文件内容
            String decryptedText = decrypt(new String(encryptedData, StandardCharsets.UTF_8), serverAesKey);
            System.out.println("解密文件内容："+decryptedText);

            // 将解密后的内容保存到文件
            try (FileWriter writer = new FileWriter("received_file.txt")) {
                writer.write(decryptedText);
            }
            System.out.println("File received and decrypted successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
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
}