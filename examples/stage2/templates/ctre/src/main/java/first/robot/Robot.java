// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import first.robot.mechanisms.*;
import org.wpilib.command3.Scheduler;
import org.wpilib.framework.OpModeRobot;

public class Robot extends OpModeRobot {

  public final Drive drive;
  public final Elevator elevator;
  public final Arm arm;
  public final Superstructure superstructure;
  public final Claw claw;

  public Robot() {
    drive = new Drive();
    elevator = new Elevator();
    arm = new Arm();
    superstructure = new Superstructure(elevator, arm);
    claw = new Claw();
  }

  @Override
  public void robotPeriodic() {
    Scheduler.getDefault().run();
  }
}
