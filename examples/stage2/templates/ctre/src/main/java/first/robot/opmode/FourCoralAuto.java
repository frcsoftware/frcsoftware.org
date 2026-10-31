/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import static org.wpilib.units.Units.Seconds;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.*;
import first.robot.simulation.CoralSim;
import java.util.List;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;

@Autonomous
public class FourCoralAuto extends PeriodicOpMode {
  Drive drive;
  Superstructure superstructure;
  Claw claw;
  Intake intake;
  Indexer indexer;

  private final Translation2d coralLocation = new Translation2d(7, -3);

  public FourCoralAuto(Robot robot) {
    drive = robot.drive;
    superstructure = robot.superstructure;
    claw = robot.claw;
    intake = robot.intake;
    indexer = robot.indexer;

    drive.setPose(new Pose2d(new Translation2d(1.4, -1.3), Rotation2d.k180deg));
    robot.coralSim.setState(CoralSim.CoralState.CLAW);

    RobotModeTriggers.autonomous()
        .onTrue(
            Command.noRequirements(
                    coro -> {
                      for (var pose :
                          List.of(
                              Poses.BLUE_REEF_J,
                              Poses.BLUE_REEF_K,
                              Poses.BLUE_REEF_L,
                              Poses.BLUE_REEF_I)) {
                        coro.fork(
                            new Drive.AutoAlignCommand(
                                    drive, pose.transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                                .withRunningContinuously(true));
                        coro.waitUntil(claw::hasCoral);
                        coro.fork(superstructure.setPosition(Superstructure.Positions.L4_PREP));
                        coro.waitUntil(superstructure::atSetpoint);

                        Drive.AutoAlignCommand finalAlign =
                            new Drive.AutoAlignCommand(drive, pose).withRunningContinuously(true);
                        coro.fork(finalAlign);
                        coro.waitUntil(finalAlign::atPosition);

                        coro.fork(superstructure.setPosition(Superstructure.Positions.L4_SCORE));
                        coro.wait(Seconds.of(0.25));
                        var clawCommand = claw.setVoltage(-6);
                        coro.fork(clawCommand);

                        coro.fork(
                            new Drive.AutoAlignCommand(
                                drive,
                                () ->
                                    new Pose2d(
                                        coralLocation,
                                        robot
                                            .drive
                                            .getPose()
                                            .getTranslation()
                                            .minus(coralLocation)
                                            .getAngle()
                                            .get()
                                            .plus(Rotation2d.k180deg))));
                        coro.fork(intake.setState(Intake.State.INTAKING));
                        coro.fork(indexer.setVoltage(12));
                        coro.fork(
                            superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                        coro.waitUntil(indexer::hasCoral);
                        coro.fork(
                            Command.noRequirements(
                                    coro2 -> {
                                      coro2.waitUntil(indexer::hasCoral);
                                      Scheduler.getDefault().cancel(clawCommand);
                                      coro2.fork(claw.setVoltage(6));
                                      coro2.fork(
                                          superstructure.setPosition(
                                              Superstructure.Positions.HANDOFF));
                                      coro2.waitUntil(claw::hasCoral);
                                    })
                                .named("Handoff"));
                      }
                    })
                .named("4 Coral Auto"));
  }
}
