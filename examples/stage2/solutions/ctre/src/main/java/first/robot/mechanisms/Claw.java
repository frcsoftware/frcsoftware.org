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
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
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
  private final TalonFX motor = new TalonFX(23, new CANBus(CANPort.CAN_S2));
  private final DigitalInput sensor = new DigitalInput(1);

  private final ClawSim sim = new ClawSim(motor);

  private final VoltageOut voltageRequest = new VoltageOut(0);

  private final StatusSignal<Voltage> appliedVoltageSignal = motor.getMotorVoltage();
  private final StatusSignal<Current> statorCurrentSignal = motor.getStatorCurrent();
  private final StatusSignal<Current> supplyCurrentSignal = motor.getSupplyCurrent();

  private final TelemetryTable table = Telemetry.getTable("Claw");

  public void periodic() {
    // Update simulation
    if (Robot.isSimulation()) {
      sim.periodic();
    }

    BaseStatusSignal.refreshAll(appliedVoltageSignal, statorCurrentSignal, supplyCurrentSignal);

    table.log("Applied Voltage", Volts.of(getAppliedVoltage()));
    table.log("Stator Current", Amps.of(statorCurrentSignal.getValueAsDouble()));
    table.log("Supply Current", Amps.of(supplyCurrentSignal.getValueAsDouble()));
    table.log("Active Commands", getRunningCommands().toString());

    table.log("Has Coral", hasCoral());
  }

  /**
   * @param voltage the voltage to apply to the motors
   * @return a command
   */
  public Command setVoltage(double voltage) {
    return run((coro) -> {
          motor.setControl(voltageRequest.withOutput(voltage));
          coro.park();
        })
        .named("Set Voltage: " + voltage + "V");
  }

  public boolean hasCoral() {
    return sensor.get();
  }

  public double getAppliedVoltage() {
    return appliedVoltageSignal.getValueAsDouble();
  }
}
