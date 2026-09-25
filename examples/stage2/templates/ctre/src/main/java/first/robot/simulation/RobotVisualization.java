package first.robot.simulation;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class RobotVisualization {

    private final Supplier<Pose2d> drivePoseSupplier;
    private final DoubleSupplier elevatorHeightSupplier;
    private final DoubleSupplier armAngleSupplier;
    private final DoubleSupplier intakeAngleSupplier;

    private final Pose3d ELEVATOR_STARTING_POSE = new Pose3d();
    private final double ELEVATOR_CARRIAGE_TRAVEL = 0.651510;

    private final Transform3d ARM_BASE_TRANSFORM = new Transform3d(0, 0, 0.4191+0.095570802, new Rotation3d(0, -Math.PI/2, 0));

    private final Pose3d INTAKE_BASE_POSE = new Pose3d(0.3210, 0, 0.29837, new Rotation3d());

    public RobotVisualization(Supplier<Pose2d> drivePoseSupplier, DoubleSupplier elevatorHeightSupplier, DoubleSupplier armAngleSupplier, DoubleSupplier intakeAngleSupplier) {
        this.drivePoseSupplier = drivePoseSupplier;
        this.elevatorHeightSupplier = elevatorHeightSupplier;
        this.armAngleSupplier = armAngleSupplier;
        this.intakeAngleSupplier = intakeAngleSupplier;
    }

    public void periodic() {
        double elevatorHeight = Math.clamp(elevatorHeightSupplier.getAsDouble(), 0, 1.347);
        double intakeAngle = Math.clamp(intakeAngleSupplier.getAsDouble(), 0, Units.degreesToRadians(138.469));

        double firstStageHeight = elevatorHeight < ELEVATOR_CARRIAGE_TRAVEL ? 0 : elevatorHeight - ELEVATOR_CARRIAGE_TRAVEL;
        Transform3d firstStageTransform = new Transform3d(0, 0, firstStageHeight, new Rotation3d());
        Pose3d firstStagePose = ELEVATOR_STARTING_POSE.transformBy(firstStageTransform);

        Transform3d carriageTransform = new Transform3d(0, 0, elevatorHeight, new Rotation3d());
        Pose3d carriagePose = ELEVATOR_STARTING_POSE.transformBy(carriageTransform);

        Transform3d armTransform = new Transform3d(0, 0, 0, new Rotation3d(0, Units.rotationsToRadians(armAngleSupplier.getAsDouble()), 0));
        Pose3d armPose = carriagePose.transformBy(ARM_BASE_TRANSFORM).transformBy(armTransform);

        Transform3d intakeTransform = new Transform3d(0, 0, 0, new Rotation3d(0, -intakeAngle, 0));
        Pose3d intakePose = INTAKE_BASE_POSE.transformBy(intakeTransform);

        Telemetry.log("Mechanisms", new Pose3d[]{firstStagePose, carriagePose, armPose, intakePose});

        Telemetry.log("Blank pose3ds", new Pose3d[]{new Pose3d(), new Pose3d(), new Pose3d(), new Pose3d()});
    }
}
