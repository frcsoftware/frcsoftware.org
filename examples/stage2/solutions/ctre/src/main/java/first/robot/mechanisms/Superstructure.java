/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import static org.wpilib.units.Units.*;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.util.MathUtil;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.Voltage;

public class Superstructure implements Mechanism {

  private final Elevator elevator = new Elevator();
  private final Arm arm = new Arm();

  private static final Distance HEIGHT_MARGIN = Meters.of(0.05);
  private static final Angle ANGLE_MARGIN = Rotations.of(0.005);

  private Distance elevatorSetpoint = Meters.zero();
  private Angle armSetpoint = Rotations.zero();

  private final TelemetryTable table = Telemetry.getTable("Superstructure");

  public void periodic() {
    elevator.periodic();
    arm.periodic();

    table.log("Active Commands", getRunningCommands().toString());
  }

  public Command setArmVoltage(Voltage voltage) {
    return run(coroutine -> {
          arm.setVoltage(voltage);
          coroutine.park();
        })
        .named("Set Arm Voltage: " + voltage.in(Volts) + "V");
  }

  public Command setArmPosition(Angle armPosition) {
    return run(coroutine -> {
          arm.setPosition(armPosition);
          coroutine.park();
        })
        .named("Set Arm Position: " + armPosition.in(Rotations) + "rot");
  }

  public Command setElevatorVoltage(Voltage voltage) {
    return run(coroutine -> {
          elevator.setVoltage(voltage);
          coroutine.park();
        })
        .named("Set Elevator Voltage: " + voltage.in(Volts) + "V");
  }

  public Command setElevatorPosition(Distance position) {
    return run(coroutine -> {
          elevator.setPosition(position);
          coroutine.park();
        })
        .named("Set Elevator Position: " + position.in(Meters) + "m");
  }

  //  public Command setPosition(Distance elevatorPosition, Angle armPosition) {
  //    return run((coroutine) -> {
  //          coroutine.fork(elevator.setPosition(elevatorPosition), arm.setPosition(armPosition));
  //        })
  //        .named("Set Position");
  //  }

  public Command setPosition(Distance elevatorPosition, Angle armPosition) {
    return run((coroutine) -> {
          elevatorSetpoint = elevatorPosition;
          armSetpoint = armPosition;

          double maxHeightOfRange =
              CollisionProfile.minHeightBetween(
                  arm.getPosition().in(Rotations),
                  armPosition.in(Rotations),
                  ANGLE_MARGIN.in(Rotations));

          boolean elevatorStartsLow = elevator.getPosition().in(Meters) < maxHeightOfRange;
          boolean elevatorEndsLow = elevatorPosition.in(Meters) < maxHeightOfRange;
          boolean armMoves =
              MathUtil.isNear(arm.getPosition().in(Rotations), armPosition.in(Rotations), 0.01);

          if (!armMoves) {
            // If the arm doesn't move, we can just move the elevator, as if both positions are
            // valid end positions, it won't pass through the collision zone to the geometry of the
            // zone
            elevator.setPosition(elevatorPosition);
            arm.setPosition(armPosition); // Set the arm position as insurance
          } else if (!elevatorStartsLow && !elevatorEndsLow) {
            // If the elevator starts and ends above the collision zone, just move the
            // arm+elevator at once
            elevator.setPosition(elevatorPosition);
            arm.setPosition(armPosition);
          } else if (!elevatorStartsLow && elevatorEndsLow) {
            // The elevator starts clear, but ends below
            // Part 1: arm+elevator to top of safe zone
            // Part 2: move elevator down

            elevator.setPosition(Meters.of(maxHeightOfRange).plus(HEIGHT_MARGIN));
            arm.setPosition(armPosition);

            coroutine.waitUntil(arm::isAtSetpoint);

            elevator.setPosition(elevatorPosition);
          } else if (elevatorStartsLow && !elevatorEndsLow) {
            // The elevator starts low, but ends clear
            // Part 1: move elevator up to top of safe zone
            // Part 2: move arm+elevator to final position

            elevator.setPosition(elevatorPosition);

            coroutine.waitUntil(() -> elevator.getPosition().in(Meters) >= maxHeightOfRange);

            arm.setPosition(armPosition);
          } else {
            // The elevator starts and ends low, so we need a fully sequenced move
            // Part 1: move elevator up to top of safe zone
            // Part 2: swing arm over
            // Part 3: drop elevator to final position

            elevator.setPosition(Meters.of(maxHeightOfRange).plus(HEIGHT_MARGIN));
            coroutine.waitUntil(elevator::isAtSetpoint);

            arm.setPosition(armPosition);
            coroutine.waitUntil(arm::isAtSetpoint);

            elevator.setPosition(elevatorPosition);
          }
          coroutine.park();
        })
        .named("Set Position");
  }

  public boolean atSetpoint() {
    return arm.isAtPosition(armSetpoint) && elevator.isAtPosition(elevatorSetpoint);
  }

  public Command setPosition(Positions position) {
    return setPosition(position.elevatorSetpoint, position.armSetpoint);
  }

  public Distance getElevatorPosition() {
    return elevator.getPosition();
  }

  public Angle getArmPosition() {
    return arm.getPosition();
  }

  public enum Positions {
    STOW(Meters.of(0), Rotations.of(-0.75)),
    HANDOFF_PREP(Meters.of(0.5), Rotations.of(-0.25)),
    HANDOFF(Meters.of(0.38), Rotations.of(-0.25)),
    L4_PREP(Meters.of(1.2), Rotations.of(-0.6)),
    L4_SCORE(Meters.of(1.2), Rotations.of(-0.525)),
    L3_PREP(Meters.of(0.5), Rotations.of(-0.6)),
    L3_SCORE(Meters.of(0.5), Rotations.of(-0.55)),
    L2_PREP(Meters.of(0.1), Rotations.of(-0.6)),
    L2_SCORE(Meters.of(0.1), Rotations.of(-0.55)),
    ;

    private final Distance elevatorSetpoint;
    private final Angle armSetpoint;

    Positions(Distance elevatorSetpoint, Angle armSetpoint) {
      this.elevatorSetpoint = elevatorSetpoint;
      this.armSetpoint = armSetpoint;
    }
  }

  public static class CollisionProfile {
    // Angle, Height
    private static final double[][] BOUNDARY = {
      {-0.43, 0.00},
      {-0.43, 0.20},
      {-0.42, 0.20},
      {-0.38, 0.30},
      {-0.33, 0.44},
      {-0.26, 0.45},
      {-0.26, 0.40},
      {-0.24, 0.40},
      {-0.24, 0.80},
      {-0.16, 0.80},
      {-0.16, 0.55},
      {-0.10, 0.40},
      {-0.05, 0.20},
      {0.00, 0.15},
    };

    public static double minHeightAt(double angle) {
      double max = 0;

      for (int i = 0; i < BOUNDARY.length - 1; i++) {
        double x0 = BOUNDARY[i][0];
        double x1 = BOUNDARY[i + 1][0];

        double y0 = BOUNDARY[i][1];
        double y1 = BOUNDARY[i + 1][1];

        // Skip pair if the angle is outside them
        if (angle < Math.min(x0, x1) || angle > Math.max(x0, x1)) continue;
        // From here on, we're within/on top of the range

        if (x0 == x1) {
          // If x0 == x1, then the edge is a vertical line and thus the max is the highest of the
          // points
          max = Math.max(max, Math.max(y0, y1));
        } else {
          double t = MathUtil.inverseLerp(x0, x1, angle);
          max = Math.max(max, MathUtil.lerp(y0, y1, t));
        }
      }
      return max;
    }

    public static double minHeightBetween(double angleA, double angleB, double angleMargin) {
      double lowAngle = Math.min(angleA, angleB) - angleMargin;
      double highAngle = Math.max(angleA, angleB) + angleMargin;

      double max = Math.max(minHeightAt(lowAngle), minHeightAt(highAngle));

      for (double[] point : BOUNDARY) {
        // Skip point if outside the range
        if (point[0] > lowAngle && point[0] < highAngle) {
          max = Math.max(max, point[1]);
        }
      }
      return max;
    }
  }
}
