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
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import first.robot.Robot;
import first.robot.simulation.ArmSim;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.math.util.MathUtil;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public class Arm {

  private static class Constants {
    static final int MOTOR_ID = 22;
    static final CANBus BUS = new CANBus(CANPort.CAN_S1);

    static final InvertedValue MOTOR_INVERSION = InvertedValue.Clockwise_Positive;

    static final double GEAR_RATIO =
        (56.0 / 16.0) * (84.0 / 10.0); // Two stage, 56:16, 84:10 reductions

    static final double kS = 0;
    static final double kG = 0.78; // 0.756
    static final double kV = 3.464;
    static final double kA = 0.192;
    static final double kP = 40;
    static final double kI = 0;
    static final double kD = 6;

    static final double POSITION_TOLERANCE = Units.degreesToRotations(3);
  }

  private final TalonFX motor = new TalonFX(Constants.MOTOR_ID, Constants.BUS);
  ;
  private final ArmSim sim = new ArmSim(motor);

  private final VoltageOut voltageRequest = new VoltageOut(0);
  private final PositionVoltage positionRequest = new PositionVoltage(0);

  private final StatusSignal<Voltage> appliedVoltageSignal = motor.getMotorVoltage();
  private final StatusSignal<Angle> positionSignal = motor.getPosition();
  private final StatusSignal<AngularVelocity> velocitySignal = motor.getVelocity();
  private final StatusSignal<Current> statorCurrentSignal = motor.getStatorCurrent();
  private final StatusSignal<Current> supplyCurrentSignal = motor.getSupplyCurrent();

  private double setpoint = 0;

  private final TelemetryTable table = Telemetry.getTable("Arm");

  public Arm() {
    TalonFXConfiguration configuration = new TalonFXConfiguration();
    configuration.MotorOutput.withNeutralMode(NeutralModeValue.Brake)
        .withInverted(Constants.MOTOR_INVERSION);
    configuration.Feedback.withSensorToMechanismRatio(Constants.GEAR_RATIO);
    configuration.Slot0.withKS(Constants.kS)
        .withKG(Constants.kG)
        .withKV(Constants.kV)
        .withKA(Constants.kA)
        .withKP(Constants.kP)
        .withKI(Constants.kI)
        .withKD(Constants.kD)
        .withGravityType(GravityTypeValue.Arm_Cosine);

    CurrentLimitsConfigs currentLimitsConfigs = new CurrentLimitsConfigs();
    currentLimitsConfigs
        .withStatorCurrentLimit(60)
        .withStatorCurrentLimitEnable(true)
        .withSupplyCurrentLimit(60)
        .withSupplyCurrentLimitEnable(true);

    configuration.withCurrentLimits(currentLimitsConfigs);

    motor.getConfigurator().apply(configuration);
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
        statorCurrentSignal,
        supplyCurrentSignal);

    table.log("Applied Voltage", Volts.of(getAppliedVoltage()));
    table.log("Position", Rotations.of(getPosition()));
    table.log("Velocity", RotationsPerSecond.of(getVelocity()));
    table.log("At Setpoint", isAtSetpoint());
  }

  /**
   * @param voltage the voltage to apply to the motor
   */
  public void setVoltage(double voltage) {
    motor.setControl(voltageRequest.withOutput(voltage));
  }

  /**
   * @param position the position for the arm to target, in rotations
   */
  public void setPosition(double position) {
    setpoint = position;
    motor.setControl(positionRequest.withPosition(position));
    table.log("Setpoint", Rotations.of(position));
  }

  /**
   * @return the current position of the arm, in rotations
   */
  public double getPosition() {
    return positionSignal.getValueAsDouble();
  }

  /**
   * @return the current velocity, in rotations per second
   */
  public double getVelocity() {
    return velocitySignal.getValueAsDouble();
  }

  /**
   * @return the current voltage being applied to the arm motor
   */
  public double getAppliedVoltage() {
    return appliedVoltageSignal.getValueAsDouble();
  }

  /**
   * @param position the position to compare against, in rotations
   * @return whether the arm is at that positoin
   */
  public boolean isAtPosition(double position) {
    return MathUtil.isNear(getPosition(), position, Constants.POSITION_TOLERANCE);
  }

  /**
   * @return whether the arm is at its current setpoint
   */
  public boolean isAtSetpoint() {
    return isAtPosition(setpoint);
  }
}
