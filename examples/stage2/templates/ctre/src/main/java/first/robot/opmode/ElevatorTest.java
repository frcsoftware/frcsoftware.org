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
public class ElevatorTest extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public ElevatorTest(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    controller.a().onTrue(robot.elevator.setPosition(0));
    controller.b().onTrue(robot.elevator.setPosition(1));
    controller.x().onTrue(robot.elevator.setPosition(0.25));
  }
}
