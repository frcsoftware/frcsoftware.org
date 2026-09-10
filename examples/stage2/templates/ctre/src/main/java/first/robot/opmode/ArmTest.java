// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;
import org.wpilib.opmode.Utility;

@Utility
public class ArmTest extends PeriodicOpMode {
  private final Robot robot;

  private final CommandGamepad controller;

  public ArmTest(Robot robot) {
    this.robot = robot;

    controller = new CommandGamepad(0);

    controller.button(0).onTrue(robot.arm.setPosition(1));
    controller.button(1).onTrue(robot.arm.setPosition(0));
  }
}
