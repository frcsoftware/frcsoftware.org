/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Trigger;
import org.wpilib.driverstation.XboxController;

class SpotTheError {
  private final XboxController xbox = new XboxController(0);
  private final Intake intake = new Intake();

  void triggerBindings() {
    // [triggerCapturedValueBug]
    boolean aButton = xbox.getAButton();
    Trigger aButtonTrigger = new Trigger(() -> aButton);
    aButtonTrigger.whileTrue(intake.runAtThrottle(0.5));
    // [/triggerCapturedValueBug]

    // [intakeOnTrueBug]
    new Trigger(() -> xbox.getLeftBumperButton()).onTrue(intake.runAtThrottle(0.5));
    // [/intakeOnTrueBug]

    // [intakeWhileTrue]
    new Trigger(() -> xbox.getLeftBumperButton()).whileTrue(intake.runAtThrottle(0.5));
    // [/intakeWhileTrue]
  }

  void triggerBindingsFixed() {
    // [triggerCapturedValueFix]
    Trigger aButtonTrigger = new Trigger(() -> xbox.getAButton());
    aButtonTrigger.whileTrue(intake.runAtThrottle(0.5));
    // [/triggerCapturedValueFix]
  }

  // [intakeClass]
  class Intake implements Mechanism {
    private final ExampleMotor motor = new ExampleMotor();

    Command runAtThrottle(double throttle) {
      return run(coroutine -> {
            while (true) {
              motor.setThrottle(throttle);
              coroutine.yield();
            }
          })
          .named("Intake");
    }
  }
  // [/intakeClass]

  void intakeClassWithDefaultCmd() {
    // [intakeClassWithDefaultCmd]
    class Intake implements Mechanism {
      private final ExampleMotor motor = new ExampleMotor();

      public Intake() {
        setDefaultCommand(runAtThrottle(0));
      }

      Command runAtThrottle(double throttle) {
        return run(coroutine -> {
              while (true) {
                motor.setThrottle(throttle);
                coroutine.yield();
              }
            })
            .named("Intake");
      }
    }
    // [/intakeClassWithDefaultCmd]
  }
}
