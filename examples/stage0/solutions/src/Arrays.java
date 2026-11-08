/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

void main() {
    // Create an integer array named `driveMotors` that holds the values
    // 1, 2, 3, 4 and print out the third value in the array.
    int[] driveMotors = {1, 2, 3, 4};
    System.out.println(driveMotors[2]);

    // Create another integer array named `distance` that holds the values
    // 10,20,30,40. Iterate through the array using the integer i and
    // print out the value of i.
    int[] distance = {10, 20, 30, 40};
    for (int i : distance) {
         System.out.println(i);
    }

    //Create an double array named `motorSpeeds` that holds the values
    // 0.1, 0.2, 0.3, 0.4, 0.5, Then print out the length of the
    // motorSpeeds array
    double[] motorSpeeds = {0.1, 0.2, 0.3, 0.4, 0.5};
    System.out.println(motorSpeeds.length);

}
