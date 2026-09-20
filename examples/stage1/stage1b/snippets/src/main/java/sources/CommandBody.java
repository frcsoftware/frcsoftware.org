/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import org.wpilib.command3.Command;
import org.wpilib.drive.DifferentialDrive;

class CommandBody {
  void main() {
    // [triangleLoop]
    int number = 1;
    while (number < 4) {
      System.out.println("-".repeat(number));
      number = number + 1;
    }
    while (number > 0) {
      System.out.println("-".repeat(number));
      number = number - 1;
    }
    // [/triangleLoop]

    var exampleCommand =
        Command.noRequirements(
                coroutine -> {
                  var motor = new ExampleMotor();
                  var differentialDrive = new DifferentialDrive(throttle -> {}, throttle -> {});

                  // [rotate90CommandBody]
                  double targetDirection = getCurrentYaw() + 90;
                  while (getCurrentYaw() < targetDirection) {
                    differentialDrive.arcadeDrive(0, 0.5);
                    coroutine.yield();
                  }
                  differentialDrive.arcadeDrive(0, 0);
                  // [/rotate90CommandBody]

                  // [fullThrottleCmdBody]
                  System.out.println("Full Speed Baby!");
                  while (true) {
                    motor.setThrottle(1.0);
                    coroutine.yield(); // Confused? We'll explain this in the section below
                  }
                  // [/fullThrottleCmdBody]
                })
            .named("Example!");
  }

  private double getCurrentYaw() {
    return 0;
  }
}
