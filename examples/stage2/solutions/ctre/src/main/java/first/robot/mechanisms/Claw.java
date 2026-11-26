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
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import first.robot.Robot;
import first.robot.simulation.ClawSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.hardware.discrete.DigitalInput;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public class Claw implements Mechanism {
  private static class Constants {
    static final int MOTOR_ID = 23;
    static final CANBus BUS = new CANBus(CANPort.CAN_S2);

    static final InvertedValue MOTOR_INVERSION = InvertedValue.Clockwise_Positive;

    static final NeutralModeValue NEUTRAL_MODE = NeutralModeValue.Brake;

    static final int SENSOR_ID = 1;
  }

  private final TalonFX motor = new TalonFX(Constants.MOTOR_ID, Constants.BUS);
  private final DigitalInput sensor = new DigitalInput(Constants.SENSOR_ID);

  private final ClawSim sim = new ClawSim(motor);

  private final VoltageOut voltageRequest = new VoltageOut(0);

  private final StatusSignal<Voltage> appliedVoltageSignal = motor.getMotorVoltage();
  private final StatusSignal<Current> statorCurrentSignal = motor.getStatorCurrent();
  private final StatusSignal<Current> supplyCurrentSignal = motor.getSupplyCurrent();

  private final TelemetryTable table = Telemetry.getTable("Claw");

  public Claw() {
    TalonFXConfiguration configuration = new TalonFXConfiguration();
    configuration.MotorOutput.withInverted(Constants.MOTOR_INVERSION)
        .withNeutralMode(Constants.NEUTRAL_MODE);
    motor.getConfigurator().apply(configuration);
  }

  public void periodic() {
    // Update simulation
    if (Robot.isSimulation()) {
      sim.periodic();
    }

    BaseStatusSignal.refreshAll(appliedVoltageSignal, statorCurrentSignal, supplyCurrentSignal);

    table.log("Applied Voltage", getAppliedVoltage());
    table.log("Stator Current", statorCurrentSignal.getValue());
    table.log("Supply Current", supplyCurrentSignal.getValue());
    table.log("Active Commands", getRunningCommands().toString());

    table.log("Has Coral", hasCoral());
  }

  /**
   * @param voltage the voltage to apply to the motor
   * @return a command
   */
  public Command setVoltage(Voltage voltage) {
    return run((coroutine) -> {
          motor.setControl(voltageRequest.withOutput(voltage));
          coroutine.park();
        })
        .named("Set Voltage: " + voltage.in(Volts) + "V");
  }

  /**
   * @param voltage the voltage to apply to the motor
   * @return a command
   */
  public Command setVoltage(Voltages voltage) {
    return setVoltage(voltage.voltage);
  }

  public boolean hasCoral() {
    return sensor.get();
  }

  public Voltage getAppliedVoltage() {
    return appliedVoltageSignal.getValue();
  }

  public enum Voltages {
    OFF(Volts.of(0)),
    HOLD_CORAL(Volts.of(6)),
    SCORE_CORAL(Volts.of(-6)),
    ;
    private final Voltage voltage;

    Voltages(Voltage voltage) {
      this.voltage = voltage;
    }
  }
}
