package first.robot.mechanisms;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.ParentConfiguration;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.StaticFeedforwardSignValue;
import com.ctre.phoenix6.swerve.*;
import org.wpilib.command3.*;
import org.wpilib.command3.button.CommandNiDsXboxController;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.math.controller.PIDController;
import org.wpilib.math.controller.ProfiledPIDController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.math.trajectory.TrapezoidProfile;
import org.wpilib.math.util.MathUtil;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;

import java.util.Set;
import java.util.function.Supplier;

import static org.wpilib.units.Units.*;

public class Drive implements Mechanism {

    private final SwerveDrivetrain<TalonFX, TalonFX, CANcoder> swerve;

    private final SwerveDrivetrainConstants constants;
    private final SwerveModuleConstantsFactory<ParentConfiguration, ParentConfiguration, ParentConfiguration> moduleConstantsFactory;

    private final double DRIVE_WIDTH = Units.inchesToMeters(22.729228);
    private final double DRIVE_LENGTH = Units.inchesToMeters(22.729228);

    private static final double MAX_VELOCITY = Units.feetToMeters(14.9);
    private static final double MAX_ACCELERATION = Units.feetToMeters(8);

    public Drive() {
        constants = new SwerveDrivetrainConstants().withCANBusName("can_s0").withPigeon2Id(4);

        moduleConstantsFactory = new SwerveModuleConstantsFactory<>()
                .withDriveMotorGearRatio(7.03)
                .withSteerMotorGearRatio(26.09090909090909)
                .withCouplingGearRatio(3.857142857142857)
                .withWheelRadius(Units.inchesToMeters(2))
//                .withSteerMotorGains(new Slot0Configs()
//                        .withKP(0.05).withKD(0)
//                        .withKS(0.2).withKV(0.72972)
//                        .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign))
                .withSteerMotorGains(new Slot0Configs()
                        .withKP(100).withKI(0).withKD(0.5)
                        .withKS(0.1).withKV(2.49).withKA(0)
                        .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign))
                .withDriveMotorGains(new Slot0Configs()
                        .withKP(8).withKD(0)
                        .withKS(0.2))
//                .withDriveMotorGains(new Slot0Configs()
//                        .withKP(0.3).withKI(0).withKD(0)
//                        .withKS(0.2).withKV(0.13))
                .withSteerMotorClosedLoopOutput(SwerveModuleConstants.ClosedLoopOutputType.Voltage)
                .withDriveMotorClosedLoopOutput(SwerveModuleConstants.ClosedLoopOutputType.Voltage)
                .withSlipCurrent(Amps.of(45))
                .withSpeedAt12Volts(MAX_VELOCITY)
                .withDriveMotorType(SwerveModuleConstants.DriveMotorArrangement.TalonFX_Integrated)
                .withSteerMotorType(SwerveModuleConstants.SteerMotorArrangement.TalonFX_Integrated)
                .withFeedbackSource(SwerveModuleConstants.SteerFeedbackType.RemoteCANcoder)
                .withDriveMotorInitialConfigs(new TalonFXConfiguration())
                .withSteerMotorInitialConfigs(new TalonFXConfiguration())
                .withEncoderInitialConfigs(new CANcoderConfiguration())
                .withSteerInertia(KilogramSquareMeters.of(0.01))
                .withDriveInertia(KilogramSquareMeters.of(0.03))
                .withSteerFrictionVoltage(Volts.of(0.1))
                .withDriveFrictionVoltage(Volts.of(0.2));

        var moduleConstants = new SwerveModuleConstants[] {
                moduleConstantsFactory.createModuleConstants(10, 11, 10, 0, DRIVE_LENGTH/2, DRIVE_WIDTH/2, false, false, false),
                moduleConstantsFactory.createModuleConstants(12, 13, 12, 0, DRIVE_LENGTH/2, -DRIVE_WIDTH/2, false, false, false),
                moduleConstantsFactory.createModuleConstants(14, 15, 14, 0, -DRIVE_LENGTH/2, DRIVE_WIDTH/2, false, false, false),
                moduleConstantsFactory.createModuleConstants(16, 17, 16, 0, -DRIVE_LENGTH/2, -DRIVE_WIDTH/2, false, false, false)
        };

        swerve = new SwerveDrivetrain<>(TalonFX::new, TalonFX::new, CANcoder::new, constants, moduleConstants);

        Scheduler.getDefault().addPeriodic(this::periodic);
    }

    public void periodic() {
        // Update sim 5x per loop for greater PID realism
        swerve.updateSimState(1.0/50.0/5.0, 12);
        swerve.updateSimState(1.0/50.0/5.0, 12);
        swerve.updateSimState(1.0/50.0/5.0, 12);
        swerve.updateSimState(1.0/50.0/5.0, 12);
        swerve.updateSimState(1.0/50.0/5.0, 12);

        Telemetry.log("Drive/Pose", getPose());

        var modules = swerve.getModules();
        SwerveModulePosition[] modulePositions = new SwerveModulePosition[4];
        for (int i = 0; i < modules.length; i++) {
            modulePositions[i] = modules[i].getPosition(true);
            Telemetry.log("Drive/ModuleVel/"+i, modules[i].getDriveMotor().getVelocity().getValueAsDouble());
        }

        Telemetry.log("Drive/ModulesReal", swerve.getState().ModuleVelocities);
        Telemetry.log("Drive/ModulesTargets", swerve.getState().ModuleTargets);
        Telemetry.log("Drive/Velocity", swerve.getState().Velocity);
        Telemetry.log("Drive/Active Commands", getRunningCommands().toString());
    }

    /**
     * @return the current pose of the drivetrain
     */
    public Pose2d getPose() {
        return swerve.getState().Pose;
    }

    /**
     * @return the current heading of the drivetrain
     */
    public Rotation2d getHeading() {
        return getPose().getRotation();
    }


    /**
     * @return Field-relative robot velocity
     */
    public ChassisVelocities getVelocity() {
        return swerve.getState().Velocity.toFieldRelative(getHeading());
    }

    /**
     * Creates a teleop drive command
     * @param controller the controller to use for inputs, the left stick controls translation, while the right stick controls rotation
     * @return a command
     */
    public Command getDriveCommand(CommandXboxController controller) {
        return runRepeatedly(() -> {
            double x = -MathUtil.applyDeadband(controller.getLeftY(), 0.1) * MAX_VELOCITY;
            double y = -MathUtil.applyDeadband(controller.getLeftX(), 0.1) * MAX_VELOCITY;
            double rotation = -MathUtil.applyDeadband(controller.getRightX(), 0.10) * 10;

            var request = new SwerveRequest.FieldCentric();
            request.withForwardPerspective(SwerveRequest.ForwardPerspectiveValue.OperatorPerspective);
            request.withVelocityX(x);
            request.withVelocityY(y);
            request.withRotationalRate(rotation);
            request.withDeadband(0.1*4.54);

            swerve.setControl(request);

            Telemetry.log("Drive/DesiredVelocity", new ChassisVelocities(x, y, rotation).toRobotRelative(getHeading()));
        }).named("Teleop Drive");
    }

    public static class AutoAlignCommand implements Command {

        private final Drive drive;

        private final SwerveRequest.ApplyFieldVelocity swerveRequest = new SwerveRequest.ApplyFieldVelocity();;

        private Pose2d targetPose = new Pose2d();
        private Supplier<Pose2d> poseSupplier = () -> {return new Pose2d();};
        private boolean usePoseSupplier = false;

        private double accelerationLimit = MAX_ACCELERATION;
        private double velocityLimit = MAX_VELOCITY;
        private double positionTolerance = Units.inchesToMeters(2);
        private double angleTolerance = Units.degreesToRadians(3);
        private boolean runContinuously = false;
        private boolean ignoreRotation = false;
        private double targetVelocity = 0;

        private double angularVelocityLimit = 5;
        private double angularAccelerationLimit = 45;

        private ProfiledPIDController headingController;

        public AutoAlignCommand(Drive drive, Pose2d target) {
            this.drive = drive;
            this.targetPose = target;
            this.usePoseSupplier = false;

            headingController = new ProfiledPIDController(6, 0, 0.2, new TrapezoidProfile.Constraints(angularVelocityLimit, angularAccelerationLimit));
            headingController.enableContinuousInput(-Math.PI, Math.PI);
        }

        public AutoAlignCommand(Drive drive, Supplier<Pose2d> poseSupplier) {
            this.drive = drive;
            this.poseSupplier = poseSupplier;
            this.usePoseSupplier = true;

            headingController = new ProfiledPIDController(6, 0, 0.2, new TrapezoidProfile.Constraints(angularVelocityLimit, angularAccelerationLimit));
            headingController.enableContinuousInput(-Math.PI, Math.PI);
        }

        @Override
        public void run(Coroutine coroutine) {
            if (usePoseSupplier) {
                targetPose = poseSupplier.get();
            }

            headingController.reset(drive.getHeading().getRadians(), drive.getVelocity().omega);

            while (runContinuously || !atPosition()) {
                var currentPose = drive.getPose();

                if (usePoseSupplier) {
                    targetPose = poseSupplier.get();
                }

                Telemetry.log("Drive/AutoAlign/At Target", atPosition());
                Telemetry.log("Drive/AutoAlign/Position in Tolerance", targetPose.getTranslation().getDistance(drive.getPose().getTranslation()) <= positionTolerance);
                Telemetry.log("Drive/AutoAlign/Angle in Tolerance", (ignoreRotation || Math.abs(targetPose.getRotation().minus(drive.getPose().getRotation()).getRadians()) <= angleTolerance));

                Telemetry.log("Drive/AutoAlign/Target Pose", targetPose);

                Translation2d vectorToTarget = targetPose.getTranslation().minus(currentPose.getTranslation());
                double distanceToEnd = vectorToTarget.getNorm();

                Telemetry.log("Drive/AutoAlign/Distance", distanceToEnd);

//                ChassisVelocities currentVelocity = drive.getVelocity();
//                Translation2d currentVelocityAsTranslation = new Translation2d(currentVelocity.vx, currentVelocity.vy);
//
//                double velocityTowardsTarget = vectorToTarget.div(distanceToEnd).dot(currentVelocityAsTranslation);
//
//                double maxDistanceForCurrentSpeed = (Math.pow(targetVelocity,2) - Math.pow(velocityTowardsTarget,2)) / (2 * -accelerationLimit);

                double velocityForDistance = Math.pow(targetVelocity,2) - 2*-accelerationLimit*distanceToEnd;

                Telemetry.log("Drive/AutoAlign/Velocity for Distance", velocityForDistance);

                double velocityToTarget = Math.min(velocityForDistance * 0.85, velocityLimit);

                Telemetry.log("Drive/AutoAlign/Targeted Velocity", velocityToTarget);

                Translation2d velocities = vectorToTarget.div(distanceToEnd).times(velocityToTarget);



                double omega = 0;

                if(!ignoreRotation) {
                    omega = headingController.calculate(currentPose.getRotation().getRadians(), targetPose.getRotation().getRadians());
                }

                ChassisVelocities chassisVelocities = new ChassisVelocities(velocities.getX(), velocities.getY(), omega);

                Telemetry.log("Drive/DesiredVelocity", chassisVelocities);

                swerveRequest.withVelocity(chassisVelocities);
                drive.swerve.setControl(swerveRequest);

                coroutine.yield();
            }

            // If command ends naturally, have the robot stop
            swerveRequest.withVelocity(new ChassisVelocities());
            drive.swerve.setControl(swerveRequest);
        }

        @Override
        public String name() {
            return "AutoAlign";
        }

        @Override
        public Set<Mechanism> requirements() {
            return Set.of(drive);
        }

        /**
         * Sets a static target pose for the command
         * @param pose The pose to target
         * @return this command
         */
        public AutoAlignCommand withTargetPose(Pose2d pose) {
            this.targetPose = pose;
            this.usePoseSupplier = false;
            return this;
        }

        /**
         * Sets a pose supplier for the command to use, which will be evaluated continuously
         * @param supplier The supplier to use for the pose target
         * @return this command
         */
        public AutoAlignCommand withTargetPoseSupplier(Supplier<Pose2d> supplier) {
            this.poseSupplier = supplier;
            this.usePoseSupplier = true;
            return this;
        }

        /**
         * Sets the maximum allowed acceleration for the robot to use
         * @param accelerationLimit The maximum allowed acceleration
         * @return this command
         */
        public AutoAlignCommand withAccelerationLimit(double accelerationLimit) {
            this.accelerationLimit = accelerationLimit;
            return this;
        }

        /**
         * Sets the maximum allowed velocity for the robot to use
         * @param velocityLimit The maximum allowed velocity
         * @return this command
         */
        public AutoAlignCommand withVelocityLimit(double velocityLimit) {
            this.velocityLimit = velocityLimit;
            return this;
        }

        /**
         * Sets the tolerance for the final position of the robot. Used to determine whether
         * @param tolerance
         * @return
         */
        public AutoAlignCommand withPositionTolerance(double tolerance) {
            this.positionTolerance = tolerance;
            return this;
        }

        public AutoAlignCommand withAngleTolerance(double tolerance) {
            this.angleTolerance = tolerance;
            return this;
        }

        public AutoAlignCommand withRunningContinuously(boolean runContinuously) {
            this.runContinuously = runContinuously;
            return this;
        }

        public AutoAlignCommand withIgnoringRotation(boolean ignoringRotation) {
            this.ignoreRotation = ignoringRotation;
            return this;
        }

        public AutoAlignCommand withTargetVelocity(double targetVelocity) {
            this.targetVelocity = targetVelocity;
            return this;
        }

        public AutoAlignCommand withAngularAccelerationLimit(double angularAccelerationLimit) {
            this.angularAccelerationLimit = angularAccelerationLimit;
            return this;
        }

        public AutoAlignCommand withAngularVelocityLimit(double angularVelocityLimit) {
            this.angularVelocityLimit = angularVelocityLimit;
            return this;
        }

        public boolean atPosition() {
            return targetPose.getTranslation().getDistance(drive.getPose().getTranslation()) <= positionTolerance && (ignoreRotation || Math.abs(targetPose.getRotation().minus(drive.getPose().getRotation()).getRadians()) <= angleTolerance);
        }

        public Command waitUntilAtPosition() {
            return Command.waitUntil(this::atPosition).named("Wait Until At Position");
        }
    }
}
