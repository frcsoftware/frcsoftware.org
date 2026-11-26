/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import static org.wpilib.units.Units.Meters;

import first.robot.Robot;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

@Utility
public class ElevatorTest extends PeriodicOpMode {

  public ElevatorTest(Robot robot) {
    CommandXboxController controller = new CommandXboxController(0);

    controller.a().onTrue(robot.superstructure.setElevatorPosition(Meters.of(0)));
    controller.b().onTrue(robot.superstructure.setElevatorPosition(Meters.of(1)));
    controller.x().onTrue(robot.superstructure.setElevatorPosition(Meters.of(0.25)));
  }
}
