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
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import first.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.MathUtil;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.simulation.SingleJointedArmSim;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.*;

public class Intake implements Mechanism {
  private static class Constants {
    static final int PIVOT_ID = 30;
    static final int ROLLERS_ID = 31;

    static final CANBus BUS = new CANBus(CANPort.CAN_S1);

    static final InvertedValue PIVOT_INVERSION = InvertedValue.Clockwise_Positive;

    static final double PIVOT_RATIO =
        (60.0 / 16.0)
            * (70.0 / 18.0)
            * (42.0 / 10.0); // Three stage, 60:16, 70:18, 42:10 reductions

    static final double PIVOT_kS = 0;
    static final double PIVOT_kV = 5.621;
    static final double PIVOT_kA = 0.03;
    static final double PIVOT_kP = 100;
    static final double PIVOT_kI = 0;
    static final double PIVOT_kD = 3;

    static final Angle PIVOT_POSITION_TOLERANCE = Degrees.of(5);

    static final Distance PIVOT_ARM_LENGTH = Inches.of(5.2692);
    static final Mass PIVOT_ARM_MASS = Pounds.of(12.296);
    static final Angle PIVOT_ARM_MIN_ANGLE = Degrees.of(-3.206);
    static final Angle PIVOT_ARM_MAX_ANGLE = Degrees.of(138.469);

    static final InvertedValue ROLLERS_INVERSION = InvertedValue.Clockwise_Positive;

    static final double ROLLERS_RATIO = (42.0 / 12.0); // Single stage, 42:12 reduction

    static final double ROLLERS_J = 0.0002;
  }

  private final TalonFX pivot = new TalonFX(Constants.PIVOT_ID, Constants.BUS);
  private final TalonFX rollers = new TalonFX(Constants.ROLLERS_ID, Constants.BUS);

  // Sim stuff
  private final TalonFXSimState pivotSimState = pivot.getSimState();
  private final TalonFXSimState rollersSimState = rollers.getSimState();

  private final SingleJointedArmSim pivotSim =
      new SingleJointedArmSim(
          DCMotor.getKrakenX44(1),
          Constants.PIVOT_RATIO,
          SingleJointedArmSim.estimateMOI(
              Constants.PIVOT_ARM_LENGTH.in(Meters), Constants.PIVOT_ARM_MASS.in(Kilogram)),
          Constants.PIVOT_ARM_LENGTH.in(Meters),
          Constants.PIVOT_ARM_MIN_ANGLE.in(Radians),
          Constants.PIVOT_ARM_MAX_ANGLE.in(Radians),
          true,
          Constants.PIVOT_ARM_MAX_ANGLE.in(Radians));
  private final FlywheelSim rollersSim =
      new FlywheelSim(
          Models.flywheelFromPhysicalConstants(
              DCMotor.getKrakenX60(1), Constants.ROLLERS_J, Constants.ROLLERS_RATIO),
          DCMotor.getKrakenX60(1));

  private final StatusSignal<Voltage> pivotAppliedVoltageSignal = pivot.getMotorVoltage();
  private final StatusSignal<Current> pivotStatorCurrentSignal = pivot.getStatorCurrent();
  private final StatusSignal<Current> pivotSupplyCurrentSignal = pivot.getSupplyCurrent();
  private final StatusSignal<Angle> pivotPositionSignal = pivot.getPosition();
  private final StatusSignal<AngularVelocity> pivotVelocitySignal = pivot.getVelocity();

  private final StatusSignal<Voltage> rollersAppliedVoltageSignal = rollers.getMotorVoltage();
  private final StatusSignal<Current> rollersStatorCurrentSignal = rollers.getStatorCurrent();
  private final StatusSignal<Current> rollersSupplyCurrentSignal = rollers.getSupplyCurrent();
  private final StatusSignal<AngularVelocity> rollersVelocitySignal = rollers.getVelocity();

  private final VoltageOut pivotVoltageRequest = new VoltageOut(0);
  private final PositionVoltage pivotPositionRequest = new PositionVoltage(0);
  private final VoltageOut rollersVoltageRequest = new VoltageOut(0);

  private final TelemetryTable table = Telemetry.getTable("Intake");
  private final TelemetryTable pivotTable = table.getTable("Pivot");
  private final TelemetryTable rollersTable = table.getTable("Rollers");

  public Intake() {
    TalonFXConfiguration pivotConfiguration = new TalonFXConfiguration();
    pivotConfiguration
        .MotorOutput
        .withNeutralMode(NeutralModeValue.Brake)
        .withInverted(Constants.PIVOT_INVERSION);
    pivotConfiguration.Feedback.withSensorToMechanismRatio(Constants.PIVOT_RATIO);
    pivotConfiguration
        .Slot0
        .withKS(Constants.PIVOT_kS)
        .withKV(Constants.PIVOT_kV)
        .withKA(Constants.PIVOT_kA)
        .withKP(Constants.PIVOT_kP)
        .withKI(Constants.PIVOT_kI)
        .withKD(Constants.PIVOT_kD);

    CurrentLimitsConfigs currentLimitsConfigs = new CurrentLimitsConfigs();
    currentLimitsConfigs
        .withStatorCurrentLimit(60)
        .withStatorCurrentLimitEnable(true)
        .withSupplyCurrentLimit(60)
        .withSupplyCurrentLimitEnable(true);

    pivotConfiguration.withCurrentLimits(currentLimitsConfigs);

    pivot.getConfigurator().apply(pivotConfiguration);

    TalonFXConfiguration rollersConfiguration = new TalonFXConfiguration();
    rollersConfiguration.withCurrentLimits(currentLimitsConfigs);
    rollersConfiguration
        .MotorOutput
        .withNeutralMode(NeutralModeValue.Brake)
        .withInverted(Constants.ROLLERS_INVERSION);

    rollers.getConfigurator().apply(rollersConfiguration);

    // Setup sim

    pivotSimState.Orientation = ChassisReference.Clockwise_Positive;
    pivotSimState.setMotorType(TalonFXSimState.MotorType.KrakenX44);

    rollersSimState.setMotorType(TalonFXSimState.MotorType.KrakenX60);
  }

  public void periodic() {
    // Update simulation
    if (Robot.isSimulation()) {
      simPeriodic();
    }

    BaseStatusSignal.refreshAll(
        pivotAppliedVoltageSignal,
        pivotStatorCurrentSignal,
        pivotSupplyCurrentSignal,
        pivotPositionSignal,
        pivotVelocitySignal,
        rollersAppliedVoltageSignal,
        rollersStatorCurrentSignal,
        rollersSupplyCurrentSignal,
        rollersVelocitySignal);

    pivotTable.log("Applied Voltage", getPivotAppliedVoltage());
    pivotTable.log("Stator Current", pivotStatorCurrentSignal.getValue());
    pivotTable.log("Supply Current", pivotSupplyCurrentSignal.getValue());
    pivotTable.log("Target Position", pivotPositionRequest.getPositionMeasure());
    pivotTable.log("Position", getPivotPosition());
    pivotTable.log("Velocity", getPivotVelocity());
    pivotTable.log("At Setpoint", isPivotAtSetpoint());
    pivotTable.log("Error", pivot.getClosedLoopError().getValue());

    rollersTable.log("Applied Voltage", getRollersAppliedVoltage());
    rollersTable.log("Stator Current", rollersStatorCurrentSignal.getValue());
    rollersTable.log("Supply Current", rollersSupplyCurrentSignal.getValue());
    rollersTable.log("Velocity", getRollersVelocity());

    table.log("Active Commands", getRunningCommands().toString());
  }

  public void simPeriodic() {
    pivotSim.setInputVoltage(Math.clamp(pivotSimState.getMotorVoltage(), -12, 12));
    pivotSim.update(0.02);

    pivotSimState.setRawRotorPosition(
        Units.radiansToRotations(pivotSim.getAngle()) * Constants.PIVOT_RATIO);
    pivotSimState.setRotorVelocity(
        Units.radiansToRotations(pivotSim.getVelocity()) * Constants.PIVOT_RATIO);

    rollersSim.setInputVoltage(rollersSimState.getMotorVoltage());
    rollersSim.update(0.02);

    rollersSimState.setRotorVelocity(
        Units.radiansToRotations(rollersSim.getAngularVelocity()) * Constants.ROLLERS_RATIO);
  }

  public Command setState(Voltage rollerVoltage, Angle pivotPosition) {
    return run(coroutine -> {
          rollers.setControl(rollersVoltageRequest.withOutput(rollerVoltage));
          pivot.setControl(pivotPositionRequest.withPosition(pivotPosition));
          coroutine.park();
        })
        .named("Set Intake State");
  }

  public Command setState(State state) {
    return setState(state.rollerVoltage, state.pivotPosition);
  }

  /**
   * @return the current position of the pivot, in rotations
   */
  public Angle getPivotPosition() {
    return pivotPositionSignal.getValue();
  }

  /**
   * @return the current velocity of the pivot, in rotations per second
   */
  public AngularVelocity getPivotVelocity() {
    return pivotVelocitySignal.getValue();
  }

  /**
   * @return the current voltage being applied to the pivot motor
   */
  public Voltage getPivotAppliedVoltage() {
    return pivotAppliedVoltageSignal.getValue();
  }

  /**
   * @return the current velocity of the pivot, in rotations per second
   */
  public AngularVelocity getRollersVelocity() {
    return rollersVelocitySignal.getValue();
  }

  /**
   * @return the current voltage being applied to the pivot motor
   */
  public Voltage getRollersAppliedVoltage() {
    return rollersAppliedVoltageSignal.getValue();
  }

  /**
   * @param position the position to compare against, in rotations
   * @return whether the arm is at that positoin
   */
  public boolean isPivotAtPosition(Angle position) {
    return MathUtil.isNear(
        getPivotPosition().in(Rotations),
        position.in(Rotations),
        Constants.PIVOT_POSITION_TOLERANCE.in(Rotations));
  }

  /**
   * @return whether the arm is at its current setpoint
   */
  public boolean isPivotAtSetpoint() {
    return isPivotAtPosition(pivotPositionRequest.getPositionMeasure());
  }

  public enum State {
    RETRACTED(Volts.of(0), Constants.PIVOT_ARM_MAX_ANGLE),
    INTAKING(Volts.of(12), Constants.PIVOT_ARM_MIN_ANGLE),
    OUTTAKING(Volts.of(-12), Constants.PIVOT_ARM_MIN_ANGLE);

    private final Voltage rollerVoltage;
    private final Angle pivotPosition;

    State(Voltage rollerVoltage, Angle pivotPosition) {
      this.rollerVoltage = rollerVoltage;
      this.pivotPosition = pivotPosition;
    }
  }
}
