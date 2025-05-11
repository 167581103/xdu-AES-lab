package com.juuu.lab3.stage2.util;

import com.juuu.lab3.stage2.Point;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EncryptUtil {
    // OPE表结构，用于存储明文到密文的映射
    private static Map<Integer, Map<Double, String>> opeTables = new HashMap<>();
    private static final int OPE_MAX_LEVELS = 1000; // OPE树的最大层数

    // 初始化OPE表
    public static void initOpeTable(int dimension, List<Point> points, SecretKey secretKey) {
        opeTables.putIfAbsent(dimension, new HashMap<>());
        Map<Double, String> opeTable = opeTables.get(dimension);

        // 收集所有该维度的坐标值
        Set<Double> coordinates = new TreeSet<>(); // 使用TreeSet自动排序
        for (Point p : points) {
            coordinates.add(p.getCoordinate(dimension));
        }

        // 为每个坐标生成OPE编码
        List<Double> sortedCoords = new ArrayList<>(coordinates);
        for (int i = 0; i < sortedCoords.size(); i++) {
            double coord = sortedCoords.get(i);
            // 生成一个保持顺序的编码，这里简化实现
            String opeCode = generateOpeCode(i, sortedCoords.size(), secretKey);
            opeTable.put(coord, opeCode);
        }
    }

    // 生成OPE编码，这里使用简化的实现
    private static String generateOpeCode(int position, int total, SecretKey secretKey) {
        try {
            // 创建一个基于位置的编码，实际实现中应该使用更安全的方法
            String code = String.format("%010d", position); // 转为固定长度的字符串
            // 使用AES加密这个编码，确保安全性
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error generating OPE code", e);
        }
    }

    // 使用OPE编码加密Point
    public static String encryptWithOPE(Point point, SecretKey secretKey) {
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < point.getDimension(); i++) {
            Map<Double, String> opeTable = opeTables.getOrDefault(i, new HashMap<>());
            double coord = point.getCoordinate(i);

            // 如果OPE表中没有该值，生成一个新的OPE编码
            if (!opeTable.containsKey(coord)) {
                // 这里简化处理，实际中应该维护OPE表的动态更新
                opeTable.put(coord, generateOpeCode(opeTable.size(), OPE_MAX_LEVELS, secretKey));
                opeTables.put(i, opeTable);
            }

            encrypted.append(opeTable.get(coord));
            if (i < point.getDimension() - 1) {
                encrypted.append(",");
            }
        }

        return encrypted.toString();
    }

    // 从OPE编码恢复Point
    public static Point decryptFromOPE(String encryptedStr, SecretKey secretKey) {
        // 注意：OPE是单向的，理论上不能解密
        // 这里只是为了演示，实际应用中不应该有这个方法
        throw new UnsupportedOperationException("OPE is one-way and cannot be decrypted");
    }

    // 生成AES密钥
    public static SecretKey generateKey(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // 可以是128, 192或256位
            return keyGen.generateKey();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加密点
     * @param point
     * @return
     */
    public static String encrypt(Point point, SecretKey secretKey) {
        try {
            StringBuilder pointStr = new StringBuilder();
            for (double coord : point.getCoordinates()) {
                pointStr.append(coord).append(",");
            }
            if (pointStr.length() > 0) {
                pointStr.deleteCharAt(pointStr.length() - 1); // 移除最后一个逗号
            }

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(pointStr.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Point fromEncryptedString(String encryptedStr, SecretKey secretKey){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedStr));
            String decryptedStr = new String(decryptedBytes, StandardCharsets.UTF_8);

            String[] coordStrs = decryptedStr.split(",");
            double[] coords = new double[coordStrs.length];
            for (int i = 0; i < coordStrs.length; i++) {
                coords[i] = Double.parseDouble(coordStrs[i]);
            }
            return new Point(coords);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        Point point = new Point(new double[]{1,2});
        SecretKey secretKey = generateKey();
        String pointCipherText = encrypt(point, secretKey);
        System.out.println(pointCipherText);
        System.out.println(fromEncryptedString(pointCipherText, secretKey));

    }
}
