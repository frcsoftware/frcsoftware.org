/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

class CommandsAndMechsPt1 implements Mechanism {
  private final ExampleMotor motor = new ExampleMotor();

  void partialDefinitions() {
    // [mechanismDef]
    class Intake implements Mechanism {
      // Store any motors specific to the mechanism as private members.
      // This can include TalonFX, SparkMax and/or SparkFlex instances.
      private final ExampleMotor motor = new ExampleMotor();
    }
    // [/mechanismDef]

    // [mechanismInRobotDef]
    class Robot {
      private final Intake intake = new Intake();
    }
    // [/mechanismInRobotDef]
  }

  void commandDefinitions() {
    var exampleCommand =
        // [commandDef]
        run(coroutine -> {
              System.out.println("Full Speed Baby!");
              while (true) {
                motor.setThrottle(1.0);
                coroutine.yield();
              }
            })
            .named("Set to Full Throttle");
    // [/commandDef]

    var exampleCommandWithPriority =
        // [commandWithPriorityDef]
        run(coroutine -> {
              System.out.println("Full Speed Baby!");
              while (true) {
                motor.setThrottle(1.0);
                coroutine.yield();
              }
            })
            .withPriority(1) // recall that the default priority is 0.
            .named("Set to Full Throttle");
    // [/commandWithPriorityDef]
  }

  // [fullThrottleIntake]
  class Intake implements Mechanism {
    private final ExampleMotor motor = new ExampleMotor();

    public Command fullThrottle() {
      return run(coroutine -> {
            System.out.println("Full Speed Baby!");
            while (true) {
              motor.setThrottle(1.0);
              coroutine.yield();
            }
          })
          .named("Set to Full Throttle");
    }
  }

  // [/fullThrottleIntake]

  // [runAtThrottleCommand]
  // This command can now set the intake at any throttle level:
  public Command runAtThrottle(double throttle) {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(throttle);
            coroutine.yield();
          }
        })
        .named("Set Throttle to " + throttle);
  }

  // [/runAtThrottleCommand]

  // [commandAwait]
  public Command printHiThenFullThrottle() {
    return run(coroutine -> {
          // "Hi" is printed before the runAtThrottle Command is run
          System.out.println("Hi!");
          coroutine.await(runAtThrottle(1.0));
        })
        .named("Set to Full Throttle & Print Hi");
  }

  // [/commandAwait]

  // [commandSequence]
  public Command deployPivot() {
    return null; // placeholder for actual command
  }

  public Command runIntakeRollers() {
    return null; // placeholder for actual command
  }

  public Command commandSequence() {
    return run(coroutine -> {
          System.out.println("Hi!"); // step 1
          coroutine.await(deployPivot()); // step 2
          coroutine.await(runIntakeRollers()); // step 3
        })
        .named("Command Sequence");
  }
  // [/commandSequence]
}
