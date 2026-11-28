/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Claw;
import first.robot.mechanisms.Drive;
import first.robot.mechanisms.Drive.AutoAlignCommand;
import first.robot.mechanisms.Superstructure;
import first.robot.simulation.CoralSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class SinglePositionCoralTeleop extends PeriodicOpMode {
  Drive drive;
  Superstructure superstructure;
  Claw claw;

  public SinglePositionCoralTeleop(Robot robot) {
    drive = robot.drive;
    superstructure = robot.superstructure;
    claw = robot.claw;

    CommandXboxController controller = new CommandXboxController(0);

    drive.setDefaultCommand(drive.getDriveCommand(controller));

    controller
        .a()
        .whileTrue(
            Command.noRequirements(
                    (coroutine) -> {
                      coroutine.fork(
                          new AutoAlignCommand(
                                  drive,
                                  Poses.BLUE_REEF_A.transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                              .withRunContinuously(true));
                      coroutine.fork(superstructure.setPosition(Superstructure.Positions.L4_PREP));
                      coroutine.waitUntil(superstructure::atSetpoint);

                      AutoAlignCommand finalAlign =
                          new AutoAlignCommand(drive, Poses.BLUE_REEF_A).withRunContinuously(true);
                      coroutine.fork(finalAlign);
                      coroutine.waitUntil(finalAlign::atPosition);

                      coroutine.fork(superstructure.setPosition(Superstructure.Positions.L4_SCORE));
                      coroutine.waitUntil(superstructure::atSetpoint);
                      coroutine.fork(claw.setVoltage(Claw.Voltages.SCORE_CORAL));
                    })
                .named("AutoAlign and Score"));

    controller
        .x()
        .onTrue(
            Command.noRequirements(coroutine -> robot.coralSim.setState(CoralSim.CoralState.CLAW))
                .named("Put coral in claw"));
  }
}
