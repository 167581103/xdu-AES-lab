package com.juuu.lab3.stage2;


/**
 * KD树节点类
 */
public class KDNode {
    public Point point;
    public KDNode left, right;
    public int splitDimension;

    public KDNode(Point point, int splitDimension) {
        this.point = point;
        this.splitDimension = splitDimension;
        this.left = null;
        this.right = null;
    }
}
