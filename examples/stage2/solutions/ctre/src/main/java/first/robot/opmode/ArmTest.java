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
  public ArmTest(Robot robot) {
    CommandXboxController controller = new CommandXboxController(0);

    controller.a().onTrue(robot.superstructure.setArmPosition(0));
    controller.b().onTrue(robot.superstructure.setArmPosition(-0.5));
    controller.x().onTrue(robot.superstructure.setArmPosition(-0.25));
    controller.y().onTrue(robot.superstructure.setArmPosition(-0.75));

    controller.leftBumper().onTrue(robot.superstructure.setArmVoltage(-0.756));
    controller.rightBumper().onTrue(robot.superstructure.setArmVoltage(0));
  }
}
