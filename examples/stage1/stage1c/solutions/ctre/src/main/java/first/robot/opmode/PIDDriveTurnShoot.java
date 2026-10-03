package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

@Autonomous
public class PIDDriveTurnShoot implements OpMode {

  public PIDDriveTurnShoot(Robot robot) {
    robot.drivetrain.setPose(new Pose2d(new Translation2d(3.6, 5.7), Rotation2d.k180deg));

    RobotModeTriggers.autonomous()
        .onTrue(
            Command.noRequirements(
                    coroutine -> {
                      coroutine.await(robot.drivetrain.driveDistance(1));
                      coroutine.await(robot.drivetrain.turnInPlace(Rotation2d.fromDegrees(135)));
                      coroutine.await(robot.drivetrain.driveDistance(-1.2));

                      coroutine.awaitAll(robot.intakeLauncher.shoot(), robot.feeder.feed());
                    })
                .named("Drive Turn Shoot Auto"));
  }
}
