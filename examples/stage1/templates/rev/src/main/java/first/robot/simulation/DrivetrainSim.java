/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.sim.SparkRelativeEncoderSim;
import static org.wpilib.units.Units.*;

import com.revrobotics.spark.SparkMax;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.DifferentialDrivetrainSim;
import org.wpilib.simulation.OnboardIMUSim;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;

public class DrivetrainSim {

  private final SparkMax leftSpark, rightSpark;
  private final SparkMaxSim leftSparkSim, rightSparkSim;
  private final SparkRelativeEncoderSim leftEncoderSim, rightEncoderSim;

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

  private final TelemetryTable table = Telemetry.getTable("Drivetrain");
  private final TelemetryTable leftMotorTable = table.getTable("LeftMotor");
  private final TelemetryTable rightMotorTable = table.getTable("RightMotor");

  public DrivetrainSim(SparkMax leftSpark, SparkMax rightSpark) {
    this.leftSpark = leftSpark;
    this.rightSpark = rightSpark;

    this.leftSparkSim = new SparkMaxSim(leftSpark, DCMotor.getNEO(2));
    this.rightSparkSim = new SparkMaxSim(rightSpark, DCMotor.getNEO(2));

    this.leftEncoderSim = leftSparkSim.getRelativeEncoderSim();
    this.rightEncoderSim = rightSparkSim.getRelativeEncoderSim();

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

    leftEncoderSim.setPosition(
        Units.radiansToRotations(m_driveSim.getLeftPosition() / kWheelRadiusMeters * kGearRatio));
    leftEncoderSim.setVelocity(
        Units.radiansPerSecondToRotationsPerMinute(
            m_driveSim.getLeftVelocity() / kWheelRadiusMeters * kGearRatio));
    rightEncoderSim.setPosition(
        Units.radiansToRotations(m_driveSim.getRightPosition() / kWheelRadiusMeters * kGearRatio));
    rightEncoderSim.setVelocity(
        Units.radiansPerSecondToRotationsPerMinute(
            m_driveSim.getRightVelocity() / kWheelRadiusMeters * kGearRatio));

    table.log("Pose", m_driveSim.getPose(), Pose2d.struct);
    table.log("LeftPosition", Meters.of(m_driveSim.getLeftPosition()));
    table.log("RightPosition", Meters.of(m_driveSim.getRightPosition()));
    table.log("LeftVelocity", MetersPerSecond.of(m_driveSim.getLeftVelocity()));
    table.log("RightVelocity", MetersPerSecond.of(m_driveSim.getRightVelocity()));

    leftMotorTable.log(
        "MotorVelocity", RadiansPerSecond.of(m_driveSim.getLeftVelocity() * linearToMotorRatio));
    rightMotorTable.log(
        "MotorVelocity", RadiansPerSecond.of(m_driveSim.getRightVelocity() * linearToMotorRatio));
    leftMotorTable.log("MotorVoltage", Volts.of(leftMotorVoltage));
    rightMotorTable.log("MotorVoltage", Volts.of(rightMotorVoltage));
    leftMotorTable.log("MotorSupplyCurrent", Amps.of(m_driveSim.getLeftCurrentDraw()));
    rightMotorTable.log("MotorSupplyCurrent", Amps.of(m_driveSim.getRightCurrentDraw()));
  }
  
  public void setPose(Pose2d pose) {
    m_driveSim.setPose(pose);
  }
}
