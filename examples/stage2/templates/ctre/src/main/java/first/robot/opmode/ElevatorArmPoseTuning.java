package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.tunable.TunableDouble;
import org.wpilib.tunable.Tunables;

@Utility
public class ElevatorArmPoseTuning extends PeriodicOpMode {
    private final Robot robot;

    private final CommandXboxController controller;

    private final TunableDouble elevatorPositionTunable;
    private final TunableDouble armPositionTunable;

    public ElevatorArmPoseTuning(Robot robot) {
        this.robot = robot;

        controller = new CommandXboxController(0);

        robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));

        elevatorPositionTunable = Tunables.addDouble("Elevator Setpoint", 0);
        armPositionTunable = Tunables.addDouble("Arm Setpoint", 0);

        controller.a().onTrue(
                robot.superstructure.run((coro) -> {
                    coro.await(robot.superstructure.setPosition(Math.clamp(elevatorPositionTunable.get(), 0, 1.347), Math.clamp(armPositionTunable.get(), 0, 1.5)));
                }).named("Set Superstructure Position from Tunables"));

        controller.leftBumper().whileTrue(new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(Poses.RED_REEF_LEFT_POSES)).withRunningContinuously(true));
        controller.rightBumper().whileTrue(new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(Poses.RED_REEF_RIGHT_POSES)).withRunningContinuously(true));


//        Scheduler.getDefault().addPeriodic(() -> {
//            Telemetry.log("elev", elevatorPositionTunable.hasChanged());
//            Telemetry.log("arm", armPositionTunable.hasChanged());
//            Telemetry.log("elevSetpoint", elevatorPositionTunable.get());
//            Telemetry.log("armSetpoint", armPositionTunable.get());
//        });

    }

    @Override
    public void close() {
        Tunables.remove("Elevator Setpoint");
        Tunables.remove("Arm Setpoint");
    }
}
