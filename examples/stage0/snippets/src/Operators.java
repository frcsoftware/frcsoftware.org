/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
// [variables]
int answer1 = 2 + 4;
int answer2 = 6 / 3;
int answer3 = 10 - 3;
// [/variables]

void main() {
    // [increments]
    int x = 6;
    int y = 7;

    x++; // x is now 7!
    y--; // y is now 6!

    System.out.println(x); // prints 7
    System.out.println(y); // prints 6
    // [/increments]

    // [arithmetic]
    int a = 10;
    int b = 5;
    a += 2;
    b -= 1;

    System.out.println(a); // prints 12
    System.out.println(b); // prints 4
    // [/arithmetic]

    // [comparison]
    int c = 2;
    int d = 4;
    System.out.println(c > d); // prints false
    // [/comparison]

    // [logical]
    boolean fiveIsGreaterThanThree = 5 > 3; // true
    boolean nineIsLessThanTwo = 9 < 2; // false

    System.out.println(fiveIsGreaterThanThree && nineIsLessThanTwo);
    System.out.println(fiveIsGreaterThanThree || nineIsLessThanTwo);
    System.out.println(!fiveIsGreaterThanThree);
    // [/logical]


    // [math1]
    int e = 0;
    int f = 2;
    System.out.println(e = f + 10); // prints 12
    // [/math1]

    // [math2]
    int magicNumber = 6;
    System.out.println(magicNumber * 2); // prints 12
    // [/math2]

}
