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

  private final StructPublisher<Pose2d> simPosePublisher;

  private final DoublePublisher leftPositionPub;
  private final DoublePublisher rightPositionPub;
  private final DoublePublisher leftVelocityPub;
  private final DoublePublisher rightVelocityPub;

  private final DoublePublisher leftVoltagePub;
  private final DoublePublisher rightVoltagePub;
  private final DoublePublisher leftCurrentPub;
  private final DoublePublisher rightCurrentPub;

  public DrivetrainSim(SparkMax leftSpark, SparkMax rightSpark) {
    this.leftSpark = leftSpark;
    this.rightSpark = rightSpark;

    var table = NetworkTableInstance.getDefault().getTable("Drivetrain");
    this.simPosePublisher = table.getStructTopic("Pose", Pose2d.struct).publish();

    this.leftPositionPub = table.getDoubleTopic("LeftPositionMeters").publish();
    this.rightPositionPub = table.getDoubleTopic("RightPositionMeters").publish();
    this.leftVelocityPub = table.getDoubleTopic("LeftVelocityMPS").publish();
    this.rightVelocityPub = table.getDoubleTopic("RightVelocityMPS").publish();
    this.leftVoltagePub = table.getDoubleTopic("LeftMotorVoltage").publish();
    this.rightVoltagePub = table.getDoubleTopic("RightMotorVoltage").publish();
    this.leftCurrentPub = table.getDoubleTopic("LeftCurrentAmps").publish();
    this.rightCurrentPub = table.getDoubleTopic("RightCurrentAmps").publish();

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
    leftVoltagePub.set(leftMotorVoltage);
    rightVoltagePub.set(rightMotorVoltage);
    leftCurrentPub.set(m_driveSim.getLeftCurrentDraw());
    rightCurrentPub.set(m_driveSim.getRightCurrentDraw());
  }
}
