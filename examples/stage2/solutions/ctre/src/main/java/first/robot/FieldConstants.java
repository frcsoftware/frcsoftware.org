/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot;

import java.util.List;
import org.wpilib.driverstation.Alliance;
import org.wpilib.driverstation.MatchState;
import org.wpilib.fields.Fields;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;

public class FieldConstants {

  public static final double fieldWidth = Fields.FRC_2025_REEFSCAPE_WELDED.width;
  public static final double fieldLength = Fields.FRC_2025_REEFSCAPE_WELDED.length;

  public static final Translation2d BLUE_REEF_CENTER = new Translation2d(4.284788, 0);
  public static final Translation2d RED_REEF_CENTER = mirrorX(BLUE_REEF_CENTER);

  public static Translation2d mirrorX(Translation2d translation) {
    return new Translation2d(-translation.getX(), translation.getY());
  }

  public static Pose2d mirrorX(Pose2d pose) {
    return new Pose2d(mirrorX(pose.getTranslation()), Rotation2d.PI.minus(pose.getRotation()));
  }

  public static Translation2d mirrorY(Translation2d translation) {
    return new Translation2d(translation.getX(), -translation.getY());
  }

  public static Pose2d mirrorY(Pose2d pose) {
    return new Pose2d(mirrorY(pose.getTranslation()), pose.getRotation().unaryMinus());
  }

  public static Translation2d rotateAboutCenter(Translation2d translation) {
    return mirrorX(mirrorY(translation));
  }

  public static Pose2d rotateAboutCenter(Pose2d pose) {
    return mirrorX(mirrorY(pose));
  }

  public static boolean shouldFlip() {
    return MatchState.getAlliance().isPresent() && MatchState.getAlliance().get() == Alliance.RED;
  }

  public static Translation2d handleFlip(Translation2d translation) {
    return shouldFlip() ? rotateAboutCenter(translation) : translation;
  }

  public static Pose2d handleFlip(Pose2d pose) {
    return shouldFlip() ? rotateAboutCenter(pose) : pose;
  }

  public static List<Pose2d> handleFlip(List<Pose2d> poses) {
    return poses.stream().map(FieldConstants::handleFlip).toList();
  }
}
