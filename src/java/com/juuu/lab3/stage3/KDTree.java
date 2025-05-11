package com.juuu.lab3.stage3;

import java.util.List;

public interface KDTree {
    // 插入一个点到kd树中
    void insert(Point point);

    // 搜索最近邻点
    Point nearestNeighbor(Point target);

    // 范围搜索，返回在指定范围内的点
    List<Point> rangeSearch(Point lowerBound, Point upperBound);
}

