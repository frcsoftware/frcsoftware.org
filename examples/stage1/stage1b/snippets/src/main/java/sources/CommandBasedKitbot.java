/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.opmode.PeriodicOpMode;

class CommandBasedKitbot {
  // [feederDef]
  public class Feeder implements Mechanism {
    // your code here...
  }
  // [/feederDef]

  void schedulerExample() {
    // [robotDef]
    class Robot extends OpModeRobot {
      @Override
      public void robotPeriodic() {
        Scheduler.getDefault().run();
      }
    }
    // [/robotDef]
  }

  class MyTeleop extends PeriodicOpMode {
    private final CommandXboxController xbox = new CommandXboxController(0);

    // [triggerBindingDef]
    public MyTeleop(Robot robot) {
      xbox.leftBumper().whileTrue(robot.intakeLauncher.intake()).whileTrue(robot.feeder.intake());

      // now fill in the rest...
    }
    // [/triggerBindingDef]
  }

  private final ExampleMotor motor = new ExampleMotor();

  // [feederSim]
  private final SingleFlywheelSim sim = new SingleFlywheelSim(motor, "Feeder"); // Create a flywheel simulation

  public void periodic() { // Update the simulation
    sim.periodic();
  }
  // [/feederSim]

  class ExampleMechanism {
    Command intake() {
      return null;
    }
  }

  class Robot {
    ExampleMechanism intakeLauncher = new ExampleMechanism();
    ExampleMechanism feeder = new ExampleMechanism();
  }

  class SingleFlywheelSim {
    SingleFlywheelSim(ExampleMotor motor, String name) {}

    void periodic() {}
  }
}
