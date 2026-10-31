/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import static org.wpilib.units.Units.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import first.robot.Robot;
import first.robot.simulation.ElevatorSim;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.math.util.MathUtil;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.*;

public class Elevator {

  private static class Constants {
    static final int LEADER_ID = 20;
    static final int FOLLOWER_ID = 21;
    static final CANBus BUS = new CANBus(CANPort.CAN_S1);

    static final InvertedValue LEADER_INVERSION = InvertedValue.Clockwise_Positive;
    static final MotorAlignmentValue FOLLOWER_ALIGNMENT = MotorAlignmentValue.Opposed;

    static final double GEAR_RATIO =
        (44.0 / 14.0) * (42.0 / 22.0); // Two stage, 14:44, 22:44 reductions
    static final double PULLEY_CIRCUMFERENCE =
        24 * 0.005; // Convert 24t pulley to circumference, in m

    static final double kS = 0;
    static final double kG = 0.294;
    static final double kV = 0.118;
    static final double kA = 0.001;
    static final double kP = 25;
    static final double kI = 0;
    static final double kD = 3.2;
    ;

    static final double POSITION_TOLERANCE = Units.inchesToMeters(0.5);
  }

  private final TalonFX leader = new TalonFX(Constants.LEADER_ID, Constants.BUS);
  private final TalonFX follower = new TalonFX(Constants.FOLLOWER_ID, Constants.BUS);

  private final ElevatorSim sim = new ElevatorSim(leader, follower);

  private final VoltageOut voltageRequest = new VoltageOut(0);
  private final PositionVoltage positionRequest = new PositionVoltage(0);

  private final StatusSignal<Voltage> appliedVoltageSignal = leader.getMotorVoltage();
  private final StatusSignal<Angle> positionSignal = leader.getPosition();
  private final StatusSignal<AngularVelocity> velocitySignal = leader.getVelocity();
  private final StatusSignal<Current> leaderStatorCurrentSignal = leader.getStatorCurrent();
  private final StatusSignal<Current> followerStatorCurrentSignal = follower.getStatorCurrent();
  private final StatusSignal<Current> leaderSupplyCurrentSignal = leader.getSupplyCurrent();
  private final StatusSignal<Current> followerSupplyCurrentSignal = follower.getSupplyCurrent();

  private double setpoint = 0;

  private TelemetryTable table = Telemetry.getTable("Elevator");

  public Elevator() {
    TalonFXConfiguration leaderConfiguration = new TalonFXConfiguration();
    leaderConfiguration
        .MotorOutput
        .withNeutralMode(NeutralModeValue.Brake)
        .withInverted(Constants.LEADER_INVERSION);
    leaderConfiguration.Feedback.withSensorToMechanismRatio(Constants.GEAR_RATIO);
    leaderConfiguration
        .Slot0
        .withKS(Constants.kS)
        .withKG(Constants.kG)
        .withKV(Constants.kV)
        .withKA(Constants.kA)
        .withKP(Constants.kP)
        .withKI(Constants.kI)
        .withKD(Constants.kD)
        .withGravityType(GravityTypeValue.Elevator_Static);

    CurrentLimitsConfigs currentLimitsConfigs = new CurrentLimitsConfigs();
    currentLimitsConfigs
        .withStatorCurrentLimit(60)
        .withStatorCurrentLimitEnable(true)
        .withSupplyCurrentLimit(60)
        .withSupplyCurrentLimitEnable(true);

    leaderConfiguration.withCurrentLimits(currentLimitsConfigs);

    leader.getConfigurator().apply(leaderConfiguration);

    follower.getConfigurator().apply(currentLimitsConfigs);

    follower.setControl(new Follower(leader.getDeviceID(), Constants.FOLLOWER_ALIGNMENT));
  }

  public void periodic() {
    // Update simulation
    if (Robot.isSimulation()) {
      sim.periodic();
    }

    BaseStatusSignal.refreshAll(
        appliedVoltageSignal,
        positionSignal,
        velocitySignal,
        leaderStatorCurrentSignal,
        followerStatorCurrentSignal,
        leaderSupplyCurrentSignal,
        followerSupplyCurrentSignal);

    table.log("Applied Voltage", Volts.of(getAppliedVoltage()));
    table.log("Position", Meters.of(getPosition()));
    table.log("Velocity", MetersPerSecond.of(getVelocity()));
    table.log("At Setpoint", isAtSetpoint());
  }

  /**
   * @param voltage the voltage to apply to the motors
   */
  public void setVoltage(double voltage) {
    leader.setControl(voltageRequest.withOutput(voltage));
  }

  /**
   * @param position the position for the elevator to target, in meters
   */
  public void setPosition(double position) {
    setpoint = position;
    leader.setControl(positionRequest.withPosition(position / Constants.PULLEY_CIRCUMFERENCE));
    table.log("Setpoint", Meters.of(position));
  }

  /**
   * @return the position of the elevator, in meters
   */
  public double getPosition() {
    return positionSignal.getValueAsDouble() * Constants.PULLEY_CIRCUMFERENCE;
  }

  /**
   * @return the velocity of the elevator, in meters per second
   */
  public double getVelocity() {
    return velocitySignal.getValueAsDouble() * Constants.PULLEY_CIRCUMFERENCE;
  }

  /**
   * @return the current voltage being applied to the arm motor
   */
  public double getAppliedVoltage() {
    return appliedVoltageSignal.getValueAsDouble();
  }

  /**
   * @param position the position to compare against, in meters
   * @return whether the elevator is at that position
   */
  public boolean isAtPosition(double position) {
    return MathUtil.isNear(getPosition(), position, Constants.POSITION_TOLERANCE);
  }

  /**
   * @return whether the elevator is at its current setpoint
   */
  public boolean isAtSetpoint() {
    return isAtPosition(setpoint);
  }
}
