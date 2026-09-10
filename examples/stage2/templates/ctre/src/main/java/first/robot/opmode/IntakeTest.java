// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.math.util.Units;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

@Utility
public class IntakeTest extends PeriodicOpMode {
  private final Robot robot;

  private final CommandGamepad controller;

  public IntakeTest(Robot robot) {
    this.robot = robot;

    controller = new CommandGamepad(0);

    controller.button(0).onTrue(Command.noRequirements(coro -> {
            coro.await(robot.intake.setRollerVoltage(12));
            coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(0)));
            }).named("Deploy intake"));
      controller.button(1).onTrue(Command.noRequirements(coro -> {
          coro.await(robot.intake.setRollerVoltage(0));
          coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(90)));
      }).named("Retract intake"));
      controller.button(2).onTrue(Command.noRequirements(coro -> {
          coro.await(robot.intake.setRollerVoltage(0));
          coro.await(robot.intake.setPivotPosition(Units.degreesToRotations(45)));
      }).named("Retract intake"));
  }
}
