/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
// [variables]
int totalMotors = 2 + 4;
int motorsPerSide = 4 / 2;
int ballsLeft = 10 - 3;
// [/variables]

void main() {
    // [increments]
    int shotsFired = 6;
    int ballsInHopper = 7;

    shotsFired++; // shotsFired is now 7!
    ballsInHopper--; // ballsInHopper is now 6!

    System.out.println(shotsFired); // prints 7
    System.out.println(ballsInHopper); // prints 6
    // [/increments]

    // [arithmetic]
    int score = 10;
    int ballsCarried = 5;
    score += 2;
    ballsCarried -= 1;

    System.out.println(score); // prints 12
    System.out.println(ballsCarried); // prints 4
    // [/arithmetic]

    // [comparison]
    int ballsScored = 2;
    int ballsNeeded = 4;
    System.out.println(ballsScored > ballsNeeded); // prints false
    // [/comparison]

    // [logical]
    boolean hasBall = true;
    boolean isAligned = false;

    System.out.println(hasBall && isAligned);
    System.out.println(hasBall || isAligned);
    System.out.println(!hasBall);
    // [/logical]


    // [math1]
    int totalPoints = 0;
    int autoPoints = 2;
    totalPoints = autoPoints + 10;
    System.out.println(totalPoints); // prints 12
    // [/math1]

    // [math2]
    int gamePieces = 6;
    System.out.println(gamePieces * 2); // prints 12
    // [/math2]

}
