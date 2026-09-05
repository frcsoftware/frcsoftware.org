/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
int matchTime = 0;

// [variables]
int CLIMBER_ID = 51; // an integer named CLIMBER_ID that holds the value 51
double UP_POSITION = 33.5; // a double named UP_POSITION that holds the value 33.5
boolean isFinished = false; // a boolean named isFinished that holds the value false
String autoName = "Side Auto"; // a String named autoName that holds the value "Side Auto"
// [/variables]

void main() {
    // [printLiteral]
    System.out.println("hello!");
    // [/printLiteral]

    // [printVariable]
    int num = 4;
    System.out.println(num); // prints out the value 4
    // [/printVariable]

    // [singleLineComment]
    // This prints Hello World
    System.out.println("Hello World");
    // [/singleLineComment]

    // [inlineComment]
    System.out.println("Hello World"); // This prints Hello World
    // [/inlineComment]

    // [multiLineComment]
    /* This prints Hello World
    This is another line */
    System.out.println("Hello World");
    // [/multiLineComment]

    // [goodComment]
    // Calculate how long is left in the match and show it to the drivers
    System.out.println(150 - matchTime);
    // [/goodComment]

    // [badComment]
    // Print 150 minus matchTime
    System.out.println(150 - matchTime);
    // [/badComment]
}
