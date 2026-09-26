/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
boolean conditionA;
boolean conditionB;
boolean condition;

class Drivetrain {
    public double setThrottle(double speed) {
       return speed;
    }
}

Drivetrain drivetrain = new Drivetrain();


void main() {

    // [ifSyntax]
    if (condition) {
    // code to run when condition is true
    }
    // [/ifSyntax]

    // [ifExample]
    double distance = 60; // distance to the wall, in centimeters
    if (distance > 10) {
        System.out.println("Driving at full speed");
        drivetrain.setThrottle(1); // runs the motors at full speed
    }
    // [/ifExample]

    // [elseIfSyntax]
    if (conditionA) {
    // code to run when conditionA is true
    } else if (conditionB) {
    // code to run when conditionA is false and conditionB is true
    }
    // [/elseIfSyntax]

    // [elseIfExample]
    distance = 30;
    if (distance > 50) {
        System.out.println("Driving at full speed");
        drivetrain.setThrottle(1); // runs the motors at full speed
    } else if (distance > 10) {
        System.out.println("Driving at half speed");
        drivetrain.setThrottle(0.5); // runs the motors at half speed
    }
    // [/elseIfExample]

    // [ifElseSyntax]
    if (condition) {
    // block of code to be executed if the condition is true
    } else {
    // block of code to be executed if the condition is false
    }
    // [/ifElseSyntax]

    // [elseExample]
    distance = 6;
    if (distance > 10) {
        System.out.println("Driving at full speed");
        drivetrain.setThrottle(1); // runs the motors at full speed
    } else {
        System.out.println("Stopped");
        drivetrain.setThrottle(0); // stops the motors
    }
    // [/elseExample]

    // [conditionalExample]
    distance = 10;
    if (distance > 50) {
        System.out.println("Driving at full speed");
        drivetrain.setThrottle(1); // runs the motors at full speed
    } else if (distance > 10) {
        System.out.println("Driving at half speed");
        drivetrain.setThrottle(0.5); // runs the motors at half speed
    } else {
        System.out.println("Stopped");
        drivetrain.setThrottle(0); // stops the motors
    }
    // [/conditionalExample]

}
