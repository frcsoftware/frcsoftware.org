/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.mechanisms;

import static org.wpilib.units.Units.*;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.ParentConfiguration;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.StaticFeedforwardSignValue;
import com.ctre.phoenix6.swerve.*;
import java.util.Set;
import java.util.function.Supplier;
import org.wpilib.command3.*;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.driverstation.Alliance;
import org.wpilib.driverstation.MatchState;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.math.controller.ProfiledPIDController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.math.trajectory.TrapezoidProfile;
import org.wpilib.math.util.MathUtil;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryTable;

public class Drive implements Mechanism {

  private final SwerveDrivetrain<TalonFX, TalonFX, CANcoder> swerve;

  private final SwerveDrivetrainConstants constants;
  private final SwerveModuleConstantsFactory<
          ParentConfiguration, ParentConfiguration, ParentConfiguration>
      moduleConstantsFactory;

  private final double DRIVE_WIDTH = Units.inchesToMeters(22.729228);
  private final double DRIVE_LENGTH = Units.inchesToMeters(22.729228);

  private static final double MAX_VELOCITY = Units.feetToMeters(14.9);
  private static final double MAX_ACCELERATION = 5;

  private Pose2d currentPose = new Pose2d();

  private static final TelemetryTable table = Telemetry.getTable("Drive");
  private static final TelemetryTable autoAlignTable = table.getTable("AutoAlign");

  public Drive() {
    constants =
        new SwerveDrivetrainConstants().withNetwork(new CANBus(CANPort.CAN_S0)).withPigeon2Id(4);

    moduleConstantsFactory =
        new SwerveModuleConstantsFactory<>()
            .withDriveMotorGearRatio((54.0 / 12.0) * (25.0 / 35.0) * (30.0 / 15.0))
            .withSteerMotorGearRatio(287.0 / 11.0)
            .withCouplingGearRatio(3.857142857142857)
            .withWheelRadius(Units.inchesToMeters(2))
            .withSteerMotorGains(
                new Slot0Configs()
                    .withKP(100)
                    .withKI(0)
                    .withKD(0.5)
                    .withKS(0.1)
                    .withKV(2.49)
                    .withKA(0)
                    .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign))
            .withDriveMotorGains(
                new Slot0Configs().withKP(0.3).withKI(0).withKD(0).withKS(0.2).withKV(0.125))
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

    var moduleConstants =
        new SwerveModuleConstants[] {
          moduleConstantsFactory.createModuleConstants(
              10, 11, 10, 0, DRIVE_LENGTH / 2, DRIVE_WIDTH / 2, false, false, false),
          moduleConstantsFactory.createModuleConstants(
              12, 13, 12, 0, DRIVE_LENGTH / 2, -DRIVE_WIDTH / 2, false, false, false),
          moduleConstantsFactory.createModuleConstants(
              14, 15, 14, 0, -DRIVE_LENGTH / 2, DRIVE_WIDTH / 2, false, false, false),
          moduleConstantsFactory.createModuleConstants(
              16, 17, 16, 0, -DRIVE_LENGTH / 2, -DRIVE_WIDTH / 2, false, false, false)
        };

    swerve =
        new SwerveDrivetrain<>(
            TalonFX::new, TalonFX::new, CANcoder::new, constants, moduleConstants);
  }

  public void periodic() {
    // Update sim 5x per loop for greater PID realism
    swerve.updateSimState(1.0 / 50.0 / 5.0, 12);
    swerve.updateSimState(1.0 / 50.0 / 5.0, 12);
    swerve.updateSimState(1.0 / 50.0 / 5.0, 12);
    swerve.updateSimState(1.0 / 50.0 / 5.0, 12);
    swerve.updateSimState(1.0 / 50.0 / 5.0, 12);

    table.log("Pose", getPose());
    table.log("ModulesReal", swerve.getState().ModuleVelocities);
    table.log("ModulesTargets", swerve.getState().ModuleTargets);
    table.log("Velocity", swerve.getState().Velocity);
    table.log("Active Commands", getRunningCommands().toString());
  }

  public void setPose(Pose2d pose) {
    swerve.resetPose(pose);
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
   *
   * @param controller the controller to use for inputs, the left stick controls translation, while
   *     the right stick controls rotation
   * @return a command
   */
  public Command getDriveCommand(CommandXboxController controller) {
    return runRepeatedly(
            () -> {
              double x = -MathUtil.applyDeadband(controller.getLeftY(), 0.1) * MAX_VELOCITY;
              double y = -MathUtil.applyDeadband(controller.getLeftX(), 0.1) * MAX_VELOCITY;
              double rotation = -MathUtil.applyDeadband(controller.getRightX(), 0.10) * 10;

              var request = new SwerveRequest.FieldCentric();
              request.withForwardPerspective(
                  SwerveRequest.ForwardPerspectiveValue.OperatorPerspective);

              // TODO: Remove when fixed
              if (MatchState.getAlliance().isPresent()
                  && MatchState.getAlliance().get() == Alliance.RED) {
                request.withVelocityX(x);
                request.withVelocityY(y);
              } else {
                request.withVelocityX(-x);
                request.withVelocityY(-y);
              }

              request.withRotationalRate(rotation);
              request.withDeadband(0.1 * MAX_VELOCITY);

              swerve.setControl(request);

              table.log(
                  "DesiredVelocity",
                  new ChassisVelocities(x, y, rotation).toRobotRelative(getHeading()));
            })
        .named("Teleop Drive");
  }

  public Command stop() {
    return run((coro) -> swerve.setControl(new SwerveRequest.ApplyRobotVelocity()))
        .named("Stop Drive");
  }

  public static class AutoAlignCommand implements Command {

    private final Drive drive;

    private final SwerveRequest.ApplyFieldVelocity swerveRequest =
        new SwerveRequest.ApplyFieldVelocity()
            .withDriveRequestType(SwerveModule.DriveRequestType.Velocity);

    private Pose2d targetPose = new Pose2d();
    private Supplier<Pose2d> poseSupplier;

    private double accelerationLimit = MAX_ACCELERATION;
    private double velocityLimit = MAX_VELOCITY;
    private double positionTolerance = Units.inchesToMeters(2);
    private double angleTolerance = Units.degreesToRadians(3);
    private boolean runContinuously = false;
    private boolean ignoreRotation = false;
    private double targetVelocity = 0;

    private double angularVelocityLimit = 5;
    private double angularAccelerationLimit = 45;

    private final ProfiledPIDController headingController =
        new ProfiledPIDController(
            6,
            0,
            0.2,
            new TrapezoidProfile.Constraints(angularVelocityLimit, angularAccelerationLimit));

    public AutoAlignCommand(Drive drive, Pose2d target) {
      this(drive, () -> target);
    }

    public AutoAlignCommand(Drive drive, Supplier<Pose2d> poseSupplier) {
      this.drive = drive;
      this.poseSupplier = poseSupplier;

      headingController.enableContinuousInput(-Math.PI, Math.PI);
    }

    @Override
    public void run(Coroutine coroutine) {
      targetPose = poseSupplier.get();

      headingController.reset(drive.getHeading().getRadians(), drive.getVelocity().omega);

      Translation2d lastVelocityVector =
          new Translation2d(drive.getVelocity().vx, drive.getVelocity().vy);
      Translation2d lastRealVelocity =
          new Translation2d(drive.getVelocity().vx, drive.getVelocity().vy);

      while (runContinuously || !atPosition()) {
        var currentPose = drive.getPose();

        targetPose = poseSupplier.get();

        autoAlignTable.log("At Target", atPosition());
        autoAlignTable.log(
            "Position in Tolerance",
            targetPose.getTranslation().getDistance(drive.getPose().getTranslation())
                <= positionTolerance);
        autoAlignTable.log(
            "Angle in Tolerance",
            (ignoreRotation
                || Math.abs(
                        targetPose.getRotation().minus(drive.getPose().getRotation()).getRadians())
                    <= angleTolerance));

        autoAlignTable.log("Target Pose", targetPose);

        Translation2d vectorToTarget =
            targetPose.getTranslation().minus(currentPose.getTranslation());
        double distanceToEnd = vectorToTarget.getNorm();

        autoAlignTable.log("Distance", Meters.of(distanceToEnd));

        //                ChassisVelocities currentVelocity = drive.getVelocity();
        //                Translation2d currentVelocityAsTranslation = new
        // Translation2d(currentVelocity.vx, currentVelocity.vy);
        //
        //                double velocityTowardsTarget =
        // vectorToTarget.div(distanceToEnd).dot(currentVelocityAsTranslation);
        //
        //                double maxDistanceForCurrentSpeed = (Math.pow(targetVelocity,2) -
        // Math.pow(velocityTowardsTarget,2)) / (2 * -accelerationLimit);

        double velocityForDistance =
            Math.sqrt(Math.pow(targetVelocity, 2) - 2 * -accelerationLimit * distanceToEnd);

        autoAlignTable.log("Velocity for Distance", MetersPerSecond.of(velocityForDistance));

        double velocityToTarget = Math.min(velocityForDistance * 0.85, velocityLimit);

        autoAlignTable.log("Targeted Velocity", MetersPerSecond.of(velocityToTarget));

        Translation2d velocities = vectorToTarget.div(distanceToEnd).times(velocityToTarget);

        lastVelocityVector =
            MathUtil.slewRateLimit(lastVelocityVector, velocities, 0.02, accelerationLimit);

        double omega = 0;

        if (!ignoreRotation) {
          omega =
              headingController.calculate(
                  currentPose.getRotation().getRadians(), targetPose.getRotation().getRadians());
        }

        ChassisVelocities chassisVelocities =
            new ChassisVelocities(lastVelocityVector.getX(), lastVelocityVector.getY(), omega);

        table.log("DesiredVelocity", chassisVelocities);

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
     *
     * @param pose The pose to target
     * @return this command
     */
    public AutoAlignCommand withTargetPose(Pose2d pose) {
      this.targetPose = pose;
      return this;
    }

    /**
     * Sets a pose supplier for the command to use, which will be evaluated continuously
     *
     * @param supplier The supplier to use for the pose target
     * @return this command
     */
    public AutoAlignCommand withTargetPoseSupplier(Supplier<Pose2d> supplier) {
      this.poseSupplier = supplier;
      return this;
    }

    /**
     * Sets the maximum allowed acceleration for the robot to use
     *
     * @param accelerationLimit The maximum allowed acceleration
     * @return this command
     */
    public AutoAlignCommand withAccelerationLimit(double accelerationLimit) {
      this.accelerationLimit = accelerationLimit;
      return this;
    }

    /**
     * Sets the maximum allowed velocity for the robot to use
     *
     * @param velocityLimit The maximum allowed velocity
     * @return this command
     */
    public AutoAlignCommand withVelocityLimit(double velocityLimit) {
      this.velocityLimit = velocityLimit;
      return this;
    }

    /**
     * Sets the tolerance for the final position of the robot. Used to determine whether the command
     * is at its target position or not
     *
     * @param tolerance The desired position tolerance, in meters
     * @return this command
     */
    public AutoAlignCommand withPositionTolerance(double tolerance) {
      this.positionTolerance = tolerance;
      return this;
    }

    /**
     * Sets the angle tolerance for the final angle of the robot. User to determine whether the
     * command is at its target position or not
     *
     * @param tolerance The desired angle tolerance, in radians
     * @return this command
     */
    public AutoAlignCommand withAngleTolerance(double tolerance) {
      this.angleTolerance = tolerance;
      return this;
    }

    /**
     * Sets whether the command will run continuously and never end. If true, the command will never
     * end, and must be ended by other means. If false, the command will end when the drivetrain is
     * within the specified position and angle tolerances
     *
     * @param runContinuously whether the command should run continuously
     * @return this command
     */
    public AutoAlignCommand withRunContinuously(boolean runContinuously) {
      this.runContinuously = runContinuously;
      return this;
    }

    /**
     * Sets whether the command will ignore the rotation of the pose or pose supplier. If true, the
     * robot will not command a turn, and the angle tolerance will be ignored
     *
     * @param ignoringRotation whether to ignore rotation
     * @return this command
     */
    public AutoAlignCommand withIgnoreRotation(boolean ignoringRotation) {
      this.ignoreRotation = ignoringRotation;
      return this;
    }

    /**
     * Sets the targeted final velocity of the command. The robot will move in such a way that it
     * tries to meet this final velocity at the final setpoint
     *
     * @param targetVelocity the desired final velocity
     * @return this command
     */
    public AutoAlignCommand withTargetVelocity(double targetVelocity) {
      this.targetVelocity = targetVelocity;
      return this;
    }

    /**
     * Sets the limit on the angular acceleration of the robot during the move
     *
     * @param angularAccelerationLimit the desired limit
     * @return this command
     */
    public AutoAlignCommand withAngularAccelerationLimit(double angularAccelerationLimit) {
      this.angularAccelerationLimit = angularAccelerationLimit;
      return this;
    }

    /**
     * Sets the limit on the angular velocity during the move
     *
     * @param angularVelocityLimit the desired limit
     * @return this command
     */
    public AutoAlignCommand withAngularVelocityLimit(double angularVelocityLimit) {
      this.angularVelocityLimit = angularVelocityLimit;
      return this;
    }

    /**
     * Getter for whether the robot is at the targeted position and within the specified tolerance.
     * Ignores the runContinuously flag
     *
     * @return whether the robot is at the targeted position
     */
    public boolean atPosition() {
      return targetPose.getTranslation().getDistance(drive.getPose().getTranslation())
              <= positionTolerance
          && (ignoreRotation
              || Math.abs(
                      targetPose.getRotation().minus(drive.getPose().getRotation()).getRadians())
                  <= angleTolerance);
    }
  }
}
