/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import first.robot.simulation.SingleFlywheelSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.bus.CANPort;

public class Feeder implements Mechanism {
  private final SparkMax motor = new SparkMax(CANPort.CAN_S0, 5, MotorType.kBrushless);
  private final SingleFlywheelSim sim = SingleFlywheelSim.forFeeder(motor);

  public Feeder() {
    setDefaultCommand(idle());
  }

  public Command feed() {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(0.75);
            coroutine.yield();
          }
        })
        .named("Feed");
  }

  public Command intake() {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(-1.0);
            coroutine.yield();
          }
        })
        .named("Intake");
  }

  public Command outtake() {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(1.0);
            coroutine.yield();
          }
        })
        .named("Outtake");
  }

  public Command idle() {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(0.0);
            coroutine.yield();
          }
        })
        .named("Idle");
  }

  public void periodic() {
    sim.periodic();
  }
}
