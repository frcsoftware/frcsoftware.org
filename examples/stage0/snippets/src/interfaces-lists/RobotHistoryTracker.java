/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import java.util.ArrayList;
import java.util.List;

// [robotHistoryTrackerClass]
class RobotHistoryTracker {
    private Point position;
    private final List<Point> history = new ArrayList<>();

    public RobotHistoryTracker(Point startPosition) {
        this.position = startPosition;
        this.history.add(startPosition);
    }

    public void move(Point delta) {
        this.position = this.position.plus(delta);
        this.history.add(this.position);
    }

    public Point getPosition() {
        return this.position;
    }

    public List<Point> getHistory() {
        return this.history;
    }
}
// [/robotHistoryTrackerClass]
