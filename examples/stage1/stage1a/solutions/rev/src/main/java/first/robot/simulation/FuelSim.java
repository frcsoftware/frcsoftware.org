/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import java.util.ArrayList;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.networktables.NetworkTable;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructArrayPublisher;

public class FuelSim {
  private enum Mode {
    INTAKE,
    SHOOT,
    OUTTAKE
  }

  private static final double SHOT_DT = 0.1,
      SHOT_VELOCITY_X = -2,
      SHOT_VELOCITY_Z = 5.5,
      SPEED_EPSILON = 20;
  private static final Transform3d
      FIRST_ROW_POSE = new Transform3d(-0.22, 0, 0.35, Rotation3d.ZERO),
      SECOND_ROW_POSE = new Transform3d(-0.08, 0, 0.3, Rotation3d.ZERO),
      THIRD_ROW_POSE = new Transform3d(0.06, 0, 0.25, Rotation3d.ZERO),
      SHOT_ORIGIN = new Transform3d(0.2, 0, 0.5, Rotation3d.ZERO);

  private static final NetworkTable logTable =
      NetworkTableInstance.getDefault().getTable("FuelSim");
  private static final StructArrayPublisher<Pose3d>
      row1FuelPub = logTable.getStructArrayTopic("Row1Fuel", Pose3d.struct).publish(),
      row2FuelPub = logTable.getStructArrayTopic("Row2Fuel", Pose3d.struct).publish(),
      row3FuelPub = logTable.getStructArrayTopic("Row3Fuel", Pose3d.struct).publish(),
      parabolaFuelPub = logTable.getStructArrayTopic("ParabolaFuel", Pose3d.struct).publish();
  private static Mode mode = null;
  private static boolean isPaused = false;
  private static double rowsOfFuel = 0;

  /** A supplier that fetches the velocity of the feeder. */
  static DoubleSupplier feederSpeedSupplier = () -> 0;

  /** A supplier that fetches the velocity of the intake. */
  static DoubleSupplier intakeLauncherSpeedSupplier = () -> 0;

  /** A supplier that fetches the robot pose. */
  static Supplier<Pose2d> robotPoseSupplier = () -> Pose2d.ZERO;

  /** Updates the fuel sim. */
  public static void periodic() {
    updateMode();
    updateVisualization();
  }

  private static Pose3d[] basicParabola(Pose3d start) {
    var poses = new ArrayList<Pose3d>();
    poses.add(start);
    double t = 0;
    var currentPose = start;
    while (poses.getLast().getZ() > 0) {
      double deltaX = SHOT_VELOCITY_X * SHOT_DT;
      double deltaZ =
          SHOT_VELOCITY_Z * SHOT_DT - 0.5 * 9.8 * (Math.pow(t + SHOT_DT, 2) - Math.pow(t, 2));
      currentPose = currentPose.plus(new Transform3d(deltaX, 0, deltaZ, Rotation3d.ZERO));
      poses.add(currentPose);
      t += SHOT_DT;
    }
    return poses.toArray(Pose3d[]::new);
  }

  private static Pose3d[] fuelRow(Pose3d base) {
    var offset = new Transform3d(0, 0.15, 0, Rotation3d.ZERO);
    var poses = new Pose3d[3];
    poses[0] = base.plus(offset.inverse());
    poses[1] = base;
    poses[2] = base.plus(offset);
    return poses;
  }

  private static void updateMode() {
    double intakeLauncherSpeed = intakeLauncherSpeedSupplier.getAsDouble();
    double feederSpeed = feederSpeedSupplier.getAsDouble();
    isPaused = false;
    if (intakeLauncherSpeed > SPEED_EPSILON && feederSpeed > SPEED_EPSILON) {
      mode = Mode.SHOOT;
    } else if (intakeLauncherSpeed < SPEED_EPSILON && feederSpeed > SPEED_EPSILON) {
      mode = Mode.OUTTAKE;
    } else if (intakeLauncherSpeed > SPEED_EPSILON && feederSpeed < SPEED_EPSILON) {
      mode = Mode.INTAKE;
    } else {
      isPaused = true;
    }
  }

  private static void updateVisualization() {
    var robotPose = new Pose3d(robotPoseSupplier.get());
    if (mode == Mode.SHOOT && rowsOfFuel > 0 && !isPaused) {
      parabolaFuelPub.set(basicParabola(robotPose.plus(SHOT_ORIGIN)));
    } else {
      parabolaFuelPub.set(new Pose3d[0]);
    }

    if (!isPaused) {
      rowsOfFuel += (mode == Mode.INTAKE ? 0.04 : -0.04);
      rowsOfFuel = Math.clamp(rowsOfFuel, 0, 3);
    }

    row1FuelPub.set(rowsOfFuel >= 1 ? fuelRow(robotPose.plus(FIRST_ROW_POSE)) : new Pose3d[0]);
    row2FuelPub.set(rowsOfFuel >= 2 ? fuelRow(robotPose.plus(SECOND_ROW_POSE)) : new Pose3d[0]);
    row3FuelPub.set(rowsOfFuel >= 3 ? fuelRow(robotPose.plus(THIRD_ROW_POSE)) : new Pose3d[0]);
  }
}
