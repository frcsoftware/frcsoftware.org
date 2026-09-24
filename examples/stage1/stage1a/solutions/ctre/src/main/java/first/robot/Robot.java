/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
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
  private final int leftLeaderID = 0;
  public TalonFX leftLeader = new TalonFX(leftLeaderID, CANBus.systemcore(0));
  private TalonFX leftFollower = new TalonFX(1, CANBus.systemcore(0));
  // [/DriveMotorsLeft]

  // [DriveMotorsRight]
  private final int rightLeaderID = 2;
  public TalonFX rightLeader = new TalonFX(rightLeaderID, CANBus.systemcore(0));
  private TalonFX rightFollower = new TalonFX(3, CANBus.systemcore(0));
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
  public TalonFX intakeLauncher = new TalonFX(4, CANBus.systemcore(0));
  public TalonFX feeder = new TalonFX(5, CANBus.systemcore(0));
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
    var leftConfig = new TalonFXConfiguration();
    // [/MotorConfigCreationLeft]
    // [MotorConfigSetLeft]
    leftConfig.MotorOutput.withInverted(InvertedValue.Clockwise_Positive);
    // [/MotorConfigSetLeft]
    // [MotorConfigLeft]
    leftLeader.getConfigurator().apply(leftConfig);
    leftFollower.getConfigurator().apply(leftConfig);

    leftFollower.setControl(new Follower(leftLeaderID, MotorAlignmentValue.Aligned));
    // [/MotorConfigLeft]

    // [MotorConfig]
    var rightConfig = new TalonFXConfiguration();
    rightConfig.MotorOutput.withInverted(InvertedValue.CounterClockwise_Positive);
    rightLeader.getConfigurator().apply(rightConfig);
    rightFollower.getConfigurator().apply(rightConfig);

    rightFollower.setControl(new Follower(rightLeaderID, MotorAlignmentValue.Aligned));
    // [/MotorConfig]
  }

  // [/AllConfigs]

  // [DriveSimPeriodic]
  @Override
  public void simulationPeriodic() {
    drivetrainSim.periodic();
    // [/DriveSimPeriodic]
    // [MotorSimPeriodic]
    intakeLauncherSim.periodic();
    feederSim.periodic();
    // [/MotorSimPeriodic]

    // [FuelSimPeriodic]
    FuelSim.periodic();
    // [/FuelSimPeriodic]
  }
  // [/RobotWithSimPart2]
  // [/FullRobot]
}
