/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

class Drivetrain {
    public void setThrottle(double speed) {}
}

boolean condition = false;
Drivetrain drivetrain = new Drivetrain();

void main() {
    // [whileSyntax]
    while (condition) {
        // code to run when condition is true
    }
    // [/whileSyntax]

    {
        // [whileExample]
        int i = 0;
        while (i < 6) {
            System.out.println(i); // prints 0, 1, 2, 3, 4, 5
            i++;
        }
        // [/whileExample]
    }

    // [whileExample2]
    int timer = 0;

    while (timer <= 5) {
      if (timer < 5) {
        System.out.println("Drive Forward");
      } else {
        System.out.println("Stop Driving");
      }
      timer++;
    }
    // [/whileExample2]


    {
        // [ForExample1]
        int i = 0;
        while (i < 6) {
            System.out.println("Hi!");
            i++;
        }
        // [/ForExample1]
    }

    // [ForExample2]
    for (int i = 0; i < 6; i++) {
        System.out.println("Hi!");
    }
    //[/ForExample2]

    // [forExample]
    for (int i = 0; i < 5; i++){
        System.out.println(i); // prints 0, 1, 2, 3, 4
    }
    // [/forExample]
}
