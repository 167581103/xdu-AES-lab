package com.juuu.lab3.stage1;


import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

/**
 * 基于 Programmable Order-Preserving Secure Index for Encrypted Database Query 实现的保序加密类
 * 对任意一维数据集实现了保序加密
 * 可在一维密文上实现范围查询
 */
public class OPE {

    private final static Double BASE = 10E6;

    private final BigDecimal a; // 线性变换参数 a > 0
    private final BigDecimal b; // 平移参数 b
    private final SecureRandom random = new SecureRandom();

    // 随机化a和b
    public OPE() {
        this.a = BigDecimal.valueOf(random.nextDouble() * BASE);
        this.b = BigDecimal.valueOf(random.nextDouble() * BASE);
    }

    // 初始化a和b
    public OPE(Double a, Double b) {
        this.a = BigDecimal.valueOf(a);
        this.b = BigDecimal.valueOf(b);
    }

    /**
     * 保序加密方法，加密整个数据集，返回整个数据集
     */
    public List<EncryptedData> encrypt(List<BigDecimal> dataSet) {
        if (dataSet.isEmpty()) {
            throw new IllegalArgumentException("数据集不能为空");
        }

        // 复制并排序数据集
        List<BigDecimal> sortedData = new ArrayList<>(dataSet);
        sortedData.sort(BigDecimal::compareTo);

        // 计算敏感性Sens
        BigDecimal sens = null;
        for (int i = 0; i < sortedData.size() - 1; i++) {
            BigDecimal diff = sortedData.get(i + 1).subtract(sortedData.get(i)).abs();
            if (sens == null || diff.compareTo(sens) < 0) {
                sens = diff;
            }
        }

        // 如果所有元素都相同，设置一个默认的小值作为sens
        if (sens == null) {
            sens = BigDecimal.valueOf(1e-10); // 或其他合适的小值
        }

        // 计算噪声范围
        BigDecimal noiseRandom = a.multiply(sens);

        // 遍历数据集，求加密数据
        List<EncryptedData> res = new ArrayList<>();
        for (BigDecimal data : dataSet) {
            // E(v) = a*v + b + noise
            res.add(new EncryptedData(data, a.multiply(data).add(b).add(BigDecimal.valueOf(random.nextDouble()).multiply(noiseRandom))));
        }

        return res;
    }

    public BigDecimal getMinIndex(BigDecimal cond) {
        return a.multiply(cond).add(b);
    }

    public class EncryptedData {
        private final BigDecimal originalValue;
        private final BigDecimal encryptedValue;

        public EncryptedData(BigDecimal originalValue, BigDecimal encryptedValue) {
            this.originalValue = originalValue;
            this.encryptedValue = encryptedValue;
        }

        public BigDecimal getOriginalValue() {
            return originalValue;
        }

        public BigDecimal getEncryptedValue() {
            return encryptedValue;
        }

        @Override
        public String toString() {
            return "Original: " + originalValue + ", Encrypted: " + encryptedValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EncryptedData that = (EncryptedData) o;
            return originalValue.equals(that.originalValue) &&
                    encryptedValue.equals(that.encryptedValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalValue, encryptedValue);
        }
    }

    public static void main(String[] args) {
        OPE ope = new OPE();
        List<BigDecimal> dataSet = new ArrayList<>();
        for (int i=0;i<10;i++) {
            dataSet.add(new BigDecimal(new Random().nextInt(100)));
        }
        List<EncryptedData> res = ope.encrypt(dataSet);

        dataSet.sort(BigDecimal::compareTo);
        res.sort(Comparator.comparing(EncryptedData::getEncryptedValue));
        System.out.println("原数据集：" + dataSet);
        System.out.println("加密数据集：" + res);

        // 基于加密后的数据集进行范围查询
        Scanner in = new Scanner(System.in);
        System.out.print("请输入要查询的值：");
        Long num = in.nextLong();
        System.out.println(binarySearch(res, ope.getMinIndex(BigDecimal.valueOf(num))));
    }

    private static EncryptedData binarySearch(List<EncryptedData> dataSet, BigDecimal num) {
        int left = 0,right = dataSet.size()-1;
        while(left <= right) {
            int mid = left+(right-left)/2;
            if (dataSet.get(mid).getEncryptedValue().compareTo(num) > 0) {
                right = mid - 1;
            } else if (dataSet.get(mid).getEncryptedValue().compareTo(num) < 0){
                left = mid + 1;
            } else return dataSet.get(mid);
        }
        return dataSet.get(left);
    }
}
