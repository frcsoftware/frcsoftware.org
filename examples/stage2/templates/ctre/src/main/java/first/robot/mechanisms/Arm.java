package first.robot.mechanisms;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import first.robot.simulation.ArmSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public class Arm implements Mechanism {

    private static class Constants {
        static final int MOTOR_ID = 22;
        static final CANBus BUS = CANBus.systemcore(1);

        static final InvertedValue MOTOR_INVERSION = InvertedValue.Clockwise_Positive;

        static final double GEAR_RATIO = (56.0 / 16.0) * (84.0 / 10.0); // Two stage, 56:16, 84:10 reductions

        static final double kS = 0;
        static final double kG = 0.777;
        static final double kV = 3.464;
        static final double kA = 0.192;
        static final double kP = 60;
        static final double kI = 0;
        static final double kD = 7;

        static final double POSITION_TOLERANCE = Units.degreesToRotations(3);
    }

    private TalonFX motor;
    private ArmSim sim;

    private VoltageOut voltageRequest;
    private PositionVoltage positionRequest;

    private StatusSignal<Voltage> appliedVoltageSignal;
    private StatusSignal<Angle> positionSignal;
    private StatusSignal<AngularVelocity> velocitySignal;
    private StatusSignal<Current> statorCurrentSignal;
    private StatusSignal<Current> supplyCurrentSignal;

    private BaseStatusSignal[] signals;

    private double setpoint = 0;

    public Arm() {
        // Construct and configure motors
        motor = new TalonFX(Constants.MOTOR_ID, Constants.BUS);

        TalonFXConfiguration configuration = new TalonFXConfiguration();
        configuration.MotorOutput.withNeutralMode(NeutralModeValue.Brake)
                .withInverted(Constants.MOTOR_INVERSION);
        configuration.Feedback.withSensorToMechanismRatio(Constants.GEAR_RATIO);
        configuration.Slot0.withKS(Constants.kS)
                .withKG(Constants.kG)
                .withKV(Constants.kV)
                .withKA(Constants.kA)
                .withKP(Constants.kP)
                .withKI(Constants.kI)
                .withKD(Constants.kD)
                .withGravityType(GravityTypeValue.Arm_Cosine);

        CurrentLimitsConfigs currentLimitsConfigs = new CurrentLimitsConfigs();
        currentLimitsConfigs.withStatorCurrentLimit(60)
                .withStatorCurrentLimitEnable(true)
                .withSupplyCurrentLimit(60)
                .withSupplyCurrentLimitEnable(true);

        configuration.withCurrentLimits(currentLimitsConfigs);

        motor.getConfigurator().apply(configuration);

        // Set up periodic method to run every code loop
        Scheduler.getDefault().addPeriodic(this::periodic);

        // Set up simulation
        sim = new ArmSim(motor);

        // Set up requests
        voltageRequest = new VoltageOut(0);
        positionRequest = new PositionVoltage(0);

        // Set up status signals
        appliedVoltageSignal = motor.getMotorVoltage();
        positionSignal = motor.getPosition();
        velocitySignal = motor.getVelocity();
        statorCurrentSignal = motor.getStatorCurrent();
        supplyCurrentSignal = motor.getSupplyCurrent();

        signals = new BaseStatusSignal[] {appliedVoltageSignal, positionSignal, velocitySignal,
                statorCurrentSignal, supplyCurrentSignal};
    }

    private void periodic() {
        // Update simulation
        sim.periodic();

        BaseStatusSignal.refreshAll(signals);

        Telemetry.log("Arm/Applied Voltage", getAppliedVoltage());
        Telemetry.log("Arm/Position", getPosition());
        Telemetry.log("Arm/Velocity", getVelocity());
        Telemetry.log("Arm/At Setpoint", isAtSetpoint());
        Telemetry.log("Arm/Active Commands", getRunningCommands().toString());
    }

    /**
     * @param voltage the voltage to apply to the motor
     * @return a command
     */
    public Command setVoltage(double voltage) {
        return run(coro -> {
            motor.setControl(voltageRequest.withOutput(voltage));
        }).named("Set Voltage " + voltage + "V");
    }

    /**
     * @param position the position for the arm to target, in rotations
     * @return a command
     */
    public Command setPosition(double position) {
        return run(coro -> {
            setpoint = position;
            motor.setControl(positionRequest.withPosition(position));
            Telemetry.log("Arm/Setpoint", position);

            coro.waitUntil(() -> isAtPosition(position));
        }).named("Set Position " + position + "rot");
    }

    /**
     * @return the current position of the arm, in rotations
     */
    public double getPosition() {
        return positionSignal.getValueAsDouble();
    }

    /**
     * @return the current velocity, in rotations per second
     */
    public double getVelocity() {
        return velocitySignal.getValueAsDouble();
    }

    /**
     * @return the current voltage being applied to the arm motor
     */
    public double getAppliedVoltage() {
        return appliedVoltageSignal.getValueAsDouble();
    }

    /**
     * @param position the position to compare against, in rotations
     * @return whether the arm is at that positoin
     */
    public boolean isAtPosition(double position) {
        return Math.abs(getPosition() - position) < Constants.POSITION_TOLERANCE;
    }

    /**
     * @return whether the arm is at its current setpoint
     */
    public boolean isAtSetpoint() {
        return isAtPosition(setpoint);
    }
}
