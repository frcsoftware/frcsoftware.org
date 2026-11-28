/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

@Utility
public class DriveTest extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public DriveTest(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));
  }
}
