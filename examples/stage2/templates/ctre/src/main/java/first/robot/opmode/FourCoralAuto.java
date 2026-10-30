package first.robot.opmode;

import static org.wpilib.units.Units.Seconds;

import first.robot.Poses;
import first.robot.Robot;
import first.robot.mechanisms.Drive;
import first.robot.mechanisms.Intake;
import first.robot.mechanisms.Superstructure;
import first.robot.simulation.CoralSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.NeedsNameBuilderStage;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.RobotModeTriggers;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;

@Autonomous
public class FourCoralAuto extends PeriodicOpMode {
  private final Robot robot;

  private final Translation2d coralLocation = new Translation2d(7, -3);

  public FourCoralAuto(Robot robot) {
    this.robot = robot;

    robot.drive.setPose(new Pose2d(new Translation2d(1.4, -1.3), Rotation2d.k180deg));
    robot.coralSim.setState(CoralSim.CoralState.CLAW);

    RobotModeTriggers.autonomous()
        .onTrue(
            Command.noRequirements(
                    coro -> {
                      coro.await(
                          createScoreCommand(
                                  Poses.BLUE_REEF_J,
                                  Superstructure.Positions.L4_PREP,
                                  Superstructure.Positions.L4_SCORE)
                              .named("Score J"));

                      coro.await(createDriveTowardsCoralCommand(coralLocation));

                      coro.await(
                          createScoreCommand(
                                  Poses.BLUE_REEF_K,
                                  Superstructure.Positions.L4_PREP,
                                  Superstructure.Positions.L4_SCORE)
                              .named("Score K"));

                      coro.await(createDriveTowardsCoralCommand(coralLocation));

                      coro.await(
                          createScoreCommand(
                                  Poses.BLUE_REEF_L,
                                  Superstructure.Positions.L4_PREP,
                                  Superstructure.Positions.L4_SCORE)
                              .named("Score L"));

                      coro.await(createDriveTowardsCoralCommand(coralLocation));

                      coro.await(
                          createScoreCommand(
                                  Poses.BLUE_REEF_I,
                                  Superstructure.Positions.L4_PREP,
                                  Superstructure.Positions.L4_SCORE)
                              .named("Score I"));

                      coro.await(robot.drive.stop());
                    })
                .named("Simple Auto"));

    new Trigger(robot.indexer::hasCoral)
        .onTrue(
            Command.noRequirements(
                    (coro) -> {
                      coro.fork(robot.indexer.setVoltage(1));
                      coro.await(
                          robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
                      coro.waitUntil(robot.indexer::hasCoral);
                      coro.await(robot.claw.setVoltage(6));
                      coro.await(
                          robot.superstructure.setPosition(Superstructure.Positions.HANDOFF));
                      coro.waitUntil(robot.claw::hasCoral);
                    })
                .named("Handoff Coral"));
  }

  private NeedsNameBuilderStage createScoreCommand(
      Pose2d pose, Superstructure.Positions prepPosition, Superstructure.Positions scorePosition) {
    return Command.noRequirements(
        (coro) -> {
          coro.fork(
              new Drive.AutoAlignCommand(
                      robot.drive, pose.transformBy(Poses.REEF_PREALIGN_TRANSFORM))
                  .withRunningContinuously(true));
          coro.waitUntil(robot.claw::hasCoral);
          coro.await(robot.superstructure.setPosition(prepPosition));

          Drive.AutoAlignCommand finalAlign =
              new Drive.AutoAlignCommand(robot.drive, pose).withRunningContinuously(true);
          coro.fork(finalAlign);
          coro.waitUntil(finalAlign::atPosition);

          coro.fork(robot.superstructure.setPosition(scorePosition));
          coro.wait(Seconds.of(0.25));
          coro.await(robot.claw.setVoltage(-6));
        });
  }

  private Command createDriveTowardsCoralCommand(Translation2d coralLocation) {
    return Command.noRequirements(
            coro -> {
              coro.fork(
                  new Drive.AutoAlignCommand(
                      robot.drive,
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
              coro.fork(robot.intake.setState(Intake.State.INTAKING));
              coro.fork(robot.indexer.setVoltage(12));
              coro.fork(robot.superstructure.setPosition(Superstructure.Positions.HANDOFF_PREP));
              coro.waitUntil(robot.indexer::hasCoral);
            })
        .named("Intake Coral");
  }
}
