// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import first.robot.mechanisms.*;
import first.robot.simulation.CoralSim;
import first.robot.simulation.RobotVisualization;
import org.wpilib.command3.Scheduler;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.math.util.Units;
import org.wpilib.system.Timer;

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
    coralSim = new CoralSim(drive::getPose, elevator::getPosition, arm::getPosition);
    claw = new Claw();
    intake = new Intake();
    indexer = new Indexer();

//    robotVisualization = new RobotVisualization(drive::getPose, elevator::getPosition, arm::getPosition, intake::getPivotPosition);
//    robotVisualization = new RobotVisualization(drive::getPose, () -> 0, () -> 0, () -> 0);
//    robotVisualization = new RobotVisualization(drive::getPose, () -> Math.max(0, Math.sin(Timer.getTimestamp()) * 2), () -> Timer.getTimestamp()/2 * Math.PI, () -> Math.sin(Timer.getTimestamp()) * Math.PI);
    robotVisualization = new RobotVisualization(drive::getPose, elevator::getPosition, arm::getPosition, () -> 0);

    Scheduler.getDefault().addPeriodic(coralSim::periodic);
    Scheduler.getDefault().addPeriodic(robotVisualization::periodic);
  }

  @Override
  public void robotPeriodic() {
    Scheduler.getDefault().run();
  }
}
