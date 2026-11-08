/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// Complete the class definition below. First, add two private final fields:
// `x` and `y`, both of type `double`. Then, define a public constructor
// `Point(double x, double y)`, and getter methods `getX()` and `getY()`.
// Lastly, below the definitions of the `x` and `y` fields, define a static final
// field `ORIGIN`, of type `Point`, and assign to it a new `Point` instance
// with x = 0 and y = 0.
static class Point {
    private final double x;
    private final double y;

    private static final Point ORIGIN = new Point(0, 0);

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
}

void main() {
    // Create two `Point` objects: one representing the point (2, 5) and stored
    // in the variable `pointOne`, and the other representing the point (3, 0)
    // and stored in the variable `pointTwo`.
    Point pointOne = new Point(2, 5);
    Point pointTwo = new Point(3, 0);

    // On two separate lines, print the x-coordinate of pointOne,
    // and the y-coordinate of pointTwo, using the `Point` class's getter functions.
    System.out.println(pointOne.getX());
    System.out.println(pointTwo.getY());

    // Using the same getter functions and conditional statements,
    // print "Point one is farther right!" if `pointOne`'s x-coordinate is larger,
    // and "Point two is farther right!" if `pointTwo`'s x-coordinate is larger.
    // After the code runs, change `pointOne`'s x-coordinate to -4, and
    // `pointTwo`'s x-coordinate to -5; the code should now print
    // "Point one is farther right!"
    if (pointOne.getX() > pointTwo.getX()) {
        System.out.println("Point one is farther right!");
    } else if (pointOne.getX() < pointTwo.getX()) {
        System.out.println("Point two is farther right!");
    }

    // Using the same getter functions, use conditional statements
    // to handle the following six cases:
    // 1. Print "p1 x-axis" if `pointOne`'s y-coordinate is 0.
    // 2. Print "p1 y-axis" if `pointOne`'s x-coordinate is 0.
    // 3. Print "p1 origin" if `pointOne` is at the origin.
    // 4. Print "p2 x-axis" if `pointTwo`'s y-coordinate is 0.
    // 5. Print "p2 y-axis" if `pointTwo`'s x-coordinate is 0.
    // 6. Print "p2 origin" if `pointTwo` is at the origin.
    // After the code runs, change `pointOne` to (0, 0) and `pointTwo` to (0, 3).
    // You should now see "p1 x-axis", "p1 y-axis", "p1 origin", and "p2 y-axis".
    if (pointOne.getY() == 0) {
        System.out.println("p1 x-axis");
    }
    if (pointOne.getX() == 0) {
        System.out.println("p1 y-axis");
    }
    if (
        (pointOne.getX() == Point.ORIGIN.getX())
        && (pointOne.getY() == Point.ORIGIN.getY())
    ) {
        System.out.println("p1 origin");
    }
    if (pointTwo.getY() == 0) {
        System.out.println("p2 x-axis");
    }
    if (pointTwo.getX() == 0) {
        System.out.println("p2 y-axis");
    }
    if (
        (pointTwo.getX() == Point.ORIGIN.getX())
        && (pointTwo.getY() == Point.ORIGIN.getY())
    ) {
        System.out.println("p2 origin");
    }

    // Lastly, print "p1 equals p2!" if `pointOne` and `pointTwo`'s x- and y-
    // coordinates are equal, and "p1 does not equal p2" otherwise. After
    // the code runs, change both `pointOne` and `pointTwo` to (5, 8);
    // you should now see "p1 equals p2!".
    // HINT: You might find it convenient to assign the result of the equality
    // check to a variable first, such as `pointsEqual`, then check the boolean
    // value of that in conditional statements.
    boolean pointsEqual = (pointOne.getX() == pointTwo.getX())
        && (pointOne.getY() == pointTwo.getY());
    if (pointsEqual) {
        System.out.println("p1 equals p2!");
    } else {
        System.out.println("p1 does not equal p2");
    }
}
