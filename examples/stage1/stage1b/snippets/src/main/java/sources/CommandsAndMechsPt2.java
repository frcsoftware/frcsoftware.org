/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import static org.wpilib.units.Units.Seconds;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

class CommandsAndMechsPt2 {
  // [defaultCommand]
  class Intake implements Mechanism {
    // Placeholder for TalonFX, SparkMax or SparkFlex
    private final ExampleMotor motor = new ExampleMotor();

    public Intake() {
      setDefaultCommand(idle());
    }

    public Command idle() {
      return run(coroutine -> {
            while (true) {
              motor.setThrottle(0);
              coroutine.yield();
            }
          })
          .named("Idle");
    }
  }

  // [/defaultCommand]

  // [noRequirementsCommand]
  class Robot {
    public Command justPrintHi() {
      return Command.noRequirements(
              coroutine -> {
                System.out.println("Hello World!");
              })
          .named("Hello World!");
    }
  }

  // [/noRequirementsCommand]

  class CommandUsages implements Mechanism {
    // [delayCommand]
    public Command wait5SecsThenPrintHi() {
      return run(coroutine -> {
            coroutine.wait(Seconds.of(5));
            System.out.println("This will show up 5 seconds later.");
          })
          .named("Wait 5 Secs, Then Print Hi");
    }

    // [/delayCommand]

    // [timeoutCommand]
    public Command fullThrottleForFiveSeconds() {
      return fullThrottle().withTimeout(Seconds.of(5));
    }

    public Command fullThrottle() {
      return null; // placeholder for actual command
    }

    // [/timeoutCommand]

    // [parallelCommands]
    public Command parallelCommands() {
      return run(coroutine -> {
            System.out.println("Hi!");
            coroutine.awaitAll(deployIntake(), runIntakeRollers());
          })
          .named("Parallel Commands");
    }

    private Command deployIntake() {
      return null; // placeholder for actual command
    }

    private Command runIntakeRollers() {
      return null; // placeholder for actual command
    }
    // [/parallelCommands]
  }
}
