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
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import first.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.simulation.SingleJointedArmSim;
import org.wpilib.system.Notifier;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public class Intake implements Mechanism {
  private final TalonFX pivot;
  private final TalonFX rollers;

  // Sim stuff
  private final TalonFXSimState pivotSimState;
  private final TalonFXSimState rollersSimState;

  private final SingleJointedArmSim pivotSim;
  private final FlywheelSim rollersSim;

  private double pivotTargetPosition = 0;

  private final StatusSignal<Voltage> pivotAppliedVoltageSignal;
  private final StatusSignal<Current> pivotStatorCurrentSignal;
  private final StatusSignal<Current> pivotSupplyCurrentSignal;
  private final StatusSignal<Angle> pivotPositionSignal;
  private final StatusSignal<AngularVelocity> pivotVelocitySignal;

  private final StatusSignal<Voltage> rollersAppliedVoltageSignal;
  private final StatusSignal<Current> rollersStatorCurrentSignal;
  private final StatusSignal<Current> rollersSupplyCurrentSignal;
  private final StatusSignal<AngularVelocity> rollersVelocitySignal;

  private final BaseStatusSignal[] statusSignals;

  private final VoltageOut pivotVoltageRequest = new VoltageOut(0);
  private final PositionVoltage pivotPositionRequest = new PositionVoltage(0);
  private final VoltageOut rollersVoltageRequest = new VoltageOut(0);

  private final TelemetryTable table = Telemetry.getTable("Intake");
  private final TelemetryTable pivotTable = table.getTable("Pivot");
  private final TelemetryTable rollersTable = table.getTable("Rollers");

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
    static final double PIVOT_kG = 0.349;
    static final double PIVOT_kV = 5.621;
    static final double PIVOT_kA = 0.03;
    //        static final double PIVOT_kP = 19; //92
    static final double PIVOT_kP = 20;
    //    static final double PIVOT_kP = 5;
    static final double PIVOT_kI = 0;
    //        static final double PIVOT_kD = 0.2; //0.5
    static final double PIVOT_kD = 0.5;
    //    static final double PIVOT_kD = 0;

    static final double PIVOT_POSITION_TOLERANCE = Units.degreesToRotations(5);

    static final double PIVOT_ARM_LENGTH = Units.inchesToMeters(5.2692);
    static final double PIVOT_ARM_MASS = Units.lbsToKilograms(12.296);
    static final double PIVOT_ARM_MIN_ANGLE = Units.degreesToRotations(-3.206);
    static final double PIVOT_ARM_MAX_ANGLE = Units.degreesToRotations(138.469);

    static final InvertedValue ROLLERS_INVERSION = InvertedValue.Clockwise_Positive;

    static final double ROLLERS_RATIO = (42.0 / 12.0); // Single stage, 42:12 reduction

    static final double ROLLERS_J = 0.0002;
  }

  public Intake() {
    pivot = new TalonFX(Constants.PIVOT_ID, Constants.BUS);
    rollers = new TalonFX(Constants.ROLLERS_ID, Constants.BUS);

    TalonFXConfiguration pivotConfiguration = new TalonFXConfiguration();
    pivotConfiguration
        .MotorOutput
        .withNeutralMode(NeutralModeValue.Brake)
        .withInverted(Constants.PIVOT_INVERSION);
    pivotConfiguration.Feedback.withSensorToMechanismRatio(Constants.PIVOT_RATIO);
    pivotConfiguration
        .Slot0
        .withKS(Constants.PIVOT_kS)
        //        .withKG(Constants.PIVOT_kG)
        .withKV(Constants.PIVOT_kV)
        .withKA(Constants.PIVOT_kA)
        .withKP(Constants.PIVOT_kP)
        .withKI(Constants.PIVOT_kI)
        .withKD(Constants.PIVOT_kD)
        .withGravityType(GravityTypeValue.Arm_Cosine);

    CurrentLimitsConfigs currentLimitsConfigs = new CurrentLimitsConfigs();
    currentLimitsConfigs
        .withStatorCurrentLimit(60)
        .withStatorCurrentLimitEnable(true)
        .withSupplyCurrentLimit(60)
        .withSupplyCurrentLimitEnable(true);

    pivotConfiguration.withCurrentLimits(currentLimitsConfigs);

    pivot.getConfigurator().apply(pivotConfiguration);

    //    pivot.setPosition(Constants.PIVOT_ARM_MAX_ANGLE); // Set starting position of pivot to
    // fully up
    //    pivot.setPosition(0.38);

    TalonFXConfiguration rollersConfiguration = new TalonFXConfiguration();
    rollersConfiguration.withCurrentLimits(currentLimitsConfigs);
    rollersConfiguration
        .MotorOutput
        .withNeutralMode(NeutralModeValue.Brake)
        .withInverted(Constants.ROLLERS_INVERSION);

    rollers.getConfigurator().apply(rollersConfiguration);

    // Create status signals

    pivotAppliedVoltageSignal = pivot.getMotorVoltage();
    pivotStatorCurrentSignal = pivot.getStatorCurrent();
    pivotSupplyCurrentSignal = pivot.getSupplyCurrent();
    pivotPositionSignal = pivot.getPosition();
    pivotVelocitySignal = pivot.getVelocity();

    rollersAppliedVoltageSignal = rollers.getMotorVoltage();
    rollersStatorCurrentSignal = rollers.getStatorCurrent();
    rollersSupplyCurrentSignal = rollers.getSupplyCurrent();
    rollersVelocitySignal = rollers.getVelocity();

    statusSignals =
        new BaseStatusSignal[] {
          pivotAppliedVoltageSignal,
          pivotStatorCurrentSignal,
          pivotSupplyCurrentSignal,
          pivotPositionSignal,
          pivotVelocitySignal,
          rollersAppliedVoltageSignal,
          rollersStatorCurrentSignal,
          rollersSupplyCurrentSignal,
          rollersVelocitySignal
        };

    // Setup sim

    pivotSimState = pivot.getSimState();
    //        pivotSimState.Orientation = ChassisReference.CounterClockwise_Positive;
    pivotSimState.Orientation = ChassisReference.Clockwise_Positive;
    rollersSimState = rollers.getSimState();

    pivotSim =
        new SingleJointedArmSim(
            DCMotor.getKrakenX44(1),
            Constants.PIVOT_RATIO,
            SingleJointedArmSim.estimateMOI(Constants.PIVOT_ARM_LENGTH, Constants.PIVOT_ARM_MASS),
            Constants.PIVOT_ARM_LENGTH,
            Units.rotationsToRadians(Constants.PIVOT_ARM_MIN_ANGLE),
            Units.rotationsToRadians(Constants.PIVOT_ARM_MAX_ANGLE),
            true,
            Units.rotationsToRadians(Constants.PIVOT_ARM_MAX_ANGLE));

    rollersSim =
        new FlywheelSim(
            Models.flywheelFromPhysicalConstants(
                DCMotor.getKrakenX60(1), Constants.ROLLERS_J, Constants.ROLLERS_RATIO),
            DCMotor.getKrakenX60(1));

    Notifier notifier = new Notifier(this::simPeriodic);

    if (Robot.isSimulation()) {
      notifier.startPeriodic(0.02 / 4);
    }
  }

  public void periodic() {
    // Update simulation
    if (Robot.isSimulation()) {
      //      simPeriodic();
    }

    BaseStatusSignal.refreshAll(statusSignals);

    pivotTable.log("Applied Voltage", Volts.of(getPivotAppliedVoltage()));
    pivotTable.log("Stator Current", Amps.of(pivotStatorCurrentSignal.getValueAsDouble()));
    pivotTable.log("Supply Current", Amps.of(pivotSupplyCurrentSignal.getValueAsDouble()));
    pivotTable.log("Target Position", Rotations.of(pivotTargetPosition));
    pivotTable.log("Position", Rotations.of(getPivotPosition()));
    pivotTable.log("Velocity", RotationsPerSecond.of(getPivotVelocity()));
    pivotTable.log("At Setpoint", isPivotAtSetpoint());
    pivotTable.log("Error", Rotations.of(pivot.getClosedLoopError().getValueAsDouble()));

    rollersTable.log("Applied Voltage", Volts.of(getRollersAppliedVoltage()));
    rollersTable.log("Stator Current", Amps.of(rollersStatorCurrentSignal.getValueAsDouble()));
    rollersTable.log("Supply Current", Amps.of(rollersSupplyCurrentSignal.getValueAsDouble()));
    rollersTable.log("Velocity", RotationsPerSecond.of(getRollersVelocity()));

    table.log("Active Commands", getRunningCommands().toString());
  }

  public void simPeriodic() {
    pivotSim.setInputVoltage(Math.clamp(pivotSimState.getMotorVoltage(), -12, 12));
    pivotSim.update(0.02 / 4);

    pivotSimState.setRawRotorPosition(
        Units.radiansToRotations(pivotSim.getAngle()) * Constants.PIVOT_RATIO);
    pivotSimState.setRotorVelocity(
        Units.radiansToRotations(pivotSim.getVelocity()) * Constants.PIVOT_RATIO);

    pivotTable.log("SimPosition", Radians.of(pivotSim.getAngle()));

    rollersSim.setInputVoltage(rollersSimState.getMotorVoltage());
    rollersSim.update(0.02 / 4);

    rollersSimState.setRotorVelocity(
        Units.radiansToRotations(rollersSim.getAngularVelocity()) * Constants.ROLLERS_RATIO);
  }

  /**
   * @param voltage voltage to set
   * @return a command
   */
  public Command setRollerVoltage(double voltage) {
    return run(coro -> rollers.setControl(rollersVoltageRequest.withOutput(voltage)))
        .named("Set Roller Voltage " + voltage + "V");
  }

  /**
   * @param voltage voltage to set
   * @return a command
   */
  public Command setPivotVoltage(double voltage) {
    return run(coro -> pivot.setControl(pivotVoltageRequest.withOutput(voltage)))
        .named("Set Pivot Voltage " + voltage + "V");
  }

  /**
   * @param position position to target, in rotations
   * @return a command
   */
  public Command setPivotPosition(double position) {
    return run(coro -> {
          pivotTargetPosition = position;
          pivot.setControl(pivotPositionRequest.withPosition(position));
          coro.waitUntil(() -> isPivotAtPosition(position));
        })
        .named("Set Pivot Position " + position + "rot");
  }

  /**
   * @return the current position of the pivot, in rotations
   */
  public double getPivotPosition() {
    return pivotPositionSignal.getValueAsDouble();
  }

  /**
   * @return the current velocity of the pivot, in rotations per second
   */
  public double getPivotVelocity() {
    return pivotVelocitySignal.getValueAsDouble();
  }

  /**
   * @return the current voltage being applied to the pivot motor
   */
  public double getPivotAppliedVoltage() {
    return pivotAppliedVoltageSignal.getValueAsDouble();
  }

  /**
   * @return the current velocity of the pivot, in rotations per second
   */
  public double getRollersVelocity() {
    return rollersVelocitySignal.getValueAsDouble();
  }

  /**
   * @return the current voltage being applied to the pivot motor
   */
  public double getRollersAppliedVoltage() {
    return rollersAppliedVoltageSignal.getValueAsDouble();
  }

  /**
   * @param position the position to compare against, in rotations
   * @return whether the arm is at that positoin
   */
  public boolean isPivotAtPosition(double position) {
    return Math.abs(getPivotPosition() - position) < Constants.PIVOT_POSITION_TOLERANCE;
  }

  /**
   * @return whether the arm is at its current setpoint
   */
  public boolean isPivotAtSetpoint() {
    return isPivotAtPosition(pivotTargetPosition);
  }
}
