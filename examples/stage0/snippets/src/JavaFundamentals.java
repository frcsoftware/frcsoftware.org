/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
// [variables]
int CLIMBER_ID = 51;
double UP_POSITION = -33.5;
double DOWN_POSITION = 0;
// [/variables]

void main() {
    // [printLiteral]
    System.out.println("hello!");
    // [/printLiteral]

    // [printVariable]
    int number = 4;
    System.out.println(number); // prints out the value 4
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
}
