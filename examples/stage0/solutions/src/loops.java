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
    public void setThrottle(double speed) {
        System.out.println("Spinning drivetrain motors at speed: " + speed);
    }
}
Drivetrain drivetrain = new Drivetrain();

void main() {
  
    // First, create a new integer variable named `error`
    // Below, create a while loop that compares if error is less than 5
    // if that is true, print "Robots should not quit", then increase `error` by one
    // if error is no longer less than 5, print "but yours did"
    int error = 0;
    while (error < 5){
        System.out.println("Robots should not quit");
        error++;
    }
    System.out.println("but yours did");

    // Create a for loop that has a new integer variable named `timer`
    // that is set to 15. Check for when timer is greater than or equal to 0,
    // then decrease timer by one. 
    // Inside the for loop, include a print statement that prints "time left: "
    // and the variable `timer`
    for (int timer = 15; timer >= 0; timer--){
        System.out.println("time left: " + timer);
    }

    // First, create a for loop that has a new integer variable named 
    // `timer` that is set to 15. The for loop checks if `timer` is 
    // greater than or equal to 0 then it decreases timer by one
    // Inside the for loop, have an if statement that checks if timer
    // is less than or equal to 0. If so, set the drivetrain speed to 0
    // If the timer is less than or equal to 15, set the speed to one
    for (int timer = 15; timer >= 0; timer--){
        if (timer <= 0 ){
            drivetrain.setThrottle(0);

        } else if (timer <= 15 ){
            drivetrain.setThrottle(1);
        }
    }
   
}
