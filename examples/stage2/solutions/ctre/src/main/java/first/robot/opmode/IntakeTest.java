/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Volts;

import first.robot.Robot;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

@Utility
public class IntakeTest extends PeriodicOpMode {

  public IntakeTest(Robot robot) {
    CommandXboxController controller = new CommandXboxController(0);

    controller.a().onTrue(robot.intake.setState(Volts.of(12), Degrees.of(-3.206)));
    controller.b().onTrue(robot.intake.setState(Volts.of(0), Degrees.of(90)));
    controller.x().onTrue(robot.intake.setState(Volts.of(0), Degrees.of(45)));
  }
}
