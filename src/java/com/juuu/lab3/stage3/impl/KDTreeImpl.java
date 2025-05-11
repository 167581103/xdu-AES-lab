package com.juuu.lab3.stage3.impl;

import com.juuu.lab3.stage3.KDNode;
import com.juuu.lab3.stage3.KDTree;
import com.juuu.lab3.stage3.Point;
import com.juuu.lab3.stage1.OPE;

import java.math.BigDecimal;
import java.util.*;

/**
 * KD树 - 优化OPE加密点处理和性能
 */
public class KDTreeImpl implements KDTree {
    private KDNode root;
    private final int k;  // 数据维度
    private final OPE ope;  // OPE加密器

    private static class EncryptedPoint {
        private final Point originalPoint;
        private final BigDecimal[] encryptedValues;
        
        public EncryptedPoint(Point point, BigDecimal[] encryptedValues) {
            this.originalPoint = point;
            this.encryptedValues = encryptedValues;
        }

        public Point getOriginalPoint() {
            return originalPoint;
        }

        public BigDecimal getEncryptedValue(int dimension) {
            return encryptedValues[dimension];
        }
    }
    
    public KDTreeImpl(Integer dimension, List<Point> points) {
        this.k = dimension;
        this.ope = new OPE(); // 初始化OPE加密器
        
        // 预处理：一次性加密所有点
        List<EncryptedPoint> encryptedPoints = preprocessAndEncryptPoints(points);
        
        // 构建KD树
        this.root = buildTree(encryptedPoints, 0);
    }
    
    /**
     * 批量加密所有点，提高效率
     */
    private List<EncryptedPoint> preprocessAndEncryptPoints(List<Point> points) {
        List<EncryptedPoint> encryptedPoints = new ArrayList<>(points.size());
        
        // 为每个维度创建值列表
        List<List<BigDecimal>> dimensionValues = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            dimensionValues.add(new ArrayList<>(points.size()));
        }
        
        // 收集每个维度的所有值
        for (Point point : points) {
            for (int dim = 0; dim < k; dim++) {
                dimensionValues.get(dim).add(BigDecimal.valueOf(point.getCoordinate(dim)));
            }
        }
        
        // 对每个维度进行批量加密
        List<List<OPE.EncryptedData>> encryptedDimensionValues = new ArrayList<>(k);
        for (int dim = 0; dim < k; dim++) {
            encryptedDimensionValues.add(ope.encrypt(dimensionValues.get(dim)));
        }
        
        // 构建加密点列表
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            BigDecimal[] encryptedValues = new BigDecimal[k];
            
            for (int dim = 0; dim < k; dim++) {
                encryptedValues[dim] = encryptedDimensionValues.get(dim).get(i).getEncryptedValue();
            }
            
            encryptedPoints.add(new EncryptedPoint(point, encryptedValues));
        }
        
        return encryptedPoints;
    }
    
    /**
     * 递归构建KD树
     */
    private KDNode buildTree(List<EncryptedPoint> points, int depth) {
        if (points.isEmpty()) return null;
        
        int currentDimension = depth % k;
        
        // 按当前维度的加密值排序
        points.sort(Comparator.comparing(p -> p.getEncryptedValue(currentDimension)));
        
        int medianIndex = points.size() / 2;
        EncryptedPoint medianPoint = points.get(medianIndex);
        
        // 创建当前节点，使用加密值
        KDNode node = new KDNode(
            medianPoint.getOriginalPoint(),
            currentDimension,
            medianPoint.getEncryptedValue(currentDimension).intValue()
        );
        
        // 递归构建左右子树
        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);
        
        return node;
    }

    @Override
    public void insert(Point point) {
        // 加密新点
        EncryptedPoint encryptedPoint = encryptSinglePoint(point);
        
        // 插入到树中
        this.root = insertRec(root, encryptedPoint, 0);
    }

    /**
     * 递归插入节点（使用加密值比较）
     */
    private KDNode insertRec(KDNode node, EncryptedPoint encryptedPoint, int depth) {
        if (node == null) {
            int splitDimension = depth % k;
            return new KDNode(
                encryptedPoint.getOriginalPoint(),
                splitDimension,
                encryptedPoint.getEncryptedValue(splitDimension).intValueExact()
            );
        }
        
        int currentDimension = node.splitDimension;
        BigDecimal encryptedValue = encryptedPoint.getEncryptedValue(currentDimension);
        BigDecimal nodeEncryptedValue = getNodeEncryptedValue(node, currentDimension);
        
        if (encryptedValue.compareTo(nodeEncryptedValue) < 0) {
            node.left = insertRec(node.left, encryptedPoint, depth + 1);
        } else {
            node.right = insertRec(node.right, encryptedPoint, depth + 1);
        }
        
        return node;
    }
    
    /**
     * 获取节点在指定维度的加密值
     */
    private BigDecimal getNodeEncryptedValue(KDNode node, int dimension) {
        return ope.encrypt(Collections.singletonList(
            BigDecimal.valueOf(node.point.getCoordinate(dimension)))
        ).get(0).getEncryptedValue();
    }

    @Override
    public Point nearestNeighbor(Point target) {
        // 加密目标点
        EncryptedPoint encryptedTarget = encryptSinglePoint(target);
        
        // 初始化最近邻和最小距离
        KDNode[] nearest = {null};
        BigDecimal[] minDistance = {BigDecimal.valueOf(Double.MAX_VALUE)};
        
        // 递归搜索最近邻
        nearestNeighborRec(root, encryptedTarget, nearest, minDistance, 0);
        
        return nearest[0] != null ? nearest[0].point : null;
    }
    
    /**
     * 递归搜索最近邻（使用加密值计算距离）
     */
    private void nearestNeighborRec(KDNode node, EncryptedPoint target, 
                                    KDNode[] nearest, BigDecimal[] minDistance, int depth) {
        if (node == null) return;
        
        int currentDimension = depth % k;
        BigDecimal targetEncryptedValue = target.getEncryptedValue(currentDimension);
        BigDecimal nodeEncryptedValue = getNodeEncryptedValue(node, currentDimension);
        
        // 计算当前节点与目标点的距离（使用加密值）
        BigDecimal distance = calculateDistanceEncrypted(node, target);
        
        // 更新最近邻
        if (distance.compareTo(minDistance[0]) < 0) {
            minDistance[0] = distance;
            nearest[0] = node;
        }
        
        // 决定搜索顺序
        KDNode nextBranch = null;
        KDNode oppositeBranch = null;
        
        if (targetEncryptedValue.compareTo(nodeEncryptedValue) < 0) {
            nextBranch = node.left;
            oppositeBranch = node.right;
        } else {
            nextBranch = node.right;
            oppositeBranch = node.left;
        }
        
        // 递归搜索更近的分支
        nearestNeighborRec(nextBranch, target, nearest, minDistance, depth + 1);
        
        // 检查是否需要搜索另一个分支
        BigDecimal splitDistance = targetEncryptedValue.subtract(nodeEncryptedValue).abs();
        if (splitDistance.compareTo(minDistance[0]) <= 0) {
            nearestNeighborRec(oppositeBranch, target, nearest, minDistance, depth + 1);
        }
    }
    
    /**
     * 计算给定点与加密目标点之间的距离（使用加密值）
     */
    private BigDecimal calculateDistanceEncrypted(KDNode node, EncryptedPoint target) {
        BigDecimal sum = BigDecimal.ZERO;
        
        for (int i = 0; i < k; i++) {
            BigDecimal nodeValue = getNodeEncryptedValue(node, i);
            BigDecimal targetValue = target.getEncryptedValue(i);
            BigDecimal diff = nodeValue.subtract(targetValue);
            sum = sum.add(diff.multiply(diff));
        }
        
        return sum;
    }

    @Override
    public List<Point> rangeSearch(Point lowerBound, Point upperBound) {
        // 加密查询范围
        EncryptedPoint encryptedLower = encryptSinglePoint(lowerBound);
        EncryptedPoint encryptedUpper = encryptSinglePoint(upperBound);
        
        List<Point> res = new ArrayList<>();
        rangeSearchRec(root, encryptedLower, encryptedUpper, res, 0);
        return res;
    }

    /**
     * 递归搜索范围内的点（使用加密值比较）
     */
    private void rangeSearchRec(KDNode node, EncryptedPoint lowerBound, 
                               EncryptedPoint upperBound, List<Point> result, int depth) {
        if (node == null) return;
        
        int currentDimension = depth % k;
        
        // 检查当前节点是否在范围内
        boolean inRange = true;
        
        for (int i = 0; i < k; i++) {
            BigDecimal nodeValue = getNodeEncryptedValue(node, i);
            BigDecimal lower = lowerBound.getEncryptedValue(i);
            BigDecimal upper = upperBound.getEncryptedValue(i);
            
            if (nodeValue.compareTo(lower) < 0 || nodeValue.compareTo(upper) > 0) {
                inRange = false;
                break;
            }
        }
        
        if (inRange) result.add(node.point);
        
        // 决定搜索哪些子树
        BigDecimal nodeEncryptedValue = getNodeEncryptedValue(node, currentDimension);
        BigDecimal lowerBoundValue = lowerBound.getEncryptedValue(currentDimension);
        BigDecimal upperBoundValue = upperBound.getEncryptedValue(currentDimension);
        
        // 递归搜索左子树
        if (lowerBoundValue.compareTo(nodeEncryptedValue) <= 0) {
            rangeSearchRec(node.left, lowerBound, upperBound, result, depth + 1);
        }
        
        // 递归搜索右子树
        if (upperBoundValue.compareTo(nodeEncryptedValue) >= 0) {
            rangeSearchRec(node.right, lowerBound, upperBound, result, depth + 1);
        }
    }
    
    /**
     * 加密单个点
     */
    private EncryptedPoint encryptSinglePoint(Point point) {
        BigDecimal[] encryptedValues = new BigDecimal[k];
        
        for (int dim = 0; dim < k; dim++) {
            List<BigDecimal> singleValueList = Collections.singletonList(
                BigDecimal.valueOf(point.getCoordinate(dim))
            );
            List<OPE.EncryptedData> encrypted = ope.encrypt(singleValueList);
            encryptedValues[dim] = encrypted.get(0).getEncryptedValue();
        }
        
        return new EncryptedPoint(point, encryptedValues);
    }

    public static void main(String[] args) {
        final Integer DIMENSION = 2;
        // 从NE数据集读取点集
        List<Point> points = new ArrayList<>();
        Scanner in = new Scanner(Objects.requireNonNull(KDTreeImpl.class.getClassLoader().getResourceAsStream("NE.txt")));
        while(in.hasNextDouble()) {
            double[] coordinates = new double[DIMENSION];
            for (int i=0;i<DIMENSION;i++) {
                coordinates[i] = in.nextDouble();
            }
            points.add(new Point(coordinates));
        }
        
        // 通过点集构建二维数据下的KDTree（使用OPE加密）
        long startTime = System.currentTimeMillis();
        KDTree kdTree = new KDTreeImpl(DIMENSION, points);
        long buildTime = System.currentTimeMillis() - startTime;
        System.out.println("KD树构建时间: " + buildTime + "ms");
        
        // 通过KDTree实现范围查询（加密空间）
        startTime = System.currentTimeMillis();
        List<Point> points1 = kdTree.rangeSearch(
            new Point(new double[]{0.1, 0.2}), 
            new Point(new double[]{0.2, 0.3})
        );
        long rangeTime = System.currentTimeMillis() - startTime;
        System.out.println("范围查询结果: " + points1.size() + "个点，耗时: " + rangeTime + "ms");
        
        // 通过KDTree实现最近邻查询（加密空间）
        startTime = System.currentTimeMillis();
        Point nearest = kdTree.nearestNeighbor(new Point(new double[]{0.433, 0.511}));
        long nnTime = System.currentTimeMillis() - startTime;
        System.out.println("最近邻查询结果: " + nearest + "，耗时: " + nnTime + "ms");
    }
}