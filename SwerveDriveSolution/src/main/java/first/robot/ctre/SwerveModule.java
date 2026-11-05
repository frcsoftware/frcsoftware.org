package first.robot.ctre;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.ClosedLoopGeneralConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import first.robot.SwerveDrivetrain;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.math.kinematics.SwerveModuleVelocity;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.DCMotorSim;

public class SwerveModule {

  /** Motor for controlling the module velocity */
  private final TalonFX driveMotor;

  /** Motor for controlling the module angle */
  private final TalonFX steerMotor;

  /** Encoder mounted to the steer column */
  private final CANcoder cancoder;

  /** Radius of module wheel in meters */
  private static final double wheelRadius = Units.inchesToMeters(2.0);

  /**
   * Maximum speed of the module in rotations per second.
   *
   * <p>3.5 m/s divided by radius gives radians per second
   */
  private static final double maxSpeedRps = Units.radiansToRotations(SwerveDrivetrain.MAX_SPEED / wheelRadius);

  /** Control request for steer motor position */
  private final PositionVoltage steerRequest = new PositionVoltage(0.0);

  /** Control request for drive motor velocity */
  private final VelocityVoltage driveRequest = new VelocityVoltage(0.0);

  /**
   * Construct a SwerveModule
   *
   * @param driveId CAN id of the drive motor
   * @param steerId CAN id of the steer motor
   * @param cancoderId CAN id of the cancoder
   */
  public SwerveModule(int driveId, int steerId, int cancoderId) {
    // Create devices with given ids on the 0 bus
    this.driveMotor = new TalonFX(driveId, CANBus.systemcore(0));
    this.steerMotor = new TalonFX(steerId, CANBus.systemcore(0));
    this.cancoder = new CANcoder(cancoderId, CANBus.systemcore(0));

    driveMotor
        .getConfigurator()
        .apply(
            new TalonFXConfiguration()
                .withSlot0(new Slot0Configs().withKV(12.0 / maxSpeedRps))
                .withFeedback(new FeedbackConfigs().withSensorToMechanismRatio(6.0)));
    steerMotor
        .getConfigurator()
        .apply(
            new TalonFXConfiguration()
                .withSlot0(new Slot0Configs().withKP(100.0).withKD(2.0))
                .withFeedback(
                    new FeedbackConfigs().withFusedCANcoder(cancoder).withRotorToSensorRatio(24.0))
                .withClosedLoopGeneral(new ClosedLoopGeneralConfigs().withContinuousWrap(true)));
  }

  /** Get the SwerveModulePosition of the module */
  public SwerveModulePosition getPosition() {
    return new SwerveModulePosition(
        Units.rotationsToRadians(driveMotor.getPosition().getValueAsDouble()) * wheelRadius,
        getAngle());
  }

  /** Applies a velocity setpoint to the motors. */
  public void setVelocity(SwerveModuleVelocity velocity) {
    // Optimze target velocity based on current module angle
    SwerveModuleVelocity optimized = velocity.optimize(getAngle());
    // Apply angle to steer motor
    steerMotor.setControl(steerRequest.withPosition(optimized.angle.getMeasure()));
    // Convert m/s to rps
    double rotationsPerSecond = Units.radiansToRotations(optimized.velocity / wheelRadius);
    // Apply speed to drive motor, with cosine compensation
    driveMotor.setControl(
        driveRequest.withVelocity(rotationsPerSecond * optimized.angle.minus(getAngle()).getCos()));
  }

  /** Return the angle of the wheel */
  public Rotation2d getAngle() {
    return Rotation2d.fromRotations(steerMotor.getPosition().getValueAsDouble());
  }

  /** Return a SwerveModuleVelocity object representing this modules velocity */
  public SwerveModuleVelocity getVelocity() {
    return new SwerveModuleVelocity(
        Units.rotationsToRadians(driveMotor.getVelocity().getValueAsDouble()) * wheelRadius,
        getAngle());
  }

  /**
   * Physics-simulated drive mechanism.
   *
   * <p>Single Jointed Arm is used becuase it properly represents a mechanism with angular position
   * and velocity states.
   */
  private final DCMotorSim driveSim =
      new DCMotorSim(
          Models.singleJointedArmFromPhysicalConstants(DCMotor.getKrakenX60(1), 0.01, 6.0),
          DCMotor.getKrakenX60(1));

  /** Physics-simulated steer mechanism. */
  private final DCMotorSim steerSim =
      new DCMotorSim(
          Models.singleJointedArmFromPhysicalConstants(DCMotor.getKrakenX60(1), 0.01, 24.0),
          DCMotor.getKrakenX60(1));

  /** Called periodically while running in simulation */
  public void simulationPeriodic() {
    // Get input from talonfx sim, and apply to mechanism
    driveSim.setInputVoltage(driveMotor.getSimState().getMotorVoltage());
    steerSim.setInputVoltage(steerMotor.getSimState().getMotorVoltage());

    // Simulate the mechanisms for a single tick
    driveSim.update(0.02);
    steerSim.update(0.02);

    // Update the talonfx and cancoder sim states with calculations from physics sim.
    // The sims do not account for gear ratio, so each calculation is multiplied by the gear ratio.
    driveMotor
        .getSimState()
        .setRotorVelocity(Units.radiansToRotations(driveSim.getAngularVelocity()) * 6.0);
    driveMotor
        .getSimState()
        .setRawRotorPosition(Units.radiansToRotations(driveSim.getAngularPosition()) * 6.0);

    steerMotor
        .getSimState()
        .setRotorVelocity(Units.radiansToRotations(steerSim.getAngularVelocity()) * 24.0);
    steerMotor
        .getSimState()
        .setRawRotorPosition(Units.radiansToRotations(steerSim.getAngularPosition()) * 24.0);

    cancoder.getSimState().setVelocity(Units.radiansToRotations(steerSim.getAngularVelocity()));
    cancoder.getSimState().setRawPosition(Units.radiansToRotations(steerSim.getAngularPosition()));
  }
}
