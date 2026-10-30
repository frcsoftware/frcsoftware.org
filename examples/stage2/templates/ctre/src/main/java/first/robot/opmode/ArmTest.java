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
public class ArmTest extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public ArmTest(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    controller.a().onTrue(robot.arm.setPosition(0));
    controller.b().onTrue(robot.arm.setPosition(-0.5));
    controller.x().onTrue(robot.arm.setPosition(-0.25));
    controller.y().onTrue(robot.arm.setPosition(-0.75));

    controller.leftBumper().onTrue(robot.arm.setVoltage(-0.756));
    controller.rightBumper().onTrue(robot.arm.setVoltage(0));
  }
}
