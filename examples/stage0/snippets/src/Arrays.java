/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

void main() {
    // [motorSpeedsLiteral]
    double[] motorSpeeds = {0.5, 0.5, 0.5, 0.5};
    // [/motorSpeedsLiteral]

    // [motorSpeedsEmpty]
    double[] emptyMotorSpeeds = new double[4];
    // [/motorSpeedsEmpty]

    // [setSpeed]
    motorSpeeds[0] = 0.7;
    System.out.println(motorSpeeds[0]); // 0.7
    // [/setSpeed]

    // [speedsLength]
    System.out.println(motorSpeeds.length); // 4
    // [/speedsLength]

    // [pathArray]
    Point[] path = {
        new Point(0, 0),
        new Point(1, 2),
        new Point(3, 3),
    };
    // [/pathArray]

    // [pathArrayEmpty]
    Point[] emptyPath = new Point[3];
    // [/pathArrayEmpty]

    try {
        // [pathArrayNull]
        emptyPath[0].norm(); // error: emptyPath[0] is null
        // [/pathArrayNull]
    } catch (NullPointerException e) {
        // for demo purposes we don't care about the exception'
    }

    // [indexLoopSpeeds]
    double total = 0;
    for (int i = 0; i < motorSpeeds.length; i++) {
        total += motorSpeeds[i];
    }
    System.out.println(total); // 2.2
    // [/indexLoopSpeeds]

    // [forEachPath]
    for (Point waypoint : path) {
        System.out.println(waypoint.getX() + ", " + waypoint.getY());
    }
    // [/forEachPath]

    // [pathLength]
    double pathLength = 0;
    for (int i = 1; i < path.length; i++) {
        Point segment = path[i].minus(path[i - 1]);
        pathLength += segment.norm();
    }
    System.out.println(pathLength); // 4.47213595499958
    // [/pathLength]
}
