package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive;
import org.wpilib.command3.Command;
import org.wpilib.command3.NeedsNameBuilderStage;
import org.wpilib.command3.button.CommandNiDsXboxController;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.util.Units;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

import java.util.List;

import static org.wpilib.units.Units.Seconds;

@Teleop
public class FullScoringTeleop extends PeriodicOpMode {
    private final Robot robot;

    private final CommandXboxController controller;

    public FullScoringTeleop(Robot robot) {
        this.robot = robot;

        controller = new CommandXboxController(0);

        robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));

        controller.leftBumper().whileTrue(createScoreCommand(Poses.BLUE_REEF_LEFT_POSES).named("Autoalign and score (left)"));
        controller.rightBumper().whileTrue(createScoreCommand(Poses.BLUE_REEF_RIGHT_POSES).named("Autoalign and score (right)"));
    }

    private NeedsNameBuilderStage createScoreCommand(List<Pose2d> poses) {
        return Command.noRequirements((coro) -> {
            coro.fork(new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(poses).transformBy(Poses.REEF_PREALIGN_TRANSFORM)).withRunningContinuously(true));
            coro.await(robot.superstructure.setPosition(1, 1));

            Drive.AutoAlignCommand finalAlign = new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(poses)).withRunningContinuously(true);
            coro.fork(finalAlign);
            coro.waitUntil(finalAlign::atPosition);

            coro.fork(robot.superstructure.setPosition(0.5, 0.5));
            coro.wait(Seconds.of(0.25));
            coro.await(robot.claw.setVoltage(-6));
        });
    }
}
