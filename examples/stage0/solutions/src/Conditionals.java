/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

class Drivetrain {
    /**
     * Dummy function that spins drivetrain motors at the specified speed.
     * @param speed
     */
    public void setThrottle(int speed) {
        String throttleInfo = String.format("Spinning drivetrain motors at speed: %d", speed);
        System.out.println(throttleInfo);
    }
}

Drivetrain drivetrain = new Drivetrain();

void main() {
    // First, create a new integer variable `shooterVelocity` below the definition
    // of `shooterEnabled`. Based on the value of `shooterEnabled`, decide whether
    // to turn on (shooterVelocity = 1) or turn off (shooterVelocity = 0) the
    // shooter motors. Then print the value of `shooterVelocity`. After running,
    // change `shooterEnabled` to `true` and re-run; the code should now print 1.
    boolean shooterEnabled = false;
    int shooterVelocity;
    if (shooterEnabled == true) {
        shooterVelocity = 1;
    } else {
        shooterVelocity = 0;
    }
    System.out.println(shooterVelocity);

    // Print "Success!" if `statusCode` is equal to 0. Otherwise, if `statusCode`
    // is NOT EQUAL to 1, print "User exited code." Else, print "There was an error."
    // After running, change `statusCode` to 0, then 2; the code should print
    // "Success!" and "User exited code." respectively.
    int statusCode = 1;
    if (statusCode == 0) {
        System.out.println("Success!");
    } else if (statusCode != 1) {
        System.out.println("User exited code.");
    } else {
        System.out.println("There was an error.");
    }

    // If `input` is greater than or equal to 5, print "Controller input detected."
    // Otherwise, print "Discarding input." and set `input` to 0. Then, below the conditional
    // statement, call `drivetrain.setThrottle()`, passing in the variable `input`.
    // After running the code, change `input` to 2; the code should now print
    // "Discarding input."
    int input = 20;
    if (input >= 5) {
        System.out.println("Controller input detected.");
    } else {
        input = 0;
        System.out.println("Discarding input.");
    }
    drivetrain.setThrottle(input);

    // If the value of `shooterAngleDeg` is greater than 75 degrees OR is less
    // than 10 degrees, print "The shooter should not move further." Otherwise, print
    // "The shooter can still move." After running, change `shooterAngleDeg` to 76;
    // the code should now print "The shooter should not move further."
    int shooterAngleDeg = 54;
    if (shooterAngleDeg > 75 || shooterAngleDeg < 10) {
        System.out.println("The shooter should not move further.");
    } else {
        System.out.println("The shooter can still move.");
    }

    // Define a variable `intakeEnabled` whose value is the inverse of `intakeDisabled`,
    // by using the logical NOT operator. Then print the value of `intakeEnabled`.
    // After running, change `intakeDisabled` to `true`; the code should now print
    // `false`.
    boolean intakeDisabled = false;
    boolean intakeEnabled = !intakeDisabled;
    System.out.println(intakeEnabled);
}