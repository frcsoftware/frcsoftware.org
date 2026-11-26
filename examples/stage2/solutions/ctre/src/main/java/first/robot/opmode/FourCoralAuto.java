/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import static org.wpilib.units.Units.Seconds;

import first.robot.FieldConstants;
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

  private final Translation2d coralLocation = FieldConstants.handleFlip(new Translation2d(7, -3));

  public FourCoralAuto(Robot robot) {
    drive = robot.drive;
    superstructure = robot.superstructure;
    claw = robot.claw;
    intake = robot.intake;
    indexer = robot.indexer;

    drive.setPose(
        FieldConstants.handleFlip(new Pose2d(new Translation2d(1.4, -1.3), Rotation2d.k180deg)));
    robot.coralSim.setState(CoralSim.CoralState.CLAW);

    RobotModeTriggers.autonomous()
        .onTrue(
            Command.noRequirements(
                    coroutine -> {
                      for (var pose :
                          List.of(
                              FieldConstants.handleFlip(Poses.BLUE_REEF_J),
                              FieldConstants.handleFlip(Poses.BLUE_REEF_K),
                              FieldConstants.handleFlip(Poses.BLUE_REEF_L),
                              FieldConstants.handleFlip(Poses.BLUE_REEF_I))) {
                        coroutine.fork(
                            new Drive.AutoAlignCommand(
                                    drive, pose.transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                                .withRunContinuously(true));
                        coroutine.waitUntil(claw::hasCoral);
                        coroutine.fork(superstructure.setPosition(Superstructure.Positions.L4_PREP));
                        coroutine.waitUntil(superstructure::atSetpoint);

                        Drive.AutoAlignCommand finalAlign =
                            new Drive.AutoAlignCommand(drive, pose).withRunContinuously(true);
                        coroutine.fork(finalAlign);
                        coroutine.waitUntil(finalAlign::atPosition);

                        coroutine.fork(superstructure.setPosition(Superstructure.Positions.L4_SCORE));
                        coroutine.wait(Seconds.of(0.25));
                        var clawCommand = claw.setVoltage(Claw.Voltages.SCORE_CORAL);
                        coroutine.fork(clawCommand);

                        coroutine.fork(
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
                        coroutine.fork(intake.setState(Intake.State.INTAKING));
                        coroutine.fork(indexer.setVoltage(Indexer.Voltages.INTAKE));

                        var superstructureCommand =
                            superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP);
                        coroutine.fork(superstructureCommand);
                        coroutine.waitUntil(indexer::hasCoral);
                        coroutine.fork(
                            Command.noRequirements(
                                    coroutine2 -> {
                                      coroutine2.waitUntil(indexer::hasCoral);
                                      Scheduler.getDefault().cancel(clawCommand);
                                      coroutine2.fork(claw.setVoltage(Claw.Voltages.HOLD_CORAL));
                                      Scheduler.getDefault().cancel(superstructureCommand);
                                      coroutine2.fork(
                                          superstructure.setPosition(
                                              Superstructure.Positions.HANDOFF));
                                      coroutine2.waitUntil(claw::hasCoral);
                                    })
                                .named("Handoff"));
                      }
                    })
                .named("4 Coral Auto"));
  }
}
