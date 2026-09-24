/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [importList]
import java.util.ArrayList;
import java.util.List;
// [/importList]

// [isTooClose]
boolean isTooClose(DistanceSensor sensor) {
    return sensor.getDistanceMeters() < 1.0;
}
// [/isTooClose]

// [genericLast]
<T> T last(T[] items) {
    return items[items.length - 1];
}
// [/genericLast]

void main() {
    // [distanceSensorCall]
    DistanceSensor ultrasonic = new UltrasonicSensor();
    DistanceSensor lidar = new LidarSensor();
    System.out.println(isTooClose(ultrasonic)); // false
    System.out.println(isTooClose(lidar));      // false
    // [/distanceSensorCall]

    // [genericLastCall]
    Point[] path = {new Point(0, 0), new Point(1, 2), new Point(3, 3)};
    DistanceSensor[] sensors = {ultrasonic, lidar};

    System.out.println(last(path).getX());       // 3.0
    System.out.println(last(sensors).getClass()); // class LidarSensor
    // [/genericLastCall]

    // [pairCall]
    Pair<String, Integer> pair = new Pair<>("foo", 42);
    String foo = pair.getFirst();
    Pair<Point, Point> pointPair = new Pair<>(new Point(0, 0), new Point(1, 2));
    Point secondPoint = pointPair.getSecond();
    // [/pairCall]

    // [historyList]
    List<Point> waypoints = new ArrayList<>();
    // [/historyList]

    // [historyAdd]
    waypoints.add(new Point(0, 0));
    waypoints.add(new Point(1, 2));
    System.out.println(waypoints.size()); // 2
    // [/historyAdd]

    // [forEachHistory]
    RobotHistoryTracker tracker = new RobotHistoryTracker(Point.ORIGIN);
    tracker.move(new Point(3, 0));
    tracker.move(new Point(0, 4));

    for (Point visited : tracker.getHistory()) {
        System.out.println(visited.getX() + ", " + visited.getY());
    }
    // [/forEachHistory]
}
