package com.juuu.lab3.stage3;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * NE数据集安全近邻查询演示
 */
public class NEDataDemo {
    private static final int DATA_SIZE = 1000;
    private static final int QUERY_X = 50;
    private static final int QUERY_Y = 50;
    private static final int K = 5;

    public static void main(String[] args) {
        // 初始化保序加密器
        OrderPreservingEncryption ope = new OrderPreservingEncryption();
        
        // 生成模拟NE数据集（二维乱序数据）
        KDTree kdTree = new KDTree(ope);
        Random random = new Random();
        
        System.out.println("生成并加密NE数据集...");
        for (int i = 0; i < DATA_SIZE; i++) {
            int x = random.nextInt(100);
            int y = random.nextInt(100);
            String originalData = "Data_" + i;
            
            // 加密坐标
            BigInteger encryptedX = ope.encrypt(BigInteger.valueOf(x));
            BigInteger encryptedY = ope.encrypt(BigInteger.valueOf(y));
            
            // 构建加密后的点并插入KD树
            KDTree.Point point = new KDTree.Point(encryptedX, encryptedY, originalData);
            kdTree.insert(point);
        }
        
        // 加密查询点
        System.out.println("加密查询点 (" + QUERY_X + ", " + QUERY_Y + ")...");
        BigInteger encryptedQueryX = ope.encrypt(BigInteger.valueOf(QUERY_X));
        BigInteger encryptedQueryY = ope.encrypt(BigInteger.valueOf(QUERY_Y));
        KDTree.Point queryPoint = new KDTree.Point(encryptedQueryX, encryptedQueryY, "QueryPoint");
        
        // 执行安全近邻查询
        System.out.println("执行安全近邻查询 (k=" + K + ")...");
        List<KDTree.Point> nearestNeighbors = kdTree.kNearestNeighbors(queryPoint, K);
        
        // 输出查询结果
        System.out.println("查询结果:");
        for (int i = 0; i < nearestNeighbors.size(); i++) {
            KDTree.Point neighbor = nearestNeighbors.get(i);
            // 解密坐标
            BigInteger decryptedX = ope.decrypt(neighbor.getCoordinate(0));
            BigInteger decryptedY = ope.decrypt(neighbor.getCoordinate(1));
            
            System.out.println((i + 1) + ". 原始数据: " + neighbor.getOriginalData() +
                    ", 坐标: (" + decryptedX + ", " + decryptedY + ")");
        }
    }
}    