/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import static org.wpilib.units.Units.Seconds;

import com.ctre.phoenix6.hardware.TalonFX;
import com.revrobotics.spark.SparkMax;
import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;
import org.wpilib.opmode.PeriodicOpMode;

class CommandBasedKitbotPt2 {
  // [opModeSkeleton]
  class MyOpModeName extends PeriodicOpMode {
    private final Robot robot;

    public MyOpModeName(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void start() {}
  }
  // [/opModeSkeleton]

  class AutoModeExampleCode extends PeriodicOpMode {
    private final Command myAutoCommand = null;

    // [startMethod]
    @Override
    public void start() {
      Scheduler.getDefault().schedule(myAutoCommand);
    }
    // [/startMethod]

    public AutoModeExampleCode(Robot robot) {
      DoubleSupplier forwardThrottle = null, rotationThrottle = null;

      var driveCmd =
          // [driveCommand]
          robot.drivetrain.arcadeDrive(forwardThrottle, rotationThrottle);
      // [/driveCommand]

      var driveCmdWithTimeout =
          // [4SecDriveCommand]
          robot.drivetrain
              .arcadeDrive(forwardThrottle, rotationThrottle)
              .withTimeout(Seconds.of(4));
      // [/4SecDriveCommand]
    }
  }

  class Robot {
    DriveCommandExamples drivetrain = new DriveCommandExamples();
  }

  class DriveCommandExamples implements Mechanism {
    private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
    private final DifferentialDrive differentialDrive = new DifferentialDrive(throttle -> {}, throttle -> {});

    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      return run(coroutine -> {
            // [arcadeDriveBody]
            while (true) {
              differentialDrive.arcadeDrive(
                  forwardThrottle.getAsDouble(), rotationThrottle.getAsDouble());
              coroutine.yield();
            }
            // [/arcadeDriveBody]
          })
          .named("Drive");
    }
  }

  class DriveBackAndShootExamples implements Mechanism {
    // [autoCmdDef]
    private Command autoCommand() {
      return null; // what should replace 'null' here?
    }
    // [/autoCmdDef]
  }

  static class DrivetrainSim {
    DrivetrainSim(TalonFX leftLeader, TalonFX rightLeader) {}

    DrivetrainSim(SparkMax leftLeader, SparkMax rightLeader) {}

    void periodic() {}
  }
}
