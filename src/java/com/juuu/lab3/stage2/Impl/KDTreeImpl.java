package com.juuu.lab3.stage2.Impl;

import com.juuu.lab3.stage2.KDNode;
import com.juuu.lab3.stage2.KDTree;
import com.juuu.lab3.stage2.Point;
import com.juuu.lab3.stage2.util.EncryptUtil;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于AES实现的加密KD树
 */
public class KDTreeImpl implements KDTree {
    private KDNode root;
    private final int k;  // 数据维度
    private final SecretKey secretKey;

    public KDTreeImpl(Integer dimension, List<Point> points, SecretKey secretKey) {
        // 通过点集构建KD树
        this.k = dimension;
        this.secretKey = secretKey;
        // 预处理，将点集转换为加密值
        points.stream().map(point -> EncryptUtil.encrypt(point, secretKey)).collect(Collectors.toList()).forEach(this::insert);
    }

    @Override
    public void insert(String pointStr) {
        // 从根结点插入
        this.root = insertRec(root, pointStr, 0);
    }

    /**
     * 递归地根据当前分割维度比较数据点，决定插入到左子树或右子树：
     * @param node
     * @param pointStr
     * @param depth
     * @return
     */
    private KDNode insertRec(KDNode node, String pointStr, int depth) {
        if (node == null) return new KDNode(pointStr, depth % k, secretKey);

        Point point = EncryptUtil.fromEncryptedString(pointStr, secretKey);
        int currentDimension = node.splitDimension;
        if (point.getCoordinate(currentDimension) < node.point.getCoordinate(currentDimension)) {
            // 要插入的点在分割维度上小于树上当前点，应该往左走
            node.left = insertRec(node.left, pointStr ,depth + 1);
        } else {
            // 要插入的点在分割维度上大于树上当前点，应该往右走
            node.right = insertRec(node.right, pointStr, depth + 1);
        }
        return node;
    }

    @Override
    public Point nearestNeighbor(Point target) {
        return null;
    }


    @Override
    public List<String> rangeSearch(Point lowerBound, Point upperBound) {
        List<String> res = new ArrayList<>();
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
    private void rangeSearchRec(KDNode node, Point lowerBound, Point upperBound, List<String> result) {
        if (node == null) return;

        // 检查node是否在范围内
        boolean inRange = true;
        for (int i = 0; i < k; i++) {
            if (node.point.getCoordinate(i) < lowerBound.getCoordinate(i) || node.point.getCoordinate(i) > upperBound.getCoordinate(i)) {
                inRange = false;
                break;
            }
        }
        if (inRange) result.add(node.pointStr);

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
}