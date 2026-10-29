// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.driverstation.NiDsXboxController;
import org.wpilib.framework.TimedRobot;

/**
 * The methods in this class are called automatically as described in the OpModeRobot documentation.
 * OpMode classes anywhere in the package (or sub-packages) where this class is located are
 * automatically registered to display in the Driver Station. If you change the name of this class
 * or the package after creating this project, you must also update the Main.java file in the
 * project.
 */
public class Robot extends TimedRobot {

  /** Robot's swerve drive */
  private final SwerveDrivetrain swerve = new SwerveDrivetrain();

  /** Driver's xbox controller */
  private final NiDsXboxController controller = new NiDsXboxController(0);

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {}

  /** This function is called exactly once when the DS first connects. */
  @Override
  public void driverStationConnected() {}

  /* Called periodically (set time interval) while the robot is enabled. */
  @Override
  public void teleopPeriodic() {
    // Use the xbox controller axes to drive the robot
    swerve.driveFieldCentric(
        -controller.getLeftY(), -controller.getLeftX(), -controller.getRightX());
  }

  /** Called periodically, always */
  @Override
  public void robotPeriodic() {
    swerve.periodic();
  }

  /** Called periodically while the robot is in simulation */
  @Override
  public void simulationPeriodic() {
    swerve.simulationPeriodic();
  }
}
