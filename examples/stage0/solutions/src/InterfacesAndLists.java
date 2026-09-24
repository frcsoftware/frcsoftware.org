/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import java.util.ArrayList;
import java.util.List;

// Define an interface named `IntakeSensor` with a single method:
// `double distanceMillimeters();`
interface IntakeSensor {
    double distanceMillimeters();
}

// Create a `BeamBreak` class that implements `IntakeSensor`.
// The method `distanceMillimeters()` should return `3.0`.
class BeamBreak implements IntakeSensor {
    @Override
    public double distanceMillimeters() {
        return 3.0;
    }
}

// Create a `LaserCAN` class that implements `IntakeSensor`.
// The method `distanceMillimeters()` should return `5.0`.
class LaserCAN implements IntakeSensor {
    @Override
    public double distanceMillimeters() {
        return 5.0;
    }
}

// Define a generic class named `Pair<A, B>` with two private final fields:
// `first` of type `A`, and `second` of type `B`.
// Provide a constructor `Pair(A first, B second)` and getter methods
// `getFirst()` and `getSecond()`.
class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}

void main() {
    // Create a variable named `beamBreak` with type `IntakeSensor`, and assign it a new instance of BeamBreak.
    // Create a variable named `currentSensor` of type `IntakeSensor`, and assign it a new instance of CurrentSensor.
    // Print the result of calling `hasGamePiece()` on both sensors.
    IntakeSensor beamBreak = new BeamBreak();
    IntakeSensor currentSensor = new LaserCAN();
    System.out.println(beamBreak.distanceMillimeters());
    System.out.println(currentSensor.distanceMillimeters());

    // Create a Pair of String and Integer (Pair<String, Integer>) with the values "Robot" and 254.
    // Print the first value and the second value separated by a space using getFirst() and getSecond().
    Pair<String, Integer> pair = new Pair<>("Robot", 254);
    System.out.println(pair.getFirst() + " " + pair.getSecond());

    // Create a List of Strings (`List<String>`) named `subsystems` using `new ArrayList<>()`.
    // Add the strings "Drivetrain", "Intake", and "Shooter" to `subsystems`.
    List<String> subsystems = new ArrayList<>();
    subsystems.add("Drivetrain");
    subsystems.add("Intake");
    subsystems.add("Shooter");

    // Print the size of the `subsystems` list.
    System.out.println(subsystems.size());

    // Using a for-each loop, iterate over `subsystems` and print each subsystem name.
    for (String subsystem : subsystems) {
        System.out.println(subsystem);
    }
}
