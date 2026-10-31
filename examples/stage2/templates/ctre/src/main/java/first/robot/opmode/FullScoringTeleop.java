/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive;
import first.robot.mechanisms.Intake;
import first.robot.mechanisms.Superstructure;
import java.util.List;
import org.wpilib.command3.Command;
import org.wpilib.command3.NeedsNameBuilderStage;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class FullScoringTeleop extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public FullScoringTeleop(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));

    controller
        .leftBumper()
        .whileTrue(
            createScoreCommand(Poses.BLUE_REEF_LEFT_POSES).named("Autoalign and score (left)"));
    controller
        .rightBumper()
        .whileTrue(
            createScoreCommand(Poses.BLUE_REEF_RIGHT_POSES).named("Autoalign and score (right)"));

    controller
        .a()
        .onTrue(
            Command.noRequirements(
                    (coro) -> {
                      coro.fork(
                          robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.waitUntil(robot.superstructure::atSetpoint);
                      coro.fork(
                          robot.superstructure.setPosition(Superstructure.Positions.HANDOFF),
                          robot.claw.setVoltage(6));
                      coro.waitUntil(robot.superstructure::atSetpoint);
                      coro.await(
                          robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.waitUntil(robot.superstructure::atSetpoint);
                    })
                .named("Handoff Coral"));

    controller
        .leftTrigger()
        .onTrue(
            Command.parallel(
                    robot.intake.setState(Intake.State.INTAKING),
                    robot.indexer.setVoltage(12),
                    robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP))
                .named("Intake"));
  }

  private NeedsNameBuilderStage createScoreCommand(List<Pose2d> poses) {
    return Command.noRequirements(
        (coro) -> {
          coro.fork(
              new Drive.AutoAlignCommand(
                      robot.drive,
                      () ->
                          robot
                              .drive
                              .getPose()
                              .nearest(poses)
                              .transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                  .withRunningContinuously(true));
          coro.fork(robot.superstructure.setPosition(Superstructure.Positions.L4_PREP));
          coro.waitUntil(robot.superstructure::atSetpoint);

          Drive.AutoAlignCommand finalAlign =
              new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(poses))
                  .withRunningContinuously(true);
          coro.fork(finalAlign);
          coro.waitUntil(finalAlign::atPosition);

          coro.fork(robot.superstructure.setPosition(Superstructure.Positions.L4_SCORE));
          coro.waitUntil(robot.superstructure::atSetpoint);
          coro.fork(robot.claw.setVoltage(-6));
          coro.park();
        });
  }
}
