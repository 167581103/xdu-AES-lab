package com.juuu.lab3.stage3;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * KD树实现，支持二维加密数据的近邻查询
 */
public class KDTree {
    private static final int DIMENSIONS = 2;
    private final OrderPreservingEncryption ope;
    private Node root;

    public KDTree(OrderPreservingEncryption ope) {
        this.ope = ope;
    }

    /**
     * 二维数据点类
     */
    public static class Point {
        private final BigInteger[] coordinates;
        private final String originalData;

        public Point(BigInteger x, BigInteger y, String originalData) {
            this.coordinates = new BigInteger[DIMENSIONS];
            this.coordinates[0] = x;
            this.coordinates[1] = y;
            this.originalData = originalData;
        }

        public BigInteger getCoordinate(int dimension) {
            return coordinates[dimension];
        }

        public String getOriginalData() {
            return originalData;
        }

        @Override
        public String toString() {
            return "(" + coordinates[0] + ", " + coordinates[1] + ")";
        }
    }

    private static class Node {
        private final Point point;
        private Node left;
        private Node right;

        public Node(Point point) {
            this.point = point;
        }
    }

    /**
     * 插入数据点
     */
    public void insert(Point point) {
        root = insertRec(root, point, 0);
    }

    private Node insertRec(Node node, Point point, int depth) {
        if (node == null) {
            return new Node(point);
        }

        int currentDimension = depth % DIMENSIONS;
        if (point.getCoordinate(currentDimension).compareTo(
                node.point.getCoordinate(currentDimension)) < 0) {
            node.left = insertRec(node.left, point, depth + 1);
        } else {
            node.right = insertRec(node.right, point, depth + 1);
        }

        return node;
    }

    /**
     * 安全近邻查询
     */
    public List<Point> kNearestNeighbors(Point queryPoint, int k) {
        PriorityQueue<NodeDistance> maxHeap = new PriorityQueue<>(
                k, Comparator.comparingDouble(o -> -o.distance));
        kNearestNeighborsRec(root, queryPoint, k, 0, maxHeap);

        List<Point> result = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            result.add(0, maxHeap.poll().node.point);
        }
        return result;
    }

    private void kNearestNeighborsRec(Node node, Point queryPoint, int k, int depth,
                                       PriorityQueue<NodeDistance> maxHeap) {
        if (node == null) {
            return;
        }

        // 计算当前点到查询点的欧氏距离
        double distance = calculateDistance(node.point, queryPoint);

        // 如果堆未满或者当前距离比堆中最大距离小，则加入堆
        if (maxHeap.size() < k || distance < maxHeap.peek().distance) {
            maxHeap.offer(new NodeDistance(node, distance));
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }

        int currentDimension = depth % DIMENSIONS;
        Node nextBranch;
        Node oppositeBranch;

        // 决定先访问哪个子树
        if (queryPoint.getCoordinate(currentDimension).compareTo(
                node.point.getCoordinate(currentDimension)) < 0) {
            nextBranch = node.left;
            oppositeBranch = node.right;
        } else {
            nextBranch = node.right;
            oppositeBranch = node.left;
        }

        // 递归搜索更可能包含近邻的子树
        kNearestNeighborsRec(nextBranch, queryPoint, k, depth + 1, maxHeap);

        // 检查对面的子树是否有更近的点
        if (maxHeap.size() < k || Math.abs(
                queryPoint.getCoordinate(currentDimension).subtract(
                        node.point.getCoordinate(currentDimension)).doubleValue()) < maxHeap.peek().distance) {
            kNearestNeighborsRec(oppositeBranch, queryPoint, k, depth + 1, maxHeap);
        }
    }

    private double calculateDistance(Point p1, Point p2) {
        BigInteger dx = p1.getCoordinate(0).subtract(p2.getCoordinate(0));
        BigInteger dy = p1.getCoordinate(1).subtract(p2.getCoordinate(1));
        double dxSquared = dx.pow(2).doubleValue();
        double dySquared = dy.pow(2).doubleValue();
        return Math.sqrt(dxSquared + dySquared);
    }

    private static class NodeDistance {
        private final Node node;
        private final double distance;

        public NodeDistance(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}    