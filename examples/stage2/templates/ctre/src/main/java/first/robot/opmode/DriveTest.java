// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.command3.button.CommandNiDsXboxController;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;
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
