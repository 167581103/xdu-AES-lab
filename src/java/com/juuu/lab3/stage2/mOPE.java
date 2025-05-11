package com.juuu.lab3.stage2;

import com.juuu.lab3.stage2.Impl.KDTreeImpl;
import com.juuu.lab3.stage2.util.EncryptUtil;

import javax.crypto.SecretKey;
import java.util.*;

public class mOPE {
    // n维-KD树，可对N维数据进行有序建模
    private KDTree kdTree;
    // OPE-Encoding密文映射表
    private Map<String, Byte> table;
    // mOPE客户端与服务端交互的密钥
    private SecretKey secretKey = EncryptUtil.generateKey();

    public mOPE() {

    }

    public mOPE(List<Point> dataSet) {
        encrypt(dataSet.stream().findAny().get().getDimension(), dataSet);
    }

    public mOPE(Integer dimension, List<Point> dataSet) {
        encrypt(dimension, dataSet);
    }

    /**
     * 对N维点集进行加密，结果保存为KDTree和table
     * @param dataSet
     */
    public void encrypt(Integer dimension, List<Point> dataSet){
        // 对点集进行预处理，将明文点集转换为密文点集
        this.kdTree = new KDTreeImpl(dimension, dataSet, secretKey);
    }

    /**
     * 利用mOPE的保序性，实现安全范围查询
     * @param lowerBound 明文下界
     * @param upperBound 明文上界
     * @return 密文结果集
     */
    public List<String> rangeSearch(Point lowerBound, Point upperBound) {
        return this.kdTree.rangeSearch(lowerBound, upperBound);
    }

    public static void main(String[] args) {
        final Integer DIMENSION = 2;
        // 从NE数据集读取点集
        List<Point> points = new ArrayList<>();
        Scanner in = new Scanner(Objects.requireNonNull(mOPE.class.getClassLoader().getResourceAsStream("NE.txt")));
        while(in.hasNextDouble()) {
            double[] coordinates = new double[DIMENSION];
            for (int i=0;i<DIMENSION;i++) {
                coordinates[i] = in.nextDouble();
            }
            points.add(new Point(coordinates));
        }
        // 通过点集构建二维数据下的mOPE
        mOPE mOPE = new mOPE(points);
        // 通过KDTree实现范围查询
        List<String> points1 = mOPE.rangeSearch(new Point(new double[]{0.1, 0.2}), new Point(new double[]{0.2, 0.3}));
        System.out.println(points1);
    }

}
