/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [lidarSensorClass]
class LidarSensor implements DistanceSensor {
    @Override
    public double getDistanceMeters() {
        // In real life, this would actually interact with hardware
        return 1.2;
    }
}
// [/lidarSensorClass]
