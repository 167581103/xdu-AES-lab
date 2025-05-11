package com.juuu.lab3.stage2;

import com.juuu.lab3.stage2.util.EncryptUtil;

import javax.crypto.SecretKey;

/**
 * KD树节点类
 */
public class KDNode {
    public Point point;
    public String pointStr;
    public KDNode left, right;
    public int splitDimension;

    public KDNode(String pointStr, int splitDimension, SecretKey secretKey) {
        this.pointStr = pointStr;
        this.point = EncryptUtil.fromEncryptedString(pointStr, secretKey);
        this.splitDimension = splitDimension;
        this.left = null;
        this.right = null;
    }
}
