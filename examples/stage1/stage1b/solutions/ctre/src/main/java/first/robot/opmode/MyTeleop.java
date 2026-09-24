/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.button.CommandNiDsXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class MyTeleop extends PeriodicOpMode {
  private final Robot robot;
  private final CommandNiDsXboxController xbox = new CommandNiDsXboxController(0);

  public MyTeleop(Robot robot) {
    this.robot = robot;

    robot.drivetrain.setDefaultCommand(
        robot.drivetrain.arcadeDrive(() -> -xbox.getLeftY(), () -> xbox.getRightX()));

    xbox.leftBumper().whileTrue(robot.intakeLauncher.intake()).whileTrue(robot.feeder.intake());

    xbox.rightBumper().whileTrue(robot.intakeLauncher.shoot()).whileTrue(robot.feeder.feed());

    xbox.a().whileTrue(robot.intakeLauncher.outtake()).whileTrue(robot.feeder.outtake());
  }

  @Override
  public void periodic() {}
}
