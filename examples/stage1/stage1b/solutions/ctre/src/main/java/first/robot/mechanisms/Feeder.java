/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.TalonFX;
import first.robot.simulation.SingleFlywheelSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.framework.RobotBase;

public class Feeder implements Mechanism {
  private final TalonFX motor = new TalonFX(5, CANBus.systemcore(0));
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
    if (RobotBase.isSimulation()) {
      sim.periodic();
    }
  }
}
