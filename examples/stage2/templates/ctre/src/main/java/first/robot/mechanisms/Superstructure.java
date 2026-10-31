/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.util.MathUtil;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;

public class Superstructure implements Mechanism {

  private final Elevator elevator;
  private final Arm arm;

  private static final double HEIGHT_MARGIN = 0.05;
  private static final double ANGLE_MARGIN = 0.005;

  private double elevatorSetpoint = 0;
  private double armSetpoint = 0;

  private TelemetryTable table = Telemetry.getTable("Superstructure");

  public Superstructure() {
    this.elevator = new Elevator();
    this.arm = new Arm();
  }

  public void periodic() {
    elevator.periodic();
    arm.periodic();

    table.log("Active Commands", getRunningCommands().toString());
  }

  public Command setArmVoltage(double voltage) {
    return run(coro -> {
          arm.setVoltage(voltage);
          coro.park();
        })
        .named("Set Arm Voltage: " + voltage + "V");
  }

  public Command setArmPosition(double armPosition) {
    return run(coro -> {
          arm.setPosition(armPosition);
          coro.park();
        })
        .named("Set Arm Position: " + armPosition + "rot");
  }

  public Command setElevatorVoltage(double voltage) {
    return run(coro -> elevator.setVoltage(voltage))
        .named("Set Elevator Voltage: " + voltage + "V");
  }

  public Command setElevatorPosition(double position) {
    return run(coro -> elevator.setPosition(position))
        .named("Set Elevator Position: " + position + "m");
  }

  //  public Command setPosition(double elevatorPosition, double armPosition) {
  //    return run((coro) -> {
  //          coro.awaitAll(elevator.setPosition(elevatorPosition), arm.setPosition(armPosition));
  //        })
  //        .named("Set Position");
  //  }

  public Command setPosition(double elevatorPosition, double armPosition) {
    return run((coro) -> {
          elevatorSetpoint = elevatorPosition;
          armSetpoint = armPosition;

          double maxHeightOfRange =
              CollisionProfile.minHeightBetween(arm.getPosition(), armPosition, ANGLE_MARGIN);

          boolean elevatorStartsLow = elevator.getPosition() < maxHeightOfRange;
          boolean elevatorEndsLow = elevatorPosition < maxHeightOfRange;
          boolean armMoves = MathUtil.isNear(arm.getPosition(), armPosition, 0.01);

          if (!elevatorStartsLow && !elevatorEndsLow) {
            // If the elevator starts and ends above the collision zone, just move the
            // arm+elevator at once
            elevator.setPosition(elevatorPosition);
            arm.setPosition(armPosition);
          } else if (!elevatorStartsLow && elevatorEndsLow) {
            // The elevator starts clear, but ends below
            // Part 1: arm+elevator to top of safe zone
            // Part 2: move elevator down

            elevator.setPosition(maxHeightOfRange + HEIGHT_MARGIN);
            arm.setPosition(armPosition);

            coro.waitUntil(arm::isAtSetpoint);

            elevator.setPosition(elevatorPosition);
          } else if (elevatorStartsLow && !elevatorEndsLow) {
            // The elevator starts low, but ends clear
            // Part 1: move elevator up to top of safe zone
            // Part 2: move arm+elevator to final position

            elevator.setPosition(elevatorPosition);

            coro.waitUntil(() -> elevator.getPosition() >= maxHeightOfRange);

            arm.setPosition(armPosition);
          } else {
            // The elevator starts and ends low, so we need a fully sequenced move
            // Part 1: move elevator up to top of safe zone
            // Part 2: swing arm over
            // Part 3: drop elevator to final position

            elevator.setPosition(maxHeightOfRange + HEIGHT_MARGIN);
            coro.waitUntil(elevator::isAtSetpoint);

            arm.setPosition(armPosition);
            coro.waitUntil(arm::isAtSetpoint);

            elevator.setPosition(elevatorPosition);
          }
          coro.park();
        })
        .named("Set Position");
  }

  public boolean atSetpoint() {
    return arm.isAtPosition(armSetpoint) && elevator.isAtPosition(elevatorSetpoint);
  }

  public Command setPosition(Positions position) {
    return setPosition(position.elevatorSetpoint, position.armSetpoint);
  }

  public double getElevatorPosition() {
    return elevator.getPosition();
  }

  public double getArmPosition() {
    return arm.getPosition();
  }

  public enum Positions {
    STOW(0, -0.75),
    HANDOFF_PREP(0.5, -0.25),
    HANDOFF(0.38, -0.25),
    L4_PREP(1.2, -0.6),
    L4_SCORE(1.2, -0.525),
    L3_PREP(0.5, -0.6),
    L3_SCORE(0.5, -0.55),
    L2_PREP(0.1, -0.6),
    L2_SCORE(0.1, -0.55),
    ;

    private double elevatorSetpoint;
    private double armSetpoint;

    Positions(double elevatorSetpoint, double armSetpoint) {
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
