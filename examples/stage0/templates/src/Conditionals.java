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


    // Print "Success!" if `statusCode` is equal to 0. Otherwise, if `statusCode`
    // is NOT EQUAL to 1, print "User exited code." Else, print "There was an error."
    // After running, change `statusCode` to 0, then 2; the code should print
    // "Success!" and "User exited code." respectively.
    int statusCode = 1;


    // If `input` is greater than or equal to 5, print "Controller input detected."
    // Otherwise, print "Discarding input." and set `input` to 0. Then, below the conditional
    // statement, call `drivetrain.setThrottle()`, passing in the variable `input`.
    // After running the code, change `input` to 2; the code should now print
    // "Discarding input."
    int input = 20;


    // If the value of `shooterAngleDeg` exceeds 75 degrees OR is less than 10 degrees,
    // print "The shooter has been stopped." Otherwise, print "The shooter is moving."
    // After running, change `shooterAngleDeg` to 76; the code should now print
    // "The shooter has been stopped."
    int shooterAngleDeg = 54;


    // Define a variable `intakeEnabled` whose value is the inverse of `intakeDisabled`,
    // using logical operators. Then print the value of `intakeEnabled`.
    // After running, change `intakeDisabled` to `true`; the code should now print
    // `false`.
    boolean intakeDisabled = false;

}