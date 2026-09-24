/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import static org.wpilib.units.Units.Inches;
import static org.wpilib.units.Units.KilogramSquareMeters;
import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.ctre.phoenix6.sim.TalonFXSimState.MotorType;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructPublisher;
import org.wpilib.simulation.DifferentialDrivetrainSim;
import org.wpilib.simulation.OnboardIMUSim;

public class DrivetrainSim {

  private final TalonFX leftTalon;
  private final TalonFXSimState leftTalonSim;
  private final TalonFX rightTalon;
  private final TalonFXSimState rightTalonSim;

  private final double kGearRatio = 10.71;
  private final double kWheelRadiusMeters = Inches.of(3.0).in(Meters);
  private final double linearToMotorRatio = (1.0 / kWheelRadiusMeters) * kGearRatio;

  private final DifferentialDrivetrainSim driveSim =
      new DifferentialDrivetrainSim(
          DCMotor.getKrakenX60Foc(2), // 2 Kraken Moters on each side of the drivetrain
          kGearRatio, // Gear ratio between the drive motor and drive wheel
          KilogramSquareMeters.of(2.1).magnitude(), // MOI of the robot (measured from CAD model)
          Kilograms.of(26.5).magnitude(), // Mass of the robot
          kWheelRadiusMeters, // Radius of the drive wheels
          Inches.of(21.5).in(Meters), // Distance between the left and right wheels
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

  /**
   * @param leftTalon the left-side TalonFX motor controller
   * @param rightTalon the left-side TalonFX motor controller
   */
  public DrivetrainSim(TalonFX leftTalon, TalonFX rightTalon) {
    this.leftTalon = leftTalon;
    leftTalonSim = new TalonFXSimState(leftTalon, ChassisReference.Clockwise_Positive);
    leftTalonSim.setMotorType(MotorType.KrakenX60);

    this.rightTalon = rightTalon;
    rightTalonSim = new TalonFXSimState(rightTalon, ChassisReference.CounterClockwise_Positive);
    rightTalonSim.setMotorType(MotorType.KrakenX60);

    driveSim.setPose(new Pose2d(2.5, 2, Rotation2d.ZERO));
    FuelSim.robotPoseSupplier = driveSim::getPose;
  }

  public void periodic() {
    driveSim.setInputs(leftTalonSim.getMotorVoltage(), rightTalonSim.getMotorVoltage());
    driveSim.update(0.02);

    OnboardIMUSim.setYaw(driveSim.getHeading().getRadians());

    leftTalonSim.setSupplyVoltage(12.0);
    rightTalonSim.setSupplyVoltage(12.0);
    leftTalonSim.setRawRotorPosition(Radians.of(driveSim.getLeftPosition() * linearToMotorRatio));
    leftTalonSim.setRotorVelocity(
        RadiansPerSecond.of(driveSim.getLeftVelocity() * linearToMotorRatio));
    rightTalonSim.setRawRotorPosition(Radians.of(driveSim.getRightPosition() * linearToMotorRatio));
    rightTalonSim.setRotorVelocity(
        RadiansPerSecond.of(driveSim.getRightVelocity() * linearToMotorRatio));

    simPosePublisher.set(driveSim.getPose());
    leftPositionPub.set(driveSim.getLeftPosition());
    rightPositionPub.set(driveSim.getRightPosition());
    leftVelocityPub.set(driveSim.getLeftVelocity());
    rightVelocityPub.set(driveSim.getRightVelocity());

    leftMotorVelocityPub.set(leftTalon.getVelocity().getValueAsDouble());
    rightMotorVelocityPub.set(rightTalon.getVelocity().getValueAsDouble());
    leftMotorVoltagePub.set(leftTalon.getMotorVoltage().getValueAsDouble());
    rightMotorVoltagePub.set(rightTalon.getMotorVoltage().getValueAsDouble());
    leftMotorSupplyCurrentPub.set(leftTalon.getSupplyCurrent().getValueAsDouble());
    rightMotorSupplyCurrentPub.set(rightTalon.getSupplyCurrent().getValueAsDouble());
  }
}
