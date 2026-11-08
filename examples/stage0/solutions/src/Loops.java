/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

class Drivetrain {
    /**
     * Mock function to simulate running drivetrain motors at specified throttle
     * @param speed
     */
    public void setThrottle(double speed) {
        System.out.println("Spinning drivetrain motors at speed: " + speed);
    }
}
Drivetrain drivetrain = new Drivetrain();

void main() {

    // First, set calibrationTime to 0
    // Below, create a while loop that checks if calibrationTime is less than 5
    // if that is true, print "Calibrating", then increase calibrationTime by one
    // when calibrationTime is no longer less than 5, print "Done!"
    int calibrationTime = 0;
    while (calibrationTime < 5){
        System.out.println("Calibrating");
        calibrationTime++;
    }
    System.out.println("Done!");

    // Create a for loop that has a new integer variable named `timer`
    // that is set to 15. Check for when `timer` is greater than or equal to 0,
    // then decrease `timer` by one.
    // Inside the for loop, include a print statement that prints "time left: "
    // and the variable `timer`
    for (int timer = 15; timer >= 0; timer--){
        System.out.println("time left: " + timer);
    }

    // First, create a for loop that has a new integer variable named
    // `timer` that is set to 15. The for loop checks if `timer` is
    // greater than or equal to 0, then it decreases `timer` by one
    // Inside the for loop, have an if statement that checks if `timer`
    // is less than or equal to 0. If so, set the drivetrain speed to 0
    // If the `timer` is less than or equal to 15, set the speed to one
    for (int timer = 15; timer >= 0; timer--){
        if (timer <= 0 ){
            drivetrain.setThrottle(0);
        } else if (timer <= 15 ){
            drivetrain.setThrottle(1);
        }
    }

}
