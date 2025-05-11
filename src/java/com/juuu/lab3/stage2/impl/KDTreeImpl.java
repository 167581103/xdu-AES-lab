package com.juuu.lab3.stage2.impl;

import com.juuu.lab3.stage1.OPE;
import com.juuu.lab3.stage2.KDNode;
import com.juuu.lab3.stage2.KDTree;
import com.juuu.lab3.stage2.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * KD树
 */
public class KDTreeImpl implements KDTree {
    private KDNode root;
    private final int k;  // 数据维度

    public KDTreeImpl(Integer dimension, List<Point> points) {
        // 通过点集构建KD树
        this.k = dimension;
        points.stream().forEach(this::insert);
    }

    @Override
    public void insert(Point point) {
        // 从根结点插入
        this.root = insertRec(root, point, 0);
    }

    /**
     * 递归地根据当前分割维度比较数据点，决定插入到左子树或右子树：
     * @param node
     * @param point
     * @param depth
     * @return
     */
    private KDNode insertRec(KDNode node, Point point, int depth) {
        if (node == null) return new KDNode(point, depth % k);
        int currentDimension = node.splitDimension;
        if (point.getCoordinate(currentDimension) < node.point.getCoordinate(currentDimension)) {
            // 要插入的点在分割维度上小于树上当前点，应该往左走
            node.left = insertRec(node.left, point ,depth + 1);
        } else {
            // 要插入的点在分割维度上大于树上当前点，应该往右走
            node.right = insertRec(node.right, point, depth + 1);
        }
        return node;
    }

    @Override
    public Point nearestNeighbor(Point target) {
        return null;
    }


    @Override
    public List<Point> rangeSearch(Point lowerBound, Point upperBound) {
        List<Point> res = new ArrayList<>();
        rangeSearchRec(root, lowerBound, upperBound, res);
        return res;
    }

    /**
     * 递归搜索左右子树，实现点的范围查询
     * @param node
     * @param lowerBound
     * @param upperBound
     * @param result
     */
    private void rangeSearchRec(KDNode node, Point lowerBound, Point upperBound, List<Point> result) {
        if (node == null) return;

        // 检查node是否在范围内
        boolean inRange = true;
        for (int i = 0; i < k; i++) {
            if (node.point.getCoordinate(i) < lowerBound.getCoordinate(i) || node.point.getCoordinate(i) > upperBound.getCoordinate(i)) {
                inRange = false;
                break;
            }
        }
        if (inRange) result.add(node.point);

        int currentDimension = node.splitDimension;
        // 递归搜索左子树（如果可能包含结果）
        if (lowerBound.getCoordinate(currentDimension) <= node.point.getCoordinate(currentDimension)) {
            rangeSearchRec(node.left, lowerBound, upperBound, result);
        }
        // 递归搜索右子树（如果可能包含结果）
        if (upperBound.getCoordinate(currentDimension) >= node.point.getCoordinate(currentDimension)) {
            rangeSearchRec(node.right, lowerBound, upperBound, result);
        }
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
        // TODO 对点集进行预处理，将点集通过OPE映射为保序性密文，后续基于保序性密文计算
        // 通过点集构建二维数据下的KDTree
        KDTree kdTree = new KDTreeImpl(DIMENSION, points);
        // 通过KDTree实现范围查询
        List<Point> points1 = kdTree.rangeSearch(new Point(new double[]{0.1, 0.2}), new Point(new double[]{0.2, 0.3}));
        System.out.println(points1);

        // 通过KDTree实现最近邻查询
    }
}