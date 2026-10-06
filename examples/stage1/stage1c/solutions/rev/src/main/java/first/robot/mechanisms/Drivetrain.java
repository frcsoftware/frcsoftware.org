/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.util.Signal;
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

public class Drivetrain implements Mechanism {
  private final SparkMax leftLeader = new SparkMax(CANPort.CAN_S0, 0, MotorType.kBrushless);
  private final SparkMax leftFollower = new SparkMax(CANPort.CAN_S0, 1, MotorType.kBrushless);
  private final SparkMax rightLeader = new SparkMax(CANPort.CAN_S0, 2, MotorType.kBrushless);
  private final SparkMax rightFollower = new SparkMax(CANPort.CAN_S0, 3, MotorType.kBrushless);

  private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
  private final DifferentialDrive differentialDrive =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  private final DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);

  private static final double WHEEL_RADIUS = Units.inchesToMeters(3.0);
  private static final double GEAR_RATIO = 10.71;
  private final PIDController driveDistanceController = new PIDController(4, 0, 0.5);
  private final PIDController headingController = new PIDController(1.5, 0, 0.1);

  private final RelativeEncoder leftEncoder = leftLeader.getEncoder();
  private final RelativeEncoder rightEncoder = rightLeader.getEncoder();

  private final Signal<Double> leftDistanceSignal = leftEncoder.getPosition();
  private final Signal<Double> rightDistanceDignal = rightEncoder.getPosition();
  private final Signal<Double> leftVelocitySignal = leftEncoder.getVelocity();
  private final Signal<Double> rightVelocitySignal = rightEncoder.getVelocity();

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

    Telemetry.log("Drive/Left Distance", getLeftDistance());
    Telemetry.log("Drive/Left Velocity", getLeftVelocity());
    Telemetry.log("Drive/Right Distance", getRightDistance());
    Telemetry.log("Drive/Right Velocity", getRightVelocity());
    Telemetry.log("Drive/Heading", getHeading());
    Telemetry.log(
        "Drive/Active Commands",
        getRunningCommands().stream().map(Command::name).collect(Collectors.toList()).toArray());
  }

  public double getLeftDistance() {
    return Units.rotationsToRadians(leftEncoder.getPosition().get()) / GEAR_RATIO * WHEEL_RADIUS;
  }

  public double getRightDistance() {
    return Units.rotationsToRadians(rightEncoder.getPosition().get()) / GEAR_RATIO * WHEEL_RADIUS;
  }

  public double getLeftVelocity() {
    return Units.rotationsPerMinuteToRadiansPerSecond(leftEncoder.getVelocity().get())
        / GEAR_RATIO
        * WHEEL_RADIUS;
  }

  public double getRightVelocity() {
    return Units.rotationsPerMinuteToRadiansPerSecond(rightEncoder.getVelocity().get())
        / GEAR_RATIO
        * WHEEL_RADIUS;
  }

  public Rotation2d getHeading() {
    return imu.getRotation2d();
  }

  public void setPose(Pose2d pose) {
    drivetrainSim.setPose(pose);
  }
}
