/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import first.robot.simulation.DrivetrainSim;
import first.robot.simulation.FuelSim;
import first.robot.simulation.SingleFlywheelSim;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;

// [FullRobot]
// [RobotWithSimPart1]
// [RobotTop]
/**
 * The methods in this class are called automatically as described in the OpModeRobot documentation.
 * OpMode classes anywhere in the package (or sub-packages) where this class is located are
 * automatically registered to display in the Driver Station. If you change the name of this class
 * or the package after creating this project, you must also update the Main.java file in the
 * project.
 */
public class Robot extends OpModeRobot {

  // [DriveMotorsLeft]
  private SparkMax leftLeader = new SparkMax(0, 0, MotorType.kBrushless);
  private SparkMax leftFollower = new SparkMax(0, 1, MotorType.kBrushless);
  // [/DriveMotorsLeft]
  // [DriveMotorsRight]
  private SparkMax rightLeader = new SparkMax(0, 2, MotorType.kBrushless);
  private SparkMax rightFollower = new SparkMax(0, 3, MotorType.kBrushless);
  // [/DriveMotorsRight]

  // [DrivetrainInstance]
  public final DifferentialDrive drivetrain =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);
  // [/DrivetrainInstance]

  // [IMU]
  private OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
  // [/IMU]
  // [/RobotTop]

  // [DrivetrainSim]
  private DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);
  // [/DrivetrainSim]
  // [/RobotWithSimPart1]

  // [AdditionalMotors]
  public SparkMax intakeLauncher = new SparkMax(0, 4, MotorType.kBrushless);
  public SparkMax feeder = new SparkMax(0, 5, MotorType.kBrushless);
  // [/AdditionalMotors]

  // [IntakeLauncherSim]
  private SingleFlywheelSim intakeLauncherSim = SingleFlywheelSim.forIntakeLauncher(intakeLauncher);
  // [/IntakeLauncherSim]
  // [FeederSim]
  private SingleFlywheelSim feederSim = SingleFlywheelSim.forFeeder(feeder);

  // [/FeederSim]

  // [RobotWithSimPart2]
  // [AllConfigs]
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    // [MotorConfigCreationLeft]
    var leftConfig = new SparkMaxConfig();
    // [/MotorConfigCreationLeft]
    // [MotorConfigSetLeft]
    leftConfig.inverted(true);
    // [/MotorConfigSetLeft]
    // [MotorConfigLeft]
    leftLeader.configure(
        leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(
        leftConfig.follow(leftLeader),
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);
    // [/MotorConfigLeft]

    // [MotorConfig]
    var rightConfig = new SparkMaxConfig();
    rightConfig.inverted(false);
    rightLeader.configure(
        rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightFollower.configure(
        rightConfig.follow(rightLeader),
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);
    // [/MotorConfig]
  }

  // [/AllConfigs]

  // [DriveSimPeriodic]
  @Override
  public void simulationPeriodic() {
    drivetrainSim.periodic();
    // [/DriveSimPeriodic]
    intakeLauncherSim.periodic();
    // [MotorSimPeriodic]
    feederSim.periodic();
    // [/MotorSimPeriodic]

    // [FuelSimPeriodic]
    FuelSim.periodic();
    // [/FuelSimPeriodic]
  }
  // [/RobotWithSimPart2]
}
// [/FullRobot]
