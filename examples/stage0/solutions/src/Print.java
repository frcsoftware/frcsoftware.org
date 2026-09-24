/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

void main() {
    // Print "Hello World!", with a newline at the end. From now on, you should use
    // `System.out.println()` and not `System.out.print()` whenever an exercise
    // asks you to print something.
    System.out.println("Hello World!");

    // Print the following lines, each with a separate `println()` statement:
    // "The robot knows where it is at all times.
    // It knows this because it knows where it isn't."
    System.out.println("The robot knows where it is at all times.");
    System.out.println("It knows this because it knows where it isn't.");

    // Define a variable `pi` that is equal to 3.14159.
    // HINT: Make sure to pick the correct datatypes.
    double pi = 3.14159;
    // Define an variable `g` that is equal to 10.
    int g = 10;
    // Define a variable `mode` that is equal to "autonomous".
    String mode = "autonomous";

    // Now, print all three variables in the **same** print statement,
    // separated by spaces. You can do this by passing in the three variables, and
    // "adding" an empty string (" ") in between each pair, as if you were adding
    // two numbers together, using the `+` operator. Ex. [pi + " " + g].
    // Java recognizes that one of the items being added is a string, and
    // converts the items on either side to a string before combining everything.
    // This method of combining strings via the `+` operator is known as string
    // concatenation.
    System.out.println(pi + " " + g + " " + mode);

    // Now, change pi to equal 3.142857 (a slightly incorrect approximation of pi
    // equal to 22 divided by 7). Then, print the value of `pi` again.
    pi = 3.142857;
    System.out.println(pi);
    
    // Create a variable `degrees` of type `double` and assign it a value of
    // 360. Then, print the variable to observe type narrowing behavior
    // (it prints 360.0 with a decimal part, instead of just 360, since the
    // variable uses a dataype with decimal parts).
    double degrees = 360;
    System.out.println(degrees);
}