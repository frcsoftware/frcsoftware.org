/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.*;
import first.robot.simulation.CoralSim;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;
import org.wpilib.tunable.TunableDouble;
import org.wpilib.tunable.Tunables;

@Utility
public class ElevatorArmPoseTuning extends PeriodicOpMode {
  Drive drive;
  Superstructure superstructure;
  Intake intake;

  final TunableDouble elevatorPositionTunable;
  final TunableDouble armPositionTunable;

  public ElevatorArmPoseTuning(Robot robot) {
    drive = robot.drive;
    superstructure = robot.superstructure;
    intake = robot.intake;

    CommandXboxController controller = new CommandXboxController(0);

    drive.setDefaultCommand(drive.getDriveCommand(controller));

    elevatorPositionTunable = Tunables.addDouble("Elevator Setpoint", 0);
    armPositionTunable = Tunables.addDouble("Arm Setpoint", 0);

    controller
        .a()
        .onTrue(
            robot
                .superstructure
                .run(
                    (coro) -> {
                      coro.await(
                          superstructure.setPosition(
                              Math.clamp(elevatorPositionTunable.get(), -0, 1.347),
                              Math.clamp(armPositionTunable.get(), -1.5, 0)));
                    })
                .named("Set Superstructure Position from Tunables"));

    controller
        .leftBumper()
        .whileTrue(
            new Drive.AutoAlignCommand(
                    drive, () -> drive.getPose().nearest(Poses.RED_REEF_LEFT_POSES))
                .withRunningContinuously(true));
    controller
        .rightBumper()
        .whileTrue(
            new Drive.AutoAlignCommand(
                    drive, () -> drive.getPose().nearest(Poses.RED_REEF_RIGHT_POSES))
                .withRunningContinuously(true));

    RobotModeTriggers.utility().onTrue(intake.setState(Intake.State.INTAKING));
    robot.coralSim.setState(CoralSim.CoralState.CLAW);
  }

  @Override
  public void close() {
    Tunables.remove("Elevator Setpoint");
    Tunables.remove("Arm Setpoint");
  }
}
