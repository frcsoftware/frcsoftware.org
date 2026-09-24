/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

void main() {
    // [createAndUse]
    // [createPoint]
    Point a = new Point(3, 4);
    Point b = new Point(1, 2);

    System.out.println(a.getX()); // 3.0
    System.out.println(b.getY()); // 2.0
    // [/createPoint]

    Point sum = a.plus(b);
    System.out.println(sum.getX()); // 4.0
    System.out.println(sum.getY()); // 6.0
    System.out.println(a.norm());   // 5.0
    // [/createAndUse]

    // [accessStaticConstant]
    System.out.println(Point.ORIGIN.getX()); // 0.0
    // [/accessStaticConstant]

    // [trackerUsage]
    RobotTracker tracker = new RobotTracker(Point.ORIGIN);
    tracker.move(new Point(3, 0));
    tracker.move(new Point(0, 4));
    System.out.println(tracker.distanceTo(Point.ORIGIN)); // 5.0
    tracker.reset();
    System.out.println(tracker.getPosition().getX()); // 0.0
    // [/trackerUsage]
}
