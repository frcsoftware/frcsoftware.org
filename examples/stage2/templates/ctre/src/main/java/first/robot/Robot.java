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
  public final Superstructure superstructure;
  public final Claw claw;
  public final Intake intake;
  public final Indexer indexer;
  public final RobotVisualization robotVisualization;

  public Robot() {
    drive = new Drive();
    superstructure = new Superstructure();
    claw = new Claw();
    intake = new Intake();
    indexer = new Indexer();
    coralSim =
        new CoralSim(
            drive::getPose,
            superstructure::getElevatorPosition,
            superstructure::getArmPosition,
            claw::getAppliedVoltage,
            intake::getPivotPosition,
            intake::getRollersAppliedVoltage,
            indexer::getAppliedVoltage);

    robotVisualization =
        new RobotVisualization(
            drive::getPose,
            superstructure::getElevatorPosition,
            superstructure::getArmPosition,
            intake::getPivotPosition);
  }

  @Override
  public void robotPeriodic() {
    drive.periodic();
    superstructure.periodic();
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
