/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [ultrasonicSensorClass]
class UltrasonicSensor implements DistanceSensor {
    @Override
    public double getDistanceMeters() {
        // In real life, this would actually interact with hardware
        return 1.5;
    }
}
// [/ultrasonicSensorClass]
