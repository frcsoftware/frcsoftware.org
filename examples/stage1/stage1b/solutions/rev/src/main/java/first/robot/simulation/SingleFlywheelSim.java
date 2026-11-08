/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import static org.wpilib.units.Units.*;

import com.revrobotics.spark.SparkMax;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;

public class SingleFlywheelSim {

  private final SparkMax motor;

  private final FlywheelSim m_flywheelSim;

  private final TelemetryTable table;
  private double rotorPositionRad;

  private static final double kBusVoltage = 12.0;

  /** Creates the physics sim for the intake launcher. */
  public static SingleFlywheelSim forIntakeLauncher(SparkMax motor) {
    var sim = new SingleFlywheelSim(motor, "IntakeLauncher");
    FuelSim.intakeLauncherVoltsSupplier = sim.m_flywheelSim::getInputVoltage;
    return sim;
  }

  /** Creates the physics sim for the feeder. */
  public static SingleFlywheelSim forFeeder(SparkMax motor) {
    var sim = new SingleFlywheelSim(motor, "Feeder");
    FuelSim.feederVoltsSupplier = sim.m_flywheelSim::getInputVoltage;
    return sim;
  }

  private SingleFlywheelSim(SparkMax motor, String name) {
    this.motor = motor;
    var gearbox = DCMotor.getNEO(1);
    this.m_flywheelSim =
        new FlywheelSim(Models.flywheelFromPhysicalConstants(gearbox, 0.001, 1.0), gearbox);

    this.table = Telemetry.getTable(name);
  }

  public void periodic() {
    double motorVoltage = motor.getThrottle() * kBusVoltage;

    m_flywheelSim.setInputVoltage(motorVoltage);
    m_flywheelSim.update(0.02);

    double radPerSec = m_flywheelSim.getAngularVelocity();
    rotorPositionRad += radPerSec * 0.02;

    table.log("MotorVoltage", Volts.of(motorVoltage));
    table.log("MotorVelocity", RadiansPerSecond.of(radPerSec));
    table.log("Current", Amps.of(m_flywheelSim.getCurrentDraw()));
    table.log("MotorPosition", Radians.of(rotorPositionRad));
  }
}
