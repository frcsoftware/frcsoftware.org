/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.driverstation.RobotState;

class SuppliersInCommandBased {
  void main() {
    var motor = new ExampleMotor();
    var xbox = new CommandXboxController(0);

    // [doubleSupplierExample]
    DoubleSupplier supplier = () -> xbox.getLeftY();
    double controllerOutput = supplier.getAsDouble();

    // [/doubleSupplierExample]

    // [triggerBooleanSupplier]
    new Trigger(() -> motor.speed() > 60);

    // [/triggerBooleanSupplier]
  }

  void correctUsages() {
    class Intake implements Mechanism {
      private final ExampleMotor motor = new ExampleMotor();

      // [runAtThrottleSupplier]
      public Command runAtThrottle(DoubleSupplier throttleSupplier) {
        return run(coroutine -> {
              while (true) {
                double throttle = throttleSupplier.getAsDouble();
                motor.setThrottle(throttle);
                coroutine.yield();
              }
            })
            .named("Run Intake");
      }

      // [/runAtThrottleSupplier]

      // [untilModifier]
      // From the Intake class mentioned earlier
      public Command fullThrottleUntilRobotDisabled() {
        return fullThrottle()
            .until(() -> RobotState.isDisabled())
            .named("Full Throttle Until Disable");
      }

      private Command fullThrottle() {
        return null; // placeholder for actual command
      }
      // [/untilModifier]
    }

    // [correctCommand]
    class Robot {
      public Robot() {
        var xbox = new CommandXboxController(0);
        var intake = new Intake();
        intake.setDefaultCommand(intake.runAtThrottle(() -> xbox.getLeftY()));
      }
    }
    // [/correctCommand]
  }

  void incorrectUsages() {
    // [runAtThrottleDouble]
    class Intake implements Mechanism {
      // Placeholder for TalonFX, SparkMax or SparkFlex
      private final ExampleMotor motor = new ExampleMotor();

      public Command runAtThrottle(double throttle) {
        return run(coroutine -> {
              while (true) {
                motor.setThrottle(throttle);
                coroutine.yield();
              }
            })
            .named("Run Intake");
      }
    }
    // [/runAtThrottleDouble]

    // [incorrectCommand]
    class Robot {
      public Robot() {
        var xbox = new CommandXboxController(0);
        var intake = new Intake();
        double controllerOutput = xbox.getLeftY();
        intake.setDefaultCommand(intake.runAtThrottle(controllerOutput));
      }
    }
    // [/incorrectCommand]
  }
}
