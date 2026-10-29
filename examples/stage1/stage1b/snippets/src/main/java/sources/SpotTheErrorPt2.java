/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.TalonFX;
import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.OpModeRobot;

public class SpotTheErrorPt2 {
  private final DifferentialDrive differentialDrive = null;

  class ArcadeDriveBug implements Mechanism {
    // [arcadeDriveBug]
    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      double forward = forwardThrottle.getAsDouble();
      double rotation = rotationThrottle.getAsDouble();
      return run(coroutine -> {
            while (true) {
              differentialDrive.arcadeDrive(forward, rotation);
              coroutine.yield();
            }
          })
          .named("Drive");
    }
    // [/arcadeDriveBug]
  }

  class ArcadeDriveFix implements Mechanism {
    // [arcadeDriveFix]
    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      return run(coroutine -> {
            while (true) {
              differentialDrive.arcadeDrive(
                  forwardThrottle.getAsDouble(), rotationThrottle.getAsDouble());
              coroutine.yield();
            }
          })
          .named("Drive");
    }
    // [/arcadeDriveFix]
  }

  // [robotPeriodicBug]
  public class Robot extends OpModeRobot {
    private final CommandXboxController xbox = new CommandXboxController(0);

    public Robot() {
      xbox.a().whileTrue(printHelloWorld());
    }

    @Override
    public void robotPeriodic() {}

    private Command printHelloWorld() {
      return Command.noRequirements(
              coroutine -> {
                while (true) {
                  System.out.println("Hello World!");
                  coroutine.yield();
                }
              })
          .named("Hello World!");
    }
  }

  // [/robotPeriodicBug]

  // [triggerUpdatesBug]
  public class Arm implements Mechanism {
    private double target = 0;
    private final TalonFX motor = new TalonFX(0, CANBus.systemcore(0));
    private final Trigger hasNotHitTarget =
        new Trigger(() -> motor.getPosition().getValueAsDouble() < target);

    public Command subOptimalSetAngle(double targetRotations) {
      return run(coroutine -> {
            target = targetRotations;
            motor.setThrottle(0.1);
            while (hasNotHitTarget.getAsBoolean()) {
              coroutine.yield();
            }
            motor.setThrottle(0);
          })
          .named("Use PID instead(you'll learn this in Stage 1C!)");
    }
  }

  // [/triggerUpdatesBug]

  class RobotFixed extends OpModeRobot {
    private final Drivetrain drivetrain = new Drivetrain();

    // [robotPeriodicFix]
    @Override
    public void robotPeriodic() {
      Scheduler.getDefault().run();
    }
    // [/robotPeriodicFix]
  }

  class Drivetrain implements Mechanism {
    void periodic() {}

    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      return null;
    }
  }
}
