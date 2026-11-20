/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import static org.wpilib.units.Units.Seconds;

import first.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;

@Autonomous
public class DriveBackThenShoot extends PeriodicOpMode {
  private final Robot robot;

  public DriveBackThenShoot(Robot robot) {
    this.robot = robot;
  }

  private Command autoCommand() {
    return Command.noRequirements(
            coroutine -> {
              coroutine.await(
                  robot.drivetrain.arcadeDrive(() -> -0.2, () -> 0.0).withTimeout(Seconds.of(2)));
              coroutine.awaitAll(robot.intakeLauncher.shoot(), robot.feeder.feed());
            })
        .named("Drive Back Then Shoot");
  }

  @Override
  public void start() {
    Scheduler.getDefault().schedule(autoCommand());
  }
}
