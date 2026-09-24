/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [robotTrackerFull]
class RobotTracker {
    // [mutableField]
    // Not final, since the robot's position changes as it moves.
    private Point position;
    // [/mutableField]

    // [robotTrackerConstructor]
    // Starts tracking from a given position.
    public RobotTracker(Point startPosition) {
        this.position = startPosition;
    }
    // [/robotTrackerConstructor]

    // [constructorChaining]
    // Starts tracking from the origin by default.
    public RobotTracker() {
        this(Point.ORIGIN);
    }
    // [/constructorChaining]

    // [stateMutation]
    // Moves the tracked position by delta.
    public void move(Point delta) {
        this.position = this.position.plus(delta);
    }
    // [/stateMutation]

    // [localVariableChain]
    // Finds the straight-line distance from the current position to target.
    public double distanceTo(Point target) {
        Point diff = target.minus(this.position);
        return diff.norm();
    }
    // [/localVariableChain]

    // [getPosition]
    // Lets other classes read the current position.
    public Point getPosition() {
        return this.position;
    }
    // [/getPosition]

    // [useStaticConstant]
    // Moves the tracked position back to the origin.
    public void reset() {
        this.position = Point.ORIGIN;
    }
    // [/useStaticConstant]
}
// [/robotTrackerFull]
