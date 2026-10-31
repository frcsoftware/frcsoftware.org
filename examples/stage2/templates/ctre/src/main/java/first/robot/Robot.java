/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot;

import first.robot.mechanisms.*;
import first.robot.simulation.CoralSim;
import first.robot.simulation.RobotVisualization;
import org.wpilib.command3.Scheduler;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.telemetry.Telemetry;

public class Robot extends OpModeRobot {

  public final CoralSim coralSim;

  public final Drive drive;
  public final Elevator elevator;
  public final Arm arm;
  public final Superstructure superstructure;
  public final Claw claw;
  public final Intake intake;
  public final Indexer indexer;
  public final RobotVisualization robotVisualization;

  public Robot() {
    drive = new Drive();
    elevator = new Elevator();
    arm = new Arm();
    superstructure = new Superstructure(elevator, arm);
    claw = new Claw();
    intake = new Intake();
    indexer = new Indexer();
    coralSim =
        new CoralSim(
            drive::getPose,
            elevator::getPosition,
            arm::getPosition,
            claw::getAppliedVoltage,
            intake::getPivotPosition,
            intake::getRollersAppliedVoltage,
            indexer::getAppliedVoltage);

    robotVisualization =
        new RobotVisualization(
            drive::getPose, elevator::getPosition, arm::getPosition, intake::getPivotPosition);
    //    robotVisualization = new RobotVisualization(drive::getPose, () -> 0, () -> 0, () -> 0);
    //    robotVisualization = new RobotVisualization(drive::getPose, () -> Math.max(0,
    // Math.sin(Timer.getTimestamp()) * 2), () -> Timer.getTimestamp()/2 * Math.PI, () ->
    // Math.sin(Timer.getTimestamp()) * Math.PI);
    //    robotVisualization =
    //        new RobotVisualization(drive::getPose, elevator::getPosition, arm::getPosition, () ->
    // 0);
  }

  @Override
  public void robotPeriodic() {
    drive.periodic();
    elevator.periodic();
    arm.periodic();
    claw.periodic();
    intake.periodic();
    indexer.periodic();

    robotVisualization.periodic();

    Scheduler.getDefault().run();

    Telemetry.log("Scheduler", Scheduler.getDefault());
  }

  @Override
  public void simulationPeriodic() {
    coralSim.periodic();
  }
}
