/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import static org.wpilib.units.Units.Seconds;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.TalonFX;
import first.robot.simulation.SingleFlywheelSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.framework.RobotBase;

public class IntakeLauncher implements Mechanism {
  private final TalonFX motor = new TalonFX(4, CANBus.systemcore(0));
  private final SingleFlywheelSim sim = SingleFlywheelSim.forIntakeLauncher(motor);

  public IntakeLauncher() {
    setDefaultCommand(idle());
  }

  public Command shoot() {
    return run(coroutine -> {
          coroutine.wait(Seconds.of(2));
          while (true) {
            motor.setThrottle(0.9);
            coroutine.yield();
          }
        })
        .named("Shoot");
  }

  public Command intake() {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(0.8);
            coroutine.yield();
          }
        })
        .named("Intake");
  }

  public Command outtake() {
    return run(coroutine -> {
          while (true) {
            motor.setThrottle(-0.8);
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
