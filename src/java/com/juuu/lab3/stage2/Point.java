package com.juuu.lab3.stage2;

/**
 * 点类
 */
public class Point implements Cloneable {
    private double[] coordinates;

    public Point(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double getCoordinate(int index) {
        if (index < 0 || index >= coordinates.length) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return coordinates[index];
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public int getDimension() {
        return coordinates.length;
    }

    public Point clone() {
        return new Point(this.coordinates);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < coordinates.length; i++) {
            sb.append(coordinates[i]);
            if (i != coordinates.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
