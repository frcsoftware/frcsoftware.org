/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import first.robot.simulation.DrivetrainSim;
import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;

public class Drivetrain implements Mechanism {
  private static final int leftLeaderID = 0, rightLeaderID = 2;
  private final TalonFX leftLeader = new TalonFX(leftLeaderID, CANBus.systemcore(0));
  private final TalonFX leftFollower = new TalonFX(1, CANBus.systemcore(0));
  private final TalonFX rightLeader = new TalonFX(rightLeaderID, CANBus.systemcore(0));
  private final TalonFX rightFollower = new TalonFX(3, CANBus.systemcore(0));

  private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
  private final DifferentialDrive differentialDrive =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  private final DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);

  public Drivetrain() {
    var leftConfig = new TalonFXConfiguration();
    leftConfig.MotorOutput.withInverted(InvertedValue.Clockwise_Positive);
    leftLeader.getConfigurator().apply(leftConfig);

    var rightConfig = new TalonFXConfiguration();
    rightConfig.MotorOutput.withInverted(InvertedValue.CounterClockwise_Positive);
    rightLeader.getConfigurator().apply(rightConfig);

    leftFollower.setControl(new Follower(leftLeaderID, MotorAlignmentValue.Aligned));
    rightFollower.setControl(new Follower(rightLeaderID, MotorAlignmentValue.Aligned));

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
    drivetrainSim.periodic();
  }
}
