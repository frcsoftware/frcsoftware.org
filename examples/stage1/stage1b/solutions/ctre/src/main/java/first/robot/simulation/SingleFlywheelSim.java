/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.ctre.phoenix6.sim.TalonFXSimState.MotorType;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;

public class SingleFlywheelSim {

  private final TalonFX talonMotor;
  private final TalonFXSimState talonMotorSim;
  private double motorPosition = 0.0;

  private final double gearRatio = 1.0;
  private final FlywheelSim flywheelSim =
      new FlywheelSim(
          Models.flywheelFromPhysicalConstants(DCMotor.getKrakenX60(1), 0.001, gearRatio),
          DCMotor.getKrakenX60(1));

  private final TelemetryTable table;

  /** Creates the physics sim for the intake launcher. */
  public static SingleFlywheelSim forIntakeLauncher(TalonFX talonMotor) {
    var sim = new SingleFlywheelSim(talonMotor, "IntakeLauncher");
    FuelSim.intakeLauncherVoltsSupplier = sim.flywheelSim::getInputVoltage;
    return sim;
  }

  /** Creates the physics sim for the feeder. */
  public static SingleFlywheelSim forFeeder(TalonFX talonMotor) {
    var sim = new SingleFlywheelSim(talonMotor, "Feeder");
    FuelSim.feederVoltsSupplier = sim.flywheelSim::getInputVoltage;
    return sim;
  }

  private SingleFlywheelSim(TalonFX talonMotor, String name) {
    this.talonMotor = talonMotor;
    this.talonMotorSim =
        new TalonFXSimState(talonMotor, ChassisReference.CounterClockwise_Positive);
    this.talonMotorSim.setMotorType(MotorType.KrakenX60);

    table = Telemetry.getTable(name);
  }

  public void periodic() {
    flywheelSim.setInputVoltage(talonMotorSim.getMotorVoltage());
    flywheelSim.update(0.02);

    double motorVelo = flywheelSim.getAngularVelocity() * gearRatio;
    motorPosition += motorVelo * 0.02 * gearRatio;

    talonMotorSim.setSupplyVoltage(12.0);
    talonMotorSim.setRawRotorPosition(Radians.of(motorPosition));
    talonMotorSim.setRotorVelocity(RadiansPerSecond.of(motorVelo));

    table.log("MotorVoltage", talonMotor.getMotorVoltage().getValue());
    table.log("MotorVelocity", talonMotor.getVelocity().getValue());
    table.log("MotorStatorCurrent", talonMotor.getStatorCurrent().getValue());
    table.log("MotorPosition", talonMotor.getPosition().getValue());
  }
}
