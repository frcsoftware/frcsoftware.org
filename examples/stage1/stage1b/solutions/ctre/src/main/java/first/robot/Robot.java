/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot;

import first.robot.mechanisms.Drivetrain;
import first.robot.mechanisms.Feeder;
import first.robot.mechanisms.IntakeLauncher;
import first.robot.simulation.FuelSim;
import org.wpilib.command3.Scheduler;
import org.wpilib.framework.OpModeRobot;

/**
 * The methods in this class are called automatically as described in the OpModeRobot documentation.
 * OpMode classes anywhere in the package (or sub-packages) where this class is located are
 * automatically registered to display in the Driver Station. If you change the name of this class
 * or the package after creating this project, you must also update the Main.java file in the
 * project.
 */
public class Robot extends OpModeRobot {
  public final Drivetrain drivetrain = new Drivetrain();
  public final IntakeLauncher intakeLauncher = new IntakeLauncher();
  public final Feeder feeder = new Feeder();

  public Robot() {}

  @Override
  public void robotPeriodic() {
    Scheduler.getDefault().run();
    drivetrain.periodic();
    intakeLauncher.periodic();
    feeder.periodic();
    FuelSim.update();
  }
}
