package first.robot.opmode;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive;
import first.robot.mechanisms.Superstructure;
import org.wpilib.command3.*;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;
import org.wpilib.telemetry.Telemetry;

import java.util.ArrayList;
import java.util.List;

import static org.wpilib.units.Units.Seconds;

@Teleop
public class FullCoralTeleop extends PeriodicOpMode {
    private final Robot robot;

    private CommandXboxController controller;

    private Trigger stowButton;
    private Trigger l2Button;
    private Trigger l3Button;
    private Trigger l4Button;
    private Trigger leftAlignButton;
    private Trigger rightAlignButton;

    private int selectedLevel = 4;

    public FullCoralTeleop (Robot robot) {
        this.robot = robot;

        controller = new CommandXboxController(0);

        stowButton = controller.x();
        l2Button = controller.a();
        l3Button = controller.b();
        l4Button = controller.y();
        leftAlignButton = controller.leftBumper();
        rightAlignButton = controller.rightBumper();

        l2Button.onTrue(Command.noRequirements((coro) -> selectedLevel = 2).named("Select L2"));
        l3Button.onTrue(Command.noRequirements((coro) -> selectedLevel = 3).named("Select L3"));
        l4Button.onTrue(Command.noRequirements((coro) -> selectedLevel = 4).named("Select L4"));

        robot.drive.setDefaultCommand(robot.drive.getDriveCommand(controller));

        StateMachine stateMachine = new StateMachine("Teleop State Machine");

        StateMachine.State stowState = stateMachine.addState(
                Command.noRequirements((coro) -> {
                    coro.await(robot.superstructure.setPosition(Superstructure.Positions.STOW));
                    while (true) { coro.yield(); }
                }).named("Stow"));
        StateMachine.State handoff = stateMachine.addState(
                Command.noRequirements((coro) -> {
                coro.await(robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                coro.waitUntil(robot.indexer::hasCoral);
                coro.await(robot.claw.setVoltage(6));
                coro.await(robot.superstructure.setPosition(Superstructure.Positions.HANDOFF));
                robot.coralSim.attemptHandoff();
                coro.waitUntil(robot.claw::hasCoral);
                coro.await(robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                while (true) { coro.yield(); }
                }).named("Handoff"));
        StateMachine.State scoreL2Left = stateMachine.addState(createScoreCommand(Poses.RED_REEF_LEFT_POSES, Superstructure.Positions.L2_PREP, Superstructure.Positions.L2_SCORE).named("Score L2 Left"));
        StateMachine.State scoreL2Right = stateMachine.addState(createScoreCommand(Poses.RED_REEF_RIGHT_POSES, Superstructure.Positions.L2_PREP, Superstructure.Positions.L2_SCORE).named("Score L2 Right"));
        StateMachine.State scoreL3Left = stateMachine.addState(createScoreCommand(Poses.RED_REEF_LEFT_POSES, Superstructure.Positions.L3_PREP, Superstructure.Positions.L3_SCORE).named("Score L2 Left"));
        StateMachine.State scoreL3Right = stateMachine.addState(createScoreCommand(Poses.RED_REEF_RIGHT_POSES, Superstructure.Positions.L3_PREP, Superstructure.Positions.L3_SCORE).named("Score L3 Right"));
        StateMachine.State scoreL4Left = stateMachine.addState(createScoreCommand(Poses.RED_REEF_LEFT_POSES, Superstructure.Positions.L4_PREP, Superstructure.Positions.L4_SCORE).named("Score L4 Left"));
        StateMachine.State scoreL4Right = stateMachine.addState(createScoreCommand(Poses.RED_REEF_RIGHT_POSES, Superstructure.Positions.L4_PREP, Superstructure.Positions.L4_SCORE).named("Score L4 Right"));

        stateMachine.setInitialState(stowState);

        stowState.switchTo(handoff).when(robot.indexer::hasCoral);

        stateMachine.switchFromAny(stowState, handoff).to(scoreL2Left).when(leftAlignButton.and(() -> selectedLevel == 2).and(robot.claw::hasCoral));
        stateMachine.switchFromAny(stowState, handoff).to(scoreL2Right).when(rightAlignButton.and(() -> selectedLevel == 2).and(robot.claw::hasCoral));
        stateMachine.switchFromAny(stowState, handoff).to(scoreL3Left).when(leftAlignButton.and(() -> selectedLevel == 3).and(robot.claw::hasCoral));
        stateMachine.switchFromAny(stowState, handoff).to(scoreL3Right).when(rightAlignButton.and(() -> selectedLevel == 3).and(robot.claw::hasCoral));
        stateMachine.switchFromAny(stowState, handoff).to(scoreL4Left).when(leftAlignButton.and(() -> selectedLevel == 4).and(robot.claw::hasCoral));
        stateMachine.switchFromAny(stowState, handoff).to(scoreL4Right).when(rightAlignButton.and(() -> selectedLevel == 4).and(robot.claw::hasCoral));

        stateMachine.switchFromAny(scoreL2Left, scoreL2Right, scoreL3Left, scoreL3Right, scoreL4Left, scoreL4Right).to(handoff).whenComplete();

        stateMachine.switchFromAny(scoreL2Left, scoreL2Right, scoreL3Left, scoreL3Right, scoreL4Left, scoreL4Right).to(stowState).when(leftAlignButton.or(rightAlignButton).negate());

        stateMachine.switchFromAny().to(stowState).when(stowButton);


//        RobotModeTriggers.disabled().onFalse(stateMachine);
        Scheduler.getDefault().schedule(stateMachine);
//        RobotModeTriggers.teleop().onTrue(stateMachine);

        Scheduler.getDefault().addPeriodic(() -> {
//            Telemetry.log("State Machine State", stateMachine.)

            var commands = new ArrayList<>(Scheduler.getDefault().getRunningCommands());
            for (int i = 0; i < commands.size(); i++) {
                Telemetry.log("ActiveCommands/"+ i, commands.get(i).name());
            }
            Telemetry.log("StateMachine", stateMachine);
            Telemetry.log("Selected Coral Level", selectedLevel);
        });

        Scheduler.getDefault().addEventListener((event) -> {
            if (event instanceof SchedulerEvent.Canceled) {
                System.out.println("CANCELLED: " + ((SchedulerEvent.Canceled) event).command().name());
            } else if (event instanceof SchedulerEvent.Interrupted) {
                System.out.println("INTERRUPTED: " + ((SchedulerEvent.Interrupted) event).command().name() + " by " + ((SchedulerEvent.Interrupted) event).interrupter().name());
            } else if (event instanceof SchedulerEvent.Completed) {
                System.out.println("COMPLETED: " + ((SchedulerEvent.Completed) event).command().name());
            }
        });
    }

    private NeedsNameBuilderStage createScoreCommand(List<Pose2d> poses, Superstructure.Positions prepPosition, Superstructure.Positions scorePosition) {
        return Command.noRequirements((coro) -> {
            coro.fork(new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(poses).transformBy(Poses.REEF_PREALIGN_TRANSFORM)).withRunningContinuously(true));
            coro.await(robot.superstructure.setPosition(prepPosition));

            Drive.AutoAlignCommand finalAlign = new Drive.AutoAlignCommand(robot.drive, () -> robot.drive.getPose().nearest(poses)).withRunningContinuously(true);
            coro.fork(finalAlign);
            coro.waitUntil(finalAlign::atPosition);

            coro.fork(robot.superstructure.setPosition(scorePosition));
            coro.wait(Seconds.of(0.25));
            coro.await(robot.claw.setVoltage(-6));
            robot.coralSim.attemptScore();
        });
    }
}
