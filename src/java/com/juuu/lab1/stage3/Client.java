package com.juuu.lab1.stage3;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final String FILE_PATH = "test";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Connected to server");

            // 初始化 DH 密钥对生成器
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(2048);

            // 生成客户端的密钥对
            KeyPair clientKeyPair = keyPairGenerator.generateKeyPair();
            // 公钥A
            PublicKey clientPublicKey = clientKeyPair.getPublic();
            // 私钥a
            PrivateKey clientPrivateKey = clientKeyPair.getPrivate();

            // 接收服务端的公钥B
            System.out.println("2. client接收公钥B");
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] serverPublicKeyBytes = (byte[]) inputStream.readObject();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("DH");
            PublicKey serverPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            // 发送客户端的公钥A给服务端
            System.out.println("3. client发送公钥A");
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(clientPublicKey.getEncoded());

            // 客户端计算共享密钥K
            KeyAgreement clientKeyAgreement = KeyAgreement.getInstance("DH");
            clientKeyAgreement.init(clientPrivateKey);
            clientKeyAgreement.doPhase(serverPublicKey, true);
            byte[] clientSharedSecret = clientKeyAgreement.generateSecret();

            // 从共享密钥中提取 AES 密钥
            SecretKey clientAesKey = new SecretKeySpec(clientSharedSecret, 0, 16, "AES");
            System.out.println("client计算共享密钥K："+ Arrays.toString(clientAesKey.getEncoded()));

            // 读取文件内容
            String fileContent = readFile(FILE_PATH);
            System.out.println("读取文件内容："+fileContent);

            // 加密文件内容
            String encryptedText = encrypt(fileContent, clientAesKey);
            System.out.println("加密文件内容："+encryptedText);

            // 发送加密后的文件内容
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            byte[] encryptedData = encryptedText.getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeInt(encryptedData.length);
            dataOutputStream.write(encryptedData);

            System.out.println("File sent successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 读取文件内容
    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        // 获取类加载器
        ClassLoader classLoader = Client.class.getClassLoader();
        // 从类路径获取文件输入流
        try (InputStream inputStream = classLoader.getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

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
}