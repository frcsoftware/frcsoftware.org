package first.robot;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.Pigeon2;
import first.robot.ctre.SwerveModule;
import org.wpilib.math.estimator.SwerveDrivePoseEstimator;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.math.kinematics.SwerveDriveKinematics;
import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.math.kinematics.SwerveModuleVelocity;
import org.wpilib.math.util.Units;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructArrayPublisher;
import org.wpilib.networktables.StructPublisher;

public class SwerveDrivetrain {

  /** Max translational speed of the robot */
  public static final double MAX_SPEED = 3.5;

  /** Max angular speed of the robot */
  private static final double MAX_ANGULAR_SPEED = Units.rotationsToRadians(.75);

  /** CANbus that the swerve devices are connected to */
  private final CANBus canbus = CANBus.systemcore(0);

  /** Gyro used for determining robot heading */
  private final Pigeon2 gyro = new Pigeon2(1, canbus);

  // Swerve Modules
  /** Front Left Module */
  private final SwerveModule flModule = new SwerveModule(1, 2, 1);

  /** Front Right Module */
  private final SwerveModule frModule = new SwerveModule(3, 4, 2);

  /** Back left module */
  private final SwerveModule blModule = new SwerveModule(5, 6, 3);

  /** Back right module */
  private final SwerveModule brModule = new SwerveModule(7, 8, 4);

  /** Array of swerve modules */
  private final SwerveModule[] modules = {flModule, frModule, blModule, brModule};

  /**
   * Swerve drive kinematics.
   *
   * <p>This is used to calculate module velocities from desired chassis velocities
   */
  private final SwerveDriveKinematics kinematics =
      new SwerveDriveKinematics(
          new Translation2d(Units.inchesToMeters(12), Units.inchesToMeters(12)),
          new Translation2d(Units.inchesToMeters(12), Units.inchesToMeters(-12)),
          new Translation2d(Units.inchesToMeters(-12), Units.inchesToMeters(12)),
          new Translation2d(Units.inchesToMeters(-12), Units.inchesToMeters(-12)));

  /** Pose Estimator */
  private final SwerveDrivePoseEstimator poseEstimator =
      new SwerveDrivePoseEstimator(
          kinematics,
          Rotation2d.kZero,
          new SwerveModulePosition[] {
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition()
          },
          Pose2d.kZero);

  /**
   * Drive the robot using field-centric velocities
   *
   * @param vx Velocity in the field-x direction
   * @param vy Velocity in the field-y direction
   * @param omega Angular velocity about the z-axis.
   */
  public void driveFieldCentric(double vx, double vy, double omega) {
    // Convert field-centric velocities to robot-centric
    ChassisVelocities chassisVelocities =
        new ChassisVelocities(vx * MAX_SPEED, vy * MAX_SPEED, omega * MAX_ANGULAR_SPEED)
            .toRobotRelative(getAngle());
    // Convert robot-centric velocities to individual module velocities
    SwerveModuleVelocity[] moduleVelocities =
        kinematics.toSwerveModuleVelocities(chassisVelocities);
    // Desaturate velocities to prevent requesting a faster speed than is possible for any
    // given module
    SwerveModuleVelocity[] desaturated =
        SwerveDriveKinematics.desaturateWheelVelocities(
            moduleVelocities, chassisVelocities, MAX_SPEED, MAX_SPEED, MAX_ANGULAR_SPEED);

    // Send velocities to the modules
    for (int i = 0; i < modules.length; i++) {
      modules[i].setVelocity(desaturated[i]);
    }
  }

  /** Returns the angle of the robot, from its estimated pose */
  public Rotation2d getAngle() {
    return poseEstimator.getEstimatedPosition().getRotation();
  }

  /** Returns the estimated pose of the robot */
  public Pose2d getPose() {
    return poseEstimator.getEstimatedPosition();
  }

  /** Publisher for publishing robot pose to networktables */
  private final StructPublisher<Pose2d> posePub =
      NetworkTableInstance.getDefault().getStructTopic("pose", Pose2d.struct).publish();

  /** Publisher for publishing robot velocities to networktables */
  private final StructArrayPublisher<SwerveModuleVelocity> moduleStates =
      NetworkTableInstance.getDefault()
          .getStructArrayTopic("modules", SwerveModuleVelocity.struct)
          .publish();

  /** Called periodically, always */
  public void periodic() {
    poseEstimator.update(
        gyro.getRotation2d(),
        new SwerveModulePosition[] {
          flModule.getPosition(),
          frModule.getPosition(),
          blModule.getPosition(),
          brModule.getPosition()
        });
    posePub.set(getPose());
    moduleStates.set(
        new SwerveModuleVelocity[] {
          flModule.getVelocity(),
          frModule.getVelocity(),
          blModule.getVelocity(),
          brModule.getVelocity()
        });
  }

  /** Last angle measured by the gyro. Used for simulating gyro rotation. */
  private double lastAngle = 0.0;

  /** Called periodically while running the robot in simulation */
  public void simulationPeriodic() {
    SwerveModuleVelocity[] velocities = new SwerveModuleVelocity[4];
    for (int i = 0; i < 4; i++) {
      modules[i].simulationPeriodic();
      velocities[i] = modules[i].getVelocity();
    }

    ChassisVelocities inv = kinematics.toChassisVelocities(velocities);
    lastAngle += inv.omega * .02;
    gyro.getSimState().setRawYaw(lastAngle * 180 / Math.PI);
  }
}
