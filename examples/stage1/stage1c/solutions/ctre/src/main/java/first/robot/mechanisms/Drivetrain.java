/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import static org.wpilib.units.Units.*;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import first.robot.simulation.DrivetrainSim;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.RobotBase;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;
import org.wpilib.math.controller.PIDController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;

public class Drivetrain implements Mechanism {
  private static final int leftLeaderID = 0, rightLeaderID = 2;
  private final TalonFX leftLeader = new TalonFX(leftLeaderID, new CANBus(CANPort.CAN_S0));
  private final TalonFX leftFollower = new TalonFX(1, new CANBus(CANPort.CAN_S0));
  private final TalonFX rightLeader = new TalonFX(rightLeaderID, new CANBus(CANPort.CAN_S0));
  private final TalonFX rightFollower = new TalonFX(3, new CANBus(CANPort.CAN_S0));

  private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
  private final DifferentialDrive differentialDrive =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  private final DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);

  private static final double WHEEL_CIRCUMFERENCE = Units.inchesToMeters(6.0) * Math.PI;
  private static final double GEAR_RATIO = 10.71;
  private final PIDController driveDistanceController = new PIDController(4, 0, 0.5);
  private final PIDController headingController = new PIDController(1.5, 0, 0.1);

  private final StatusSignal<Angle> leftDistanceSignal = leftLeader.getPosition();
  private final StatusSignal<Angle> rightDistanceSignal = rightLeader.getPosition();
  private final StatusSignal<AngularVelocity> leftVelocitySignal = leftLeader.getVelocity();
  private final StatusSignal<AngularVelocity> rightVelocitySignal = rightLeader.getVelocity();

  public Drivetrain() {
    var leftConfig = new TalonFXConfiguration();
    leftConfig.MotorOutput.withInverted(InvertedValue.Clockwise_Positive);
    leftConfig.Feedback.withSensorToMechanismRatio(GEAR_RATIO);
    leftLeader.getConfigurator().apply(leftConfig);

    var rightConfig = new TalonFXConfiguration();
    rightConfig.MotorOutput.withInverted(InvertedValue.CounterClockwise_Positive);
    rightConfig.Feedback.withSensorToMechanismRatio(GEAR_RATIO);
    rightLeader.getConfigurator().apply(rightConfig);

    leftFollower.setControl(new Follower(leftLeaderID, MotorAlignmentValue.Aligned));
    rightFollower.setControl(new Follower(rightLeaderID, MotorAlignmentValue.Aligned));

    setDefaultCommand(idle());

    driveDistanceController.setTolerance(0.05, 0.05);
    headingController.setTolerance(0.05, 0.2);
    headingController.enableContinuousInput(-Math.PI, Math.PI);
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
                forwardThrottle.getAsDouble(), -rotationThrottle.getAsDouble());
            coroutine.yield();
          }
        })
        .named("Drive");
  }

  public Command driveDistance(double distanceMeters) {
    return run(coroutine -> {
          double startingDistance = (getLeftDistance() + getRightDistance()) / 2;
          double targetDistance = startingDistance + distanceMeters;

          driveDistanceController.setSetpoint(targetDistance);
          driveDistanceController.reset();

          while (!driveDistanceController.atSetpoint()) {
            double currentDistance = (getLeftDistance() + getRightDistance()) / 2;

            double effort = driveDistanceController.calculate(currentDistance);
            differentialDrive.arcadeDrive(effort, 0);

            Telemetry.log("Drive/Drive Distance/Target", targetDistance);
            Telemetry.log("Drive/Drive Distance/Current", currentDistance);
            Telemetry.log("Drive/Drive Distance/Error", driveDistanceController.getError());
            Telemetry.log("Drive/Drive Distance/Effort", effort);

            coroutine.yield();
          }
        })
        .named("Drive Distance " + distanceMeters + "m");
  }

  public Command turnInPlace(Rotation2d target) {
    return run(coroutine -> {
          headingController.setSetpoint(target.getRadians());
          headingController.reset();

          while (!headingController.atSetpoint()) {
            double effort = headingController.calculate(getHeading().getRadians());

            differentialDrive.arcadeDrive(0, effort);

            Telemetry.log("Drive/Turn in Place/Target", target);
            Telemetry.log("Drive/Turn in Place/Current", getHeading());
            Telemetry.log("Drive/Turn in Place/Error", headingController.getError());
            Telemetry.log("Drive/Turn in Place/Effort", effort);

            coroutine.yield();
          }
        })
        .named("Turn in Place to angle " + target.getDegrees() + "deg");
  }

  public void periodic() {
    if (RobotBase.isSimulation()) {
      drivetrainSim.periodic();
    }

    StatusSignal.refreshAll(
        leftDistanceSignal, rightDistanceSignal, leftVelocitySignal, rightVelocitySignal);

    Telemetry.log("Drive/Left Distance", getLeftDistance());
    Telemetry.log("Drive/Left Velocity", getLeftDistance());
    Telemetry.log("Drive/Right Distance", getRightDistance());
    Telemetry.log("Drive/Right Velocity", getRightVelocity());
    Telemetry.log("Drive/Heading", getHeading());
    Telemetry.log(
        "Drive/Active Commands",
        getRunningCommands().stream().map(Command::name).collect(Collectors.toList()).toArray());
  }

  public double getLeftDistance() {
    return leftDistanceSignal.getValue().in(Rotation) * WHEEL_CIRCUMFERENCE;
  }

  public double getRightDistance() {
    return rightDistanceSignal.getValue().in(Rotation) * WHEEL_CIRCUMFERENCE;
  }

  public double getLeftVelocity() {
    return leftVelocitySignal.getValue().in(RotationsPerSecond) * WHEEL_CIRCUMFERENCE;
  }

  public double getRightVelocity() {
    return rightVelocitySignal.getValue().in(RotationsPerSecond) * WHEEL_CIRCUMFERENCE;
  }

  public Rotation2d getHeading() {
    return imu.getRotation2d();
  }

  public void setPose(Pose2d pose) {
    drivetrainSim.setPose(pose);
  }
}
