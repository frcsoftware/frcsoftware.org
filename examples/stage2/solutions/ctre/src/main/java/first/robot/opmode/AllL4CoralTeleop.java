/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.FieldConstants;
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
            createScoreCommand(FieldConstants.handleFlip(Poses.BLUE_REEF_LEFT_POSES))
                .named("Autoalign and score (left)"));
    controller
        .rightBumper()
        .whileTrue(
            createScoreCommand(FieldConstants.handleFlip(Poses.BLUE_REEF_RIGHT_POSES))
                .named("Autoalign and score (right)"));

    controller
        .a()
        .onTrue(
            Command.noRequirements(
                    (coroutine) -> {
                      coroutine.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coroutine.waitUntil(superstructure::atSetpoint);
                      coroutine.fork(
                          superstructure.setPosition(Superstructure.Positions.HANDOFF),
                          claw.setVoltage(Claw.Voltages.HOLD_CORAL));
                      coroutine.waitUntil(superstructure::atSetpoint);
                      coroutine.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coroutine.waitUntil(superstructure::atSetpoint);
                    })
                .named("Handoff Coral"));

    controller
        .leftTrigger()
        .onTrue(
            Command.parallel(
                    intake.setState(Intake.State.INTAKING),
                    indexer.setVoltage(Indexer.Voltages.INTAKE),
                    superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP))
                .named("Intake"));
  }

  private NeedsNameBuilderStage createScoreCommand(List<Pose2d> poses) {
    return Command.noRequirements(
        (coroutine) -> {
          coroutine.fork(
              new Drive.AutoAlignCommand(
                      drive,
                      () ->
                          drive.getPose().nearest(poses).transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                  .withRunContinuously(true));
          coroutine.fork(superstructure.setPosition(Superstructure.Positions.L4_PREP));
          coroutine.waitUntil(superstructure::atSetpoint);

          Drive.AutoAlignCommand finalAlign =
              new Drive.AutoAlignCommand(drive, () -> drive.getPose().nearest(poses))
                  .withRunContinuously(true);
          coroutine.fork(finalAlign);
          coroutine.waitUntil(finalAlign::atPosition);

          coroutine.fork(superstructure.setPosition(Superstructure.Positions.L4_SCORE));
          coroutine.waitUntil(superstructure::atSetpoint);
          coroutine.fork(claw.setVoltage(Claw.Voltages.SCORE_CORAL));
          coroutine.park();
        });
  }
}
