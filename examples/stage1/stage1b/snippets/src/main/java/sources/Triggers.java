/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.driverstation.RobotState;
import org.wpilib.driverstation.XboxController;
import org.wpilib.framework.TimedRobot;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;

class Triggers {
  void main() {
    var motor = new ExampleMotor();
    var intake = new ExampleMechanism();
    var shooter = new ExampleMechanism();

    // [motorTooFastTrigger]
    Trigger motorTooFastTrigger = new Trigger(() -> motor.speed() > 60);
    boolean isMotorTooFast = motorTooFastTrigger.getAsBoolean();
    // [/motorTooFastTrigger]

    // [multiBindingsVerbose]
    Trigger teleopEnabledTrigger = new Trigger(() -> RobotState.isTeleopEnabled());
    teleopEnabledTrigger.whileTrue(intake.runAtThrottle(0.5));
    teleopEnabledTrigger.whileTrue(shooter.runAtThrottle(0.5));
    // [/multiBindingsVerbose]

    // [multiBindings]
    new Trigger(() -> RobotState.isTeleopEnabled())
        .whileTrue(intake.runAtThrottle(0.5))
        .whileTrue(shooter.runAtThrottle(0.5));
    // [/multiBindings]

    // [xboxGetAButton]
    XboxController xbox = new XboxController(0);
    boolean aButtonHeld = xbox.getAButton();
    // [/xboxGetAButton]

    // [triggerAButton]
    XboxController xbox2 = new XboxController(0);
    Trigger aButton = new Trigger(() -> xbox2.getAButton());
    aButton.whileTrue(someCommand());
    // [/triggerAButton]

    // [commandXboxController]
    CommandXboxController xbox3 = new CommandXboxController(0);
    Trigger aButton2 = xbox3.a();
    aButton2.whileTrue(someCommand());
    // shorthand:
    xbox3.a().whileTrue(someCommand());
    // [/commandXboxController]

    // [scheduleAndPrint]
    Scheduler.getDefault().schedule(myAutonomousCommand());
    System.out.println("Hello!");
    // [/scheduleAndPrint]
  }

  void scheduleMethodExamples() {
    // [timedRobotExample]
    class Robot extends TimedRobot {
      @Override
      public void autonomousInit() {
        Scheduler.getDefault().schedule(myAutonomousCommand());
      }
    }
    // [/timedRobotExample]

    // [opModeExample]
    @Autonomous
    class MyAutoOpMode extends PeriodicOpMode {
      @Override
      public void start() {
        Scheduler.getDefault().schedule(myAutonomousCommand());
      }
    }
    // [/opModeExample]
  }

  // [teleopEnabledRobot]
  public class Robot {
    private final ExampleMechanism intake = new ExampleMechanism();

    private static boolean teleopEnabled() {
      return RobotState.isTeleopEnabled(); // in case you didn't know!
    }

    public Robot() {
      Trigger teleopEnabledTrigger = new Trigger(() -> teleopEnabled());
      teleopEnabledTrigger.onTrue(intake.runAtThrottle(0.5));

      // A shorthand for the above:
      new Trigger(() -> teleopEnabled()).onTrue(intake.runAtThrottle(0.5));
    }
  }

  // [/teleopEnabledRobot]

  Command myAutonomousCommand() {
    return null;
  }

  Command someCommand() {
    return null;
  }

  class ExampleMechanism {
    Command runAtThrottle(double throttle) {
      return null;
    }
  }
}
