package first.robot;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Transform2d;
import org.wpilib.math.geometry.Translation2d;

import java.util.List;

public class Poses {

    public static final Pose2d BLUE_REEF_A = new Pose2d(FieldConstants.BLUE_REEF_CENTER.plus(new Translation2d(1.4, -0.164309)), Rotation2d.ZERO);
    public static final Pose2d BLUE_REEF_B = FieldConstants.mirrorY(BLUE_REEF_A);
    public static final Pose2d BLUE_REEF_C = BLUE_REEF_A.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(60));
    public static final Pose2d BLUE_REEF_D = BLUE_REEF_B.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(60));
    public static final Pose2d BLUE_REEF_E = BLUE_REEF_A.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(120));
    public static final Pose2d BLUE_REEF_F = BLUE_REEF_B.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(120));
    public static final Pose2d BLUE_REEF_G = BLUE_REEF_A.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(180));
    public static final Pose2d BLUE_REEF_H = BLUE_REEF_B.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(180));
    public static final Pose2d BLUE_REEF_I = BLUE_REEF_A.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(240));
    public static final Pose2d BLUE_REEF_J = BLUE_REEF_B.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(240));
    public static final Pose2d BLUE_REEF_K = BLUE_REEF_A.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(300));
    public static final Pose2d BLUE_REEF_L = BLUE_REEF_B.rotateAround(FieldConstants.BLUE_REEF_CENTER, Rotation2d.fromDegrees(300));

    public static final List<Pose2d> BLUE_REEF_POSES = List.of(BLUE_REEF_A, BLUE_REEF_B, BLUE_REEF_C, BLUE_REEF_D, BLUE_REEF_E, BLUE_REEF_F, BLUE_REEF_G, BLUE_REEF_H, BLUE_REEF_I, BLUE_REEF_J, BLUE_REEF_K, BLUE_REEF_L);
    public static final List<Pose2d> BLUE_REEF_LEFT_POSES = List.of(BLUE_REEF_A, BLUE_REEF_C, BLUE_REEF_E, BLUE_REEF_G, BLUE_REEF_I, BLUE_REEF_K);
    public static final List<Pose2d> BLUE_REEF_RIGHT_POSES = List.of(BLUE_REEF_B, BLUE_REEF_D, BLUE_REEF_F, BLUE_REEF_H, BLUE_REEF_J, BLUE_REEF_L);

    public static final Pose2d RED_REEF_A = FieldConstants.rotateAboutCenter(BLUE_REEF_A);
    public static final Pose2d RED_REEF_B = FieldConstants.rotateAboutCenter(BLUE_REEF_B);
    public static final Pose2d RED_REEF_C = FieldConstants.rotateAboutCenter(BLUE_REEF_C);
    public static final Pose2d RED_REEF_D = FieldConstants.rotateAboutCenter(BLUE_REEF_D);
    public static final Pose2d RED_REEF_E = FieldConstants.rotateAboutCenter(BLUE_REEF_E);
    public static final Pose2d RED_REEF_F = FieldConstants.rotateAboutCenter(BLUE_REEF_F);
    public static final Pose2d RED_REEF_G = FieldConstants.rotateAboutCenter(BLUE_REEF_G);
    public static final Pose2d RED_REEF_H = FieldConstants.rotateAboutCenter(BLUE_REEF_H);
    public static final Pose2d RED_REEF_I = FieldConstants.rotateAboutCenter(BLUE_REEF_I);
    public static final Pose2d RED_REEF_J = FieldConstants.rotateAboutCenter(BLUE_REEF_J);
    public static final Pose2d RED_REEF_K = FieldConstants.rotateAboutCenter(BLUE_REEF_K);
    public static final Pose2d RED_REEF_L = FieldConstants.rotateAboutCenter(BLUE_REEF_L);

    public static final List<Pose2d> RED_REEF_POSES = List.of(RED_REEF_A, RED_REEF_B, RED_REEF_C, RED_REEF_D, RED_REEF_E, RED_REEF_F, RED_REEF_G, RED_REEF_H, RED_REEF_I, RED_REEF_J, RED_REEF_K, RED_REEF_L);
    public static final List<Pose2d> RED_REEF_LEFT_POSES = List.of(RED_REEF_A, RED_REEF_C, RED_REEF_E, RED_REEF_G, RED_REEF_I, RED_REEF_K);
    public static final List<Pose2d> RED_REEF_RIGHT_POSES = List.of(RED_REEF_B, RED_REEF_D, RED_REEF_F, RED_REEF_H, RED_REEF_J, RED_REEF_L);

    public static final Transform2d REEF_PREALIGN_TRANSFORM = new Transform2d(0.3, 0, Rotation2d.ZERO);
}
