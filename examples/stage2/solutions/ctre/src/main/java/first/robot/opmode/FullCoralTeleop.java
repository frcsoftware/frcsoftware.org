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
import java.util.List;
import org.wpilib.command3.*;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class FullCoralTeleop extends PeriodicOpMode {
  private final Drive drive;
  private final Superstructure superstructure;
  private final Claw claw;
  private final Intake intake;
  private final Indexer indexer;

  private int selectedLevel = 4;

  public FullCoralTeleop(Robot robot) {
    drive = robot.drive;
    superstructure = robot.superstructure;
    claw = robot.claw;
    intake = robot.intake;
    indexer = robot.indexer;

    CommandXboxController controller = new CommandXboxController(0);

    Trigger stowButton = controller.x();
    Trigger l2Button = controller.a();
    Trigger l3Button = controller.b();
    Trigger l4Button = controller.y();
    Trigger leftAlignButton = controller.leftBumper();
    Trigger rightAlignButton = controller.rightBumper();
    Trigger intakeButton = controller.leftTrigger();

    l2Button.onTrue(Command.noRequirements((coro) -> selectedLevel = 2).named("Select L2"));
    l3Button.onTrue(Command.noRequirements((coro) -> selectedLevel = 3).named("Select L3"));
    l4Button.onTrue(Command.noRequirements((coro) -> selectedLevel = 4).named("Select L4"));

    drive.setDefaultCommand(drive.getDriveCommand(controller));

    StateMachine stateMachine = new StateMachine("Teleop State Machine");

    StateMachine.State intaking =
        stateMachine.addState(
            Command.noRequirements(
                    coro -> {
                      coro.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.fork(intake.setState(Intake.State.INTAKING));
                      coro.fork(indexer.setVoltage(12));
                      coro.park();
                    })
                .named("Intaking"));
    StateMachine.State stowState =
        stateMachine.addState(
            Command.noRequirements(
                    (coro) -> {
                      coro.fork(superstructure.setPosition(Superstructure.Positions.STOW));
                      coro.fork(intake.setState(Intake.State.RETRACTED));
                      coro.park();
                    })
                .named("Stow"));
    StateMachine.State handoff =
        stateMachine.addState(
            Command.noRequirements(
                    (coro) -> {
                      coro.fork(indexer.setVoltage(1));
                      coro.fork(intake.setState(Intake.State.RETRACTED));
                      coro.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.waitUntil(superstructure::atSetpoint);

                      coro.waitUntil(indexer::hasCoral);
                      coro.fork(claw.setVoltage(6));
                      coro.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF));
                      coro.waitUntil(claw::hasCoral);
                      coro.fork(superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.park();
                    })
                .named("Handoff"));
    StateMachine.State scoreL2Left =
        stateMachine.addState(
            createScoreCommand(
                    FieldConstants.handleFlip(Poses.BLUE_REEF_LEFT_POSES),
                    Superstructure.Positions.L2_PREP,
                    Superstructure.Positions.L2_SCORE)
                .named("Score L2 Left"));
    StateMachine.State scoreL2Right =
        stateMachine.addState(
            createScoreCommand(
                    FieldConstants.handleFlip(Poses.BLUE_REEF_RIGHT_POSES),
                    Superstructure.Positions.L2_PREP,
                    Superstructure.Positions.L2_SCORE)
                .named("Score L2 Right"));
    StateMachine.State scoreL3Left =
        stateMachine.addState(
            createScoreCommand(
                    FieldConstants.handleFlip(Poses.BLUE_REEF_LEFT_POSES),
                    Superstructure.Positions.L3_PREP,
                    Superstructure.Positions.L3_SCORE)
                .named("Score L2 Left"));
    StateMachine.State scoreL3Right =
        stateMachine.addState(
            createScoreCommand(
                    FieldConstants.handleFlip(Poses.BLUE_REEF_RIGHT_POSES),
                    Superstructure.Positions.L3_PREP,
                    Superstructure.Positions.L3_SCORE)
                .named("Score L3 Right"));
    StateMachine.State scoreL4Left =
        stateMachine.addState(
            createScoreCommand(
                    FieldConstants.handleFlip(Poses.BLUE_REEF_LEFT_POSES),
                    Superstructure.Positions.L4_PREP,
                    Superstructure.Positions.L4_SCORE)
                .named("Score L4 Left"));
    StateMachine.State scoreL4Right =
        stateMachine.addState(
            createScoreCommand(
                    FieldConstants.handleFlip(Poses.BLUE_REEF_RIGHT_POSES),
                    Superstructure.Positions.L4_PREP,
                    Superstructure.Positions.L4_SCORE)
                .named("Score L4 Right"));

    stateMachine.setInitialState(stowState);

    stateMachine.switchFromAny(stowState, intaking).to(handoff).when(indexer::hasCoral);

    stowState.switchTo(intaking).when(intakeButton.and(new Trigger(indexer::hasCoral).negate()));

    stateMachine
        .switchFromAny(stowState, handoff)
        .to(scoreL2Left)
        .when(leftAlignButton.and(() -> selectedLevel == 2).and(claw::hasCoral));
    stateMachine
        .switchFromAny(stowState, handoff)
        .to(scoreL2Right)
        .when(rightAlignButton.and(() -> selectedLevel == 2).and(claw::hasCoral));
    stateMachine
        .switchFromAny(stowState, handoff)
        .to(scoreL3Left)
        .when(leftAlignButton.and(() -> selectedLevel == 3).and(claw::hasCoral));
    stateMachine
        .switchFromAny(stowState, handoff)
        .to(scoreL3Right)
        .when(rightAlignButton.and(() -> selectedLevel == 3).and(claw::hasCoral));
    stateMachine
        .switchFromAny(stowState, handoff)
        .to(scoreL4Left)
        .when(leftAlignButton.and(() -> selectedLevel == 4).and(claw::hasCoral));
    stateMachine
        .switchFromAny(stowState, handoff)
        .to(scoreL4Right)
        .when(rightAlignButton.and(() -> selectedLevel == 4).and(claw::hasCoral));

    stateMachine
        .switchFromAny(
            scoreL2Left, scoreL2Right, scoreL3Left, scoreL3Right, scoreL4Left, scoreL4Right)
        .to(intaking)
        .whenComplete();

    stateMachine
        .switchFromAny(
            scoreL2Left, scoreL2Right, scoreL3Left, scoreL3Right, scoreL4Left, scoreL4Right)
        .to(stowState)
        .when(leftAlignButton.or(rightAlignButton).negate());

    stateMachine
        .switchFromAny()
        .to(stowState)
        .when(stowButton.and(new Trigger(indexer::hasCoral).negate()));

    RobotModeTriggers.teleop().onTrue(stateMachine);
  }

  private NeedsNameBuilderStage createScoreCommand(
      List<Pose2d> poses,
      Superstructure.Positions prepPosition,
      Superstructure.Positions scorePosition) {
    return Command.noRequirements(
        (coro) -> {
          coro.fork(
              new Drive.AutoAlignCommand(
                      drive,
                      () ->
                          drive.getPose().nearest(poses).transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                  .withRunningContinuously(true));
          coro.fork(superstructure.setPosition(prepPosition));
          coro.waitUntil(superstructure::atSetpoint);

          Drive.AutoAlignCommand finalAlign =
              new Drive.AutoAlignCommand(drive, () -> drive.getPose().nearest(poses))
                  .withRunningContinuously(true);
          coro.fork(finalAlign);
          coro.waitUntil(finalAlign::atPosition);

          coro.fork(superstructure.setPosition(scorePosition));
          coro.waitUntil(superstructure::atSetpoint);
          coro.fork(claw.setVoltage(-6));
          coro.wait(Seconds.of(0.25));
        });
  }
}
