/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.*;
import java.util.List;
import org.wpilib.command3.Command;
import org.wpilib.command3.NeedsNameBuilderStage;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class AllL4CoralTeleop extends PeriodicOpMode {
  private final Drive drive;
  private final Superstructure superstructure;
  private final Claw claw;
  private final Intake intake;
  private final Indexer indexer;

  public AllL4CoralTeleop(Robot robot) {
    drive = robot.drive;
    superstructure = robot.superstructure;
    claw = robot.claw;
    intake = robot.intake;
    indexer = robot.indexer;

    CommandXboxController controller = new CommandXboxController(0);

    drive.setDefaultCommand(drive.getDriveCommand(controller));

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
                      coro.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.waitUntil(superstructure::atSetpoint);
                      coro.fork(
                          superstructure.setPosition(Superstructure.Positions.HANDOFF),
                          claw.setVoltage(6));
                      coro.waitUntil(superstructure::atSetpoint);
                      coro.await(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.waitUntil(superstructure::atSetpoint);
                    })
                .named("Handoff Coral"));

    controller
        .leftTrigger()
        .onTrue(
            Command.parallel(
                    intake.setState(Intake.State.INTAKING),
                    indexer.setVoltage(12),
                    superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP))
                .named("Intake"));
  }

  private NeedsNameBuilderStage createScoreCommand(List<Pose2d> poses) {
    return Command.noRequirements(
        (coro) -> {
          coro.fork(
              new Drive.AutoAlignCommand(
                      drive,
                      () ->
                          drive.getPose().nearest(poses).transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                  .withRunningContinuously(true));
          coro.fork(superstructure.setPosition(Superstructure.Positions.L4_PREP));
          coro.waitUntil(superstructure::atSetpoint);

          Drive.AutoAlignCommand finalAlign =
              new Drive.AutoAlignCommand(drive, () -> drive.getPose().nearest(poses))
                  .withRunningContinuously(true);
          coro.fork(finalAlign);
          coro.waitUntil(finalAlign::atPosition);

          coro.fork(superstructure.setPosition(Superstructure.Positions.L4_SCORE));
          coro.waitUntil(superstructure::atSetpoint);
          coro.fork(claw.setVoltage(-6));
          coro.park();
        });
  }
}
