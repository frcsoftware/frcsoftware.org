/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import first.robot.simulation.DrivetrainSim;
import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.RobotBase;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;

public class Drivetrain implements Mechanism {
  private final SparkMax leftLeader = new SparkMax(CANPort.CAN_S0, 0, MotorType.kBrushless);
  private final SparkMax leftFollower = new SparkMax(CANPort.CAN_S0, 1, MotorType.kBrushless);
  private final SparkMax rightLeader = new SparkMax(CANPort.CAN_S0, 2, MotorType.kBrushless);
  private final SparkMax rightFollower = new SparkMax(CANPort.CAN_S0, 3, MotorType.kBrushless);

  private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
  private final DifferentialDrive differentialDrive =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  private final DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);

  public Drivetrain() {
    var leftConfig = new SparkMaxConfig().inverted(true);
    leftLeader.configure(
        leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(
        leftConfig.follow(leftLeader),
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);

    var rightConfig = new SparkMaxConfig().inverted(false);
    rightLeader.configure(
        rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightFollower.configure(
        rightConfig.follow(rightLeader),
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);

    setDefaultCommand(idle());
  }

  public Command idle() {
    return run(coroutine -> {
          while (true) {
            differentialDrive.arcadeDrive(0.0, 0.0);
            coroutine.yield();
          }
        })
        .named("Idle");
  }

  public Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
    return run(coroutine -> {
          while (true) {
            differentialDrive.arcadeDrive(
                forwardThrottle.getAsDouble(), rotationThrottle.getAsDouble());
            coroutine.yield();
          }
        })
        .named("Drive");
  }

  public void periodic() {
    if (RobotBase.isSimulation()) {
      drivetrainSim.periodic();
    }
  }
}
