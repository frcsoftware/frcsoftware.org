/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.driverstation.XboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

// [FullDrivetrain]
@Teleop
public class MyTeleop extends PeriodicOpMode {
  private final Robot robot;
  // [Controller]
  private final XboxController xboxController = new XboxController(0);

  // [/Controller]

  /** The Robot instance is passed into the opmode via the constructor. */
  public MyTeleop(Robot robot) {
    this.robot = robot;
  }

  // [DriveSimPeriodic]
  @Override
  public void periodic() {
    /* Called periodically (set time interval) while the robot is enabled. */
    robot.drivetrain.arcadeDrive(-xboxController.getLeftY(), xboxController.getRightX());
    // [/DriveSimPeriodic]
    // [/FullDrivetrain]

    // [launchFuel]
    if (xboxController.getRightBumperButton()) {
      // launch
      robot.intakeLauncher.setThrottle(0.9);
      robot.feeder.setThrottle(0.75);

      // [/launchFuel]
      // [intakeFuel]
    } else if (xboxController.getLeftBumperButton()) {
      // intake
      robot.intakeLauncher.setThrottle(0.8);
      robot.feeder.setThrottle(-1.0);

      // [/intakeFuel]
      // [outakeFuel]
    } else if (xboxController.getAButton()) {
      // outake
      robot.intakeLauncher.setThrottle(-0.8);
      robot.feeder.setThrottle(1.0);

      // [/outakeFuel]
      // [stopMotors]
    } else {
      // stop
      robot.intakeLauncher.setThrottle(0.0);
      robot.feeder.setThrottle(0.0);
    }
    // [/stopMotors]
  }
}
