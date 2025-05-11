package com.juuu.lab3.stage3.impl;

import com.juuu.lab3.stage3.KDNode;
import com.juuu.lab3.stage3.KDTree;
import com.juuu.lab3.stage3.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * 支持数值化OPE编码的KD树实现
 */
public class KDTreeImpl implements KDTree {
    private KDNode root;
    private final int k;  // 数据维度
    private static final int INITIAL_STEP = 1 << 30;  // 初始步长（2^30，保证足够编码空间）

    public KDTreeImpl(Integer dimension, List<Point> points) {
        this.k = dimension;
        points.forEach(this::insert);
    }

    @Override
    public void insert(Point point) {
        // 根节点初始编码为INITIAL_STEP，初始步长为INITIAL_STEP
        this.root = insertRec(root, point, 0, INITIAL_STEP, INITIAL_STEP);
    }

    /**
     * 递归插入并生成OPE编码
     * @param node 当前节点
     * @param point 待插入点
     * @param depth 当前深度（用于计算分割维度）
     * @param parentCode 父节点编码
     * @param step 当前层级步长（随深度指数级递减）
     * @return 插入后的子树根节点
     */
    private KDNode insertRec(KDNode node, Point point, int depth, int parentCode, int step) {
        if (node == null) {
            // 新节点编码：根节点用INITIAL_STEP，其他节点由父节点传递
            return new KDNode(point, depth % k, parentCode);
        }

        int currentDimension = node.splitDimension;
        double currentCoord = node.point.getCoordinate(currentDimension);
        double newCoord = point.getCoordinate(currentDimension);

        int nextStep = step >> 1;  // 步长减半（指数级递减）
        if (nextStep < 1) nextStep = 1;  // 防止步长过小

        if (newCoord < currentCoord) {
            // 左子节点编码 = 父节点编码 - 步长
            int leftCode = parentCode - nextStep;
            node.left = insertRec(node.left, point, depth + 1, leftCode, nextStep);
            if (node.left.opeCode >= node.opeCode) {
                throw new IllegalStateException("左子树编码不满足小于父节点规则");
            }
        } else {
            // 右子节点编码 = 父节点编码 + 步长
            int rightCode = parentCode + nextStep;
            node.right = insertRec(node.right, point, depth + 1, rightCode, nextStep);
            if (node.right.opeCode <= node.opeCode) {
                throw new IllegalStateException("右子树编码不满足大于父节点规则");
            }
        }
        return node;
    }

    @Override
    public Point nearestNeighbor(Point target) {
        KDNode closest = root;
        KDNode current = root;

        while (current != null) {
            // 更新最近节点
            if (distance(target, current.point) < distance(target, closest.point)) {
                closest = current;
            }

            // 根据当前维度的坐标值决定搜索方向
            int dim = current.splitDimension;
            if (target.getCoordinate(dim) < current.point.getCoordinate(dim)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return closest.point;
    }

    @Override
    public List<Point> rangeSearch(Point lowerBound, Point upperBound) {
        List<Point> result = new ArrayList<>();
        rangeSearchRec(root, lowerBound, upperBound, result);
        return result;
    }

    // 辅助方法：递归范围查询
    private void rangeSearchRec(KDNode node, Point lower, Point upper, List<Point> result) {
        if (node == null) return;

        // 检查当前节点是否在空间范围内
        if (isInRange(node.point, lower, upper)) {
            result.add(node.point);
        }

        // 根据编码有序性剪枝（左子树编码更小，右子树更大）
        int dim = node.splitDimension;
        if (lower.getCoordinate(dim) < node.point.getCoordinate(dim)) {
            rangeSearchRec(node.left, lower, upper, result);
        }
        if (upper.getCoordinate(dim) > node.point.getCoordinate(dim)) {
            rangeSearchRec(node.right, lower, upper, result);
        }
    }

    // 辅助方法：计算欧氏距离
    private double distance(Point a, Point b) {
        double sum = 0;
        for (int i = 0; i < k; i++) {
            double diff = a.getCoordinate(i) - b.getCoordinate(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    // 辅助方法：判断点是否在范围内
    private boolean isInRange(Point p, Point lower, Point upper) {
        for (int i = 0; i < k; i++) {
            double coord = p.getCoordinate(i);
            if (coord < lower.getCoordinate(i) || coord > upper.getCoordinate(i)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        final Integer DIMENSION = 2;
        List<Point> points = new ArrayList<>();
        Scanner in = new Scanner(Objects.requireNonNull(KDTreeImpl.class.getClassLoader().getResourceAsStream("NE.txt")));

        while(in.hasNextDouble()) {
            double[] coordinates = new double[DIMENSION];
            for (int i=0; i<DIMENSION; i++) {
                coordinates[i] = in.nextDouble();
            }
            points.add(new Point(coordinates));
        }

        KDTree kdTree = new KDTreeImpl(DIMENSION, points);

        // 测试范围查询（示例范围）
        List<Point> rangeResult = kdTree.rangeSearch(
            new Point(new double[]{0.1, 0.2}),
            new Point(new double[]{0.2, 0.3})
        );
        System.out.println("范围查询结果数量：" + rangeResult.size());

        // 测试最近邻查询（示例目标点）
        Point target = new Point(new double[]{0.434, 0.512});
        Point nearest = kdTree.nearestNeighbor(target);
        System.out.println("最近邻点：" + nearest);
    }
}