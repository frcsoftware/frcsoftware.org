// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive;
import first.robot.mechanisms.Drive.AutoAlignCommand;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.command3.button.CommandNiDsXboxController;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.math.util.Units;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;
import org.wpilib.opmode.Utility;
import static org.wpilib.units.Units.*;
import org.wpilib.units.measure.Time;

@Teleop
public class BasicScoringTeleop extends PeriodicOpMode {
  private final Robot robot;

  private final CommandXboxController controller;

  public BasicScoringTeleop(Robot robot) {
    this.robot = robot;

    controller = new CommandXboxController(0);

    robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));

    controller.a().whileTrue(Command.noRequirements((coro) -> {
        coro.fork(new AutoAlignCommand(robot.drive, Poses.BLUE_REEF_A.transformBy(Poses.REEF_PREALIGN_TRANSFORM)).withRunningContinuously(true));
        coro.await(robot.superstructure.setPosition(1, 1));


        AutoAlignCommand finalAlign = new AutoAlignCommand(robot.drive, Poses.BLUE_REEF_A).withRunningContinuously(true);
        coro.fork(finalAlign);
        coro.waitUntil(finalAlign::atPosition);

        coro.fork(robot.superstructure.setPosition(0.5, 0.5));
        coro.wait(Seconds.of(0.25));
        coro.await(robot.claw.setVoltage(-6));
    }).named("AutoAlign and Score"));
  }
}
