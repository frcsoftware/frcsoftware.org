package first.robot.simulation;

import first.robot.FieldConstants;
import org.wpilib.command3.Scheduler;
import org.wpilib.hardware.discrete.DigitalInput;
import org.wpilib.hardware.hal.SimDevice;
import org.wpilib.math.geometry.*;
import org.wpilib.math.shape.Rectangle2d;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.DIOSim;
import org.wpilib.system.Timer;
import org.wpilib.telemetry.Telemetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class CoralSim {

    private final Supplier<Pose2d> drivePoseSupplier;
    private final DoubleSupplier elevatorHeightSupplier;
    private final DoubleSupplier armAngleSupplier;

    private final DIOSim clawSensor;
    private final DIOSim indexerSensor;

    private final Transform3d INTAKE_CORAL_TRANSFORM = new Transform3d(0.5, 0, 0.15, Rotation3d.fromDegrees(0, 35, 45));
    private final Transform3d INDEXER_CORAL_TRANSFORM = new Transform3d(0, 0, 0.201295, new Rotation3d());

    private CoralState coralState = CoralState.GROUND;
    private final Timer coralIntakeTimer = new Timer();

    private boolean[][] redScoredCoral = new boolean[12][3];
    private boolean[][] blueScoredCoral = new boolean[12][3];

    private Pose3d[] groundCoralPoses = new Pose3d[] {
            new Pose3d(7, 3, Units.inchesToMeters(4.5/2), Rotation3d.fromDegrees(0, 0, -35)),
            new Pose3d(7, -3, Units.inchesToMeters(4.5/2), Rotation3d.fromDegrees(0, 0, 35)),
            new Pose3d(-7, -3, Units.inchesToMeters(4.5/2), Rotation3d.fromDegrees(0, 0, -35)),
            new Pose3d(-7, 3, Units.inchesToMeters(4.5/2), Rotation3d.fromDegrees(0, 0, 35)),
    };
    private Transform2d INTAKE_DETECTION_TRANSFORM = new Transform2d(0.6, 0, Rotation2d.ZERO);
    private double INTAKE_DETECTION_THRESHOLD = 0.3;
    private double INTAKE_TIME_THRESHOLD = 0.5;
//    private double INTAKE_TIME_THRESHOLD = 100;

    private Transform3d HANDOFF_DETECTION_TRANSFORM = new Transform3d(0, 0, 0.201295, new Rotation3d());
    private double HANDOFF_DETECTION_THRESHOLD = 0.05;

    public CoralSim(Supplier<Pose2d> drivePoseSupplier, DoubleSupplier elevatorHeightSupplier, DoubleSupplier armAngleSupplier) {
        this.drivePoseSupplier = drivePoseSupplier;
        this.elevatorHeightSupplier = elevatorHeightSupplier;
        this.armAngleSupplier = armAngleSupplier;

        indexerSensor = new DIOSim(0);
        clawSensor = new DIOSim(1);

        indexerSensor.setIsInput(true);
        clawSensor.setIsInput(true);

        Scheduler.getDefault().addPeriodic(this::periodic);

//        Arrays.fill(redScoredCoral, new boolean[]{true, true, true});
//        Arrays.fill(blueScoredCoral, new boolean[]{true, true, true});
    }

    public void periodic() {
        List<Pose3d> coralPoses = new ArrayList<>();

        switch(coralState) {
            case GROUND:
                coralPoses.addAll(Arrays.stream(groundCoralPoses).toList());

                indexerSensor.setValue(false);
                clawSensor.setValue(false);

                var intakeRect = new Rectangle2d(drivePoseSupplier.get().transformBy(INTAKE_DETECTION_TRANSFORM), 0.2, 0.7);

                Telemetry.log("intake detection point", new Pose3d(drivePoseSupplier.get().transformBy(INTAKE_DETECTION_TRANSFORM)));

                for (var pose : groundCoralPoses) {
                    if (intakeRect.contains(pose.getTranslation().toTranslation2d())) {
                        coralState = CoralState.INTAKE;
                        coralIntakeTimer.restart();
                    }
                }
                break;
            case INTAKE:
                var intakePose = new Pose3d(drivePoseSupplier.get()).transformBy(INTAKE_CORAL_TRANSFORM);
                var indexerPose = new Pose3d(drivePoseSupplier.get()).transformBy(INDEXER_CORAL_TRANSFORM);
                coralPoses.add(intakePose.interpolate(indexerPose, coralIntakeTimer.get() / INTAKE_TIME_THRESHOLD));

                indexerSensor.setValue(false);
                clawSensor.setValue(false);

                if (coralIntakeTimer.hasElapsed(INTAKE_TIME_THRESHOLD)) {
                    coralState = CoralState.INDEXER;
                }
                break;
            case INDEXER:
                coralPoses.add(new Pose3d(drivePoseSupplier.get()).transformBy(INDEXER_CORAL_TRANSFORM));

                indexerSensor.setValue(true);
                clawSensor.setValue(false);
                break;
            case CLAW:
                coralPoses.add(getClawCoralPose());

                indexerSensor.setValue(false);
                clawSensor.setValue(true);
                break;
        }

        for (int branch = 0; branch < 12; branch++) {
            for (int level = 2; level <= 4; level++) {
                if (redScoredCoral[branch][level-2]) {
                    coralPoses.add(getCoralPose(true, branch, level));
                }
                if (blueScoredCoral[branch][level-2]) {
                    coralPoses.add(getCoralPose(false, branch, level));
                }
            }
        }

        Telemetry.log("Coral", coralPoses, Pose3d.class);
    }

    private double CORAL_SCORE_DISTANCE_THRESHOLD = 0.25;

    public void attemptScore() {
        boolean red = drivePoseSupplier.get().getX() < 0;
        topLoop:
        for (int branch = 0; branch < 12; branch++) {
            for (int level = 2; level <= 4; level++) {
                if (getCoralPose(red, branch, level).getTranslation().getDistance(getClawCoralPose().getTranslation()) < CORAL_SCORE_DISTANCE_THRESHOLD) {
                    if (red) {
                        redScoredCoral[branch][level-2] = true;
                    } else {
                        blueScoredCoral[branch][level-2] = true;
                    }

                    coralState = CoralState.GROUND;

                    break topLoop;
                }
            }
        }
    }

    public void attemptHandoff() {
        if (coralState == CoralState.INDEXER) {
            Telemetry.log("dist", new Pose3d(drivePoseSupplier.get()).transformBy(HANDOFF_DETECTION_TRANSFORM).getTranslation().getDistance(getClawCoralPose().getTranslation()));
            if (new Pose3d(drivePoseSupplier.get()).transformBy(HANDOFF_DETECTION_TRANSFORM).getTranslation().getDistance(getClawCoralPose().getTranslation()) < HANDOFF_DETECTION_THRESHOLD) {
                coralState = CoralState.CLAW;
            }
        }
    }

    private enum CoralState {
        GROUND,
        INTAKE,
        INDEXER,
        CLAW
    }

    private final double CORAL_Y_OFFSET = 0.164309;
    private final Transform3d CORAL_LEFT_TRANSFORM = new Transform3d(0, -CORAL_Y_OFFSET, 0, new Rotation3d());
    private final Transform3d CORAL_RIGHT_TRANSFORM = new Transform3d(0, CORAL_Y_OFFSET, 0, new Rotation3d());
    private final Transform3d CORAL_L2_TRANSFORM = new Transform3d(0.71, 0, 0.725, new Rotation3d(0, Units.degreesToRadians(-35), 0));
    private final Transform3d CORAL_L3_TRANSFORM = new Transform3d(0.71, 0, 1.13, new Rotation3d(0, Units.degreesToRadians(-35), 0));
    private final Transform3d CORAL_L4_TRANSFORM = new Transform3d(0.76, 0, 1.73, new Rotation3d(0, Units.degreesToRadians(90), 0));

    private Pose3d getCoralPose(boolean red, int branch, int level) {
        var reefCenter = red ? FieldConstants.RED_REEF_CENTER : FieldConstants.BLUE_REEF_CENTER;
        var reefCenterPointed = new Pose2d(reefCenter, Rotation2d.fromDegrees(60).times(branch / 2).plus(red ? Rotation2d.k180deg : Rotation2d.ZERO));
        var shiftedCenter = new Pose3d(reefCenterPointed).transformBy((branch % 2 == 0) ? CORAL_LEFT_TRANSFORM : CORAL_RIGHT_TRANSFORM);
        var branchTransform = switch (level) {
            case 2 -> CORAL_L2_TRANSFORM;
            case 3 -> CORAL_L3_TRANSFORM;
            case 4 -> CORAL_L4_TRANSFORM;
            default -> CORAL_L2_TRANSFORM;
        };
        Telemetry.log("reefCenter", new Pose2d(reefCenter, Rotation2d.ZERO));
        return shiftedCenter.transformBy(branchTransform);
    }

    private Pose3d getClawCoralPose() {
        var elevatorTransform = new Transform3d(0, 0, elevatorHeightSupplier.getAsDouble() + 0.514671, new Rotation3d());
        var armTransform = new Transform3d(0, 0, 0, new Rotation3d(0, Units.rotationsToRadians(armAngleSupplier.getAsDouble()), 0));
        var clawCoralTransform = new Transform3d(0.68036-0.02, 0, 0, new Rotation3d(0, Math.PI/2, 0));

        return new Pose3d(drivePoseSupplier.get()).transformBy(elevatorTransform).transformBy(armTransform).transformBy(clawCoralTransform);
    }
}
