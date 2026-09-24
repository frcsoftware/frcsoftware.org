/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import com.revrobotics.spark.SparkMax;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructPublisher;
import org.wpilib.simulation.DifferentialDrivetrainSim;
import org.wpilib.simulation.OnboardIMUSim;

public class DrivetrainSim {

  private final SparkMax leftSpark, rightSpark;

  private final double kGearRatio = 10.71;
  private final double kWheelRadiusMeters = 0.0762; // 3 inches
  private final double linearToMotorRatio = (1.0 / kWheelRadiusMeters) * kGearRatio;
  private static final double kBusVoltage = 12.0;

  private final DifferentialDrivetrainSim m_driveSim =
      new DifferentialDrivetrainSim(
          DCMotor.getNEO(2), // 2 NEO motors on each side of the drivetrain.
          kGearRatio,
          2.1, // MOI of 2.1 kg m^2 (from CAD model).
          26.5, // Mass of the robot is 26.5 kg.
          kWheelRadiusMeters, // Robot uses 3" radius (6" diameter) wheels.
          0.546, // Distance between wheels in meters.
          null);

  // we add front slashes here so that the keys show up consistently between the CTRE and REV
  // examples.
  private final StructPublisher<Pose2d> simPosePublisher =
      NetworkTableInstance.getDefault().getStructTopic("/Drivetrain/Pose", Pose2d.struct).publish();

  private final DoublePublisher leftPositionPub =
      NetworkTableInstance.getDefault().getDoubleTopic("/Drivetrain/LeftPositionMeters").publish();
  private final DoublePublisher rightPositionPub =
      NetworkTableInstance.getDefault().getDoubleTopic("/Drivetrain/RightPositionMeters").publish();
  private final DoublePublisher leftVelocityPub =
      NetworkTableInstance.getDefault().getDoubleTopic("/Drivetrain/LeftVelocityMPS").publish();
  private final DoublePublisher rightVelocityPub =
      NetworkTableInstance.getDefault().getDoubleTopic("/Drivetrain/RightVelocityMPS").publish();

  private final DoublePublisher leftMotorVelocityPub =
      NetworkTableInstance.getDefault()
          .getDoubleTopic("/Drivetrain/LeftMotor/MotorVelocityRPS")
          .publish();
  private final DoublePublisher rightMotorVelocityPub =
      NetworkTableInstance.getDefault()
          .getDoubleTopic("/Drivetrain/RightMotor/MotorVelocityRPS")
          .publish();
  private final DoublePublisher leftMotorVoltagePub =
      NetworkTableInstance.getDefault()
          .getDoubleTopic("/Drivetrain/LeftMotor/MotorVoltage")
          .publish();
  private final DoublePublisher rightMotorVoltagePub =
      NetworkTableInstance.getDefault()
          .getDoubleTopic("/Drivetrain/RightMotor/MotorVoltage")
          .publish();
  private final DoublePublisher leftMotorSupplyCurrentPub =
      NetworkTableInstance.getDefault()
          .getDoubleTopic("/Drivetrain/LeftMotor/MotorSupplyCurrent")
          .publish();
  private final DoublePublisher rightMotorSupplyCurrentPub =
      NetworkTableInstance.getDefault()
          .getDoubleTopic("/Drivetrain/RightMotor/MotorSupplyCurrent")
          .publish();

  public DrivetrainSim(SparkMax leftSpark, SparkMax rightSpark) {
    this.leftSpark = leftSpark;
    this.rightSpark = rightSpark;

    m_driveSim.setPose(new Pose2d(2.5, 2, Rotation2d.ZERO));
    FuelSim.robotPoseSupplier = m_driveSim::getPose;
  }

  public void periodic() {
    double leftMotorVoltage = leftSpark.getThrottle() * kBusVoltage;
    double rightMotorVoltage = rightSpark.getThrottle() * kBusVoltage;

    m_driveSim.setInputs(leftMotorVoltage, rightMotorVoltage);
    m_driveSim.update(0.02);

    OnboardIMUSim.setYaw(m_driveSim.getHeading().getRadians());

    simPosePublisher.set(m_driveSim.getPose());
    leftPositionPub.set(m_driveSim.getLeftPosition());
    rightPositionPub.set(m_driveSim.getRightPosition());
    leftVelocityPub.set(m_driveSim.getLeftVelocity());
    rightVelocityPub.set(m_driveSim.getRightVelocity());

    leftMotorVelocityPub.set(m_driveSim.getLeftVelocity() * linearToMotorRatio);
    rightMotorVelocityPub.set(m_driveSim.getRightVelocity() * linearToMotorRatio);
    leftMotorVoltagePub.set(leftMotorVoltage);
    rightMotorVoltagePub.set(rightMotorVoltage);
    leftMotorSupplyCurrentPub.set(m_driveSim.getLeftCurrentDraw());
    rightMotorSupplyCurrentPub.set(m_driveSim.getRightCurrentDraw());
  }
}
