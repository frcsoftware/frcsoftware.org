/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import static org.wpilib.units.Units.*;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive.AutoAlignCommand;
import first.robot.mechanisms.Superstructure;
import first.robot.simulation.CoralSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class BasicScoringTeleop extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public BasicScoringTeleop(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));

    controller
        .a()
        .whileTrue(
            Command.noRequirements(
                    (coro) -> {
                      coro.fork(
                          new AutoAlignCommand(
                                  robot.drive,
                                  Poses.BLUE_REEF_A.transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                              .withRunningContinuously(true));
                      coro.fork(robot.superstructure.setPosition(Superstructure.Positions.L4_PREP));
                      coro.waitUntil(robot.superstructure::atSetpoint);

                      AutoAlignCommand finalAlign =
                          new AutoAlignCommand(robot.drive, Poses.BLUE_REEF_A)
                              .withRunningContinuously(true);
                      coro.fork(finalAlign);
                      coro.waitUntil(finalAlign::atPosition);

                      coro.fork(
                          robot.superstructure.setPosition(Superstructure.Positions.L4_SCORE));
                      coro.waitUntil(robot.superstructure::atSetpoint);
                      coro.fork(robot.claw.setVoltage(-6));
                    })
                .named("AutoAlign and Score"));

    controller
        .x()
        .onTrue(
            Command.noRequirements(coro -> robot.coralSim.setState(CoralSim.CoralState.CLAW))
                .named("Put coral in claw"));
  }
}
