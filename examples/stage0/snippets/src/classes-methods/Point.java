/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [pointFull]
class Point {
    // [finalFields]
    // Each Point has its own x and y coordinates, and they never change.
    private final double x;
    private final double y;
    // [/finalFields]
    // [staticConstant]
    // Shared by every Point, instead of belonging to just one instance.
    public static final Point ORIGIN = new Point(0, 0);
    // [/staticConstant]

    // [constructor]
    // Sets this Point's coordinates to the given x and y values.
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    // [/constructor]

    // [getters]
    // Let other classes read the private x and y fields.
    public double getX() { return this.x; }
    public double getY() { return this.y; }
    // [/getters]

    // [plus]
    // Adds this Point's coordinates to another Point's coordinates.
    public Point plus(Point other) {
        return new Point(this.x + other.x, this.y + other.y);
    }
    // [/plus]

    // [minus]
    // Subtracts another Point's coordinates from this Point's coordinates.
    public Point minus(Point other) {
        return new Point(this.x - other.x, this.y - other.y);
    }
    // [/minus]

    // [norm]
    // Computes this Point's distance from the origin.
    public double norm() {
        double sumOfSquares = this.x * this.x + this.y * this.y;
        return Math.sqrt(sumOfSquares);
    }
    // [/norm]
}
// [/pointFull]
