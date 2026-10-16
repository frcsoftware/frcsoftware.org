/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.math.util.Units;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

@Utility
public class IntakeTest extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public IntakeTest(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    controller
        .a()
        .onTrue(
            Command.noRequirements(
                    coro -> {
                      coro.await(robot.intake.setRollerVoltage(12));
                      //
                      // coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(0)));
                      coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(-3.206)));
                    })
                .named("Deploy intake"));
    controller
        .b()
        .onTrue(
            Command.noRequirements(
                    coro -> {
                      coro.await(robot.intake.setRollerVoltage(0));
                      coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(90)));
                    })
                .named("Retract intake"));
    controller
        .x()
        .onTrue(
            Command.noRequirements(
                    coro -> {
                      coro.await(robot.intake.setRollerVoltage(0));
                      coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(45)));
                    })
                .named("Retract intake"));
  }
}
