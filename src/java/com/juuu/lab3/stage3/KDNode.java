package com.juuu.lab3.stage3;

/**
 * KD树节点类（带数值化OPE编码）
 */
public class KDNode {
    public Point point;
    public KDNode left, right;
    public int splitDimension;
    public int opeCode;  // 数值化编码（如示例中的1/2/3）

    public KDNode(Point point, int splitDimension, int opeCode) {
        this.point = point;
        this.splitDimension = splitDimension;
        this.opeCode = opeCode;
        this.left = null;
        this.right = null;
    }
}