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
import first.robot.simulation.ElevatorSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.math.util.Units;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.units.measure.*;

public class Elevator implements Mechanism {

    private static class Constants {
        static final int LEADER_ID = 20;
        static final int FOLLOWER_ID = 21;
        static final CANBus BUS = CANBus.systemcore(1);

        static final InvertedValue LEADER_INVERSION = InvertedValue.Clockwise_Positive;
        static final MotorAlignmentValue FOLLOWER_ALIGNMENT = MotorAlignmentValue.Opposed;

        static final double GEAR_RATIO = (44.0 / 14.0) * (42.0 / 22.0); // Two stage, 14:44, 22:44 reductions
        static final double PULLEY_CIRCUMFERENCE = 24 * 0.005; // Convert 24t pulley to circumference, in m

        static final double kS = 0;
        static final double kG = 0.26;
        static final double kV = 5.94;
        static final double kA = 0.03;
        static final double kP = 18.3;
        static final double kI = 0;
        static final double kD = 0.7;;

        static final double POSITION_TOLERANCE = Units.inchesToMeters(0.5);
    }

    private final TalonFX leader;
    private final TalonFX follower;

    private final ElevatorSim sim;

    private final VoltageOut voltageRequest;
    private final PositionVoltage positionRequest;

    private final StatusSignal<Voltage> appliedVoltageSignal;
    private final StatusSignal<Angle> positionSignal;
    private final StatusSignal<AngularVelocity> velocitySignal;
    private final StatusSignal<Current> leaderStatorCurrentSignal;
    private final StatusSignal<Current> followerStatorCurrentSignal;
    private final StatusSignal<Current> leaderSupplyCurrentSignal;
    private final StatusSignal<Current> followerSupplyCurrentSignal;

    private final BaseStatusSignal[] signals;

    private double setpoint = 0;

    public Elevator() {
        // Construct and configure motors
        leader = new TalonFX(Constants.LEADER_ID, Constants.BUS);
        follower = new TalonFX(Constants.FOLLOWER_ID, Constants.BUS);

        TalonFXConfiguration leaderConfiguration = new TalonFXConfiguration();
        leaderConfiguration.MotorOutput.withNeutralMode(NeutralModeValue.Brake)
                .withInverted(Constants.LEADER_INVERSION);
        leaderConfiguration.Feedback.withSensorToMechanismRatio(Constants.GEAR_RATIO);
        leaderConfiguration.Slot0.withKS(Constants.kS)
                .withKG(Constants.kG)
                .withKV(Constants.kV)
                .withKA(Constants.kA)
                .withKP(Constants.kP)
                .withKI(Constants.kI)
                .withKD(Constants.kD)
                .withGravityType(GravityTypeValue.Elevator_Static);

        CurrentLimitsConfigs currentLimitsConfigs = new CurrentLimitsConfigs();
        currentLimitsConfigs.withStatorCurrentLimit(60)
                .withStatorCurrentLimitEnable(true)
                .withSupplyCurrentLimit(60)
                .withSupplyCurrentLimitEnable(true);

        leaderConfiguration.withCurrentLimits(currentLimitsConfigs);

        leader.getConfigurator().apply(leaderConfiguration);

        follower.getConfigurator().apply(currentLimitsConfigs);

        follower.setControl(new Follower(leader.getDeviceID(), Constants.FOLLOWER_ALIGNMENT));

        // Set up periodic method to run every code loop
        Scheduler.getDefault().addPeriodic(this::periodic);

        // Set up simulation
        sim = new ElevatorSim(leader, follower);

        // Set up requests
        voltageRequest = new VoltageOut(0);
        positionRequest = new PositionVoltage(0);

        // Set up status signals
        appliedVoltageSignal = leader.getMotorVoltage();
        positionSignal = leader.getPosition();
        velocitySignal = leader.getVelocity();
        leaderStatorCurrentSignal = leader.getStatorCurrent();
        followerStatorCurrentSignal = follower.getStatorCurrent();
        leaderSupplyCurrentSignal = leader.getSupplyCurrent();
        followerSupplyCurrentSignal = follower.getSupplyCurrent();

        signals = new BaseStatusSignal[] {appliedVoltageSignal, positionSignal, velocitySignal,
                leaderStatorCurrentSignal, followerStatorCurrentSignal, leaderSupplyCurrentSignal,
                followerSupplyCurrentSignal};
    }

    private void periodic() {
        // Update simulation
        sim.periodic();

        BaseStatusSignal.refreshAll(signals);

        Telemetry.log("Elevator/Applied Voltage", getAppliedVoltage());
        Telemetry.log("Elevator/Position", getPosition());
        Telemetry.log("Elevator/Velocity", getVelocity());
        Telemetry.log("Elevator/At Setpoint", isAtSetpoint());
        Telemetry.log("Elevator/Active Commands", getRunningCommands().toString());
    }

    /**
     * @param voltage the voltage to apply to the motors
     * @return a command
     */
    public Command setVoltage(double voltage) {
        return run(coro -> {
            leader.setControl(voltageRequest.withOutput(voltage));
        }).named("Set Voltage " + voltage + "V");
    }

    /**
     * @param position the position for the elevator to target, in meters
     * @return a command
     */
    public Command setPosition(double position) {
        return run(coro -> {
            setpoint = position;
            leader.setControl(positionRequest.withPosition(position / Constants.PULLEY_CIRCUMFERENCE));
            Telemetry.log("Elevator/Setpoint", position);

            coro.waitUntil(() -> isAtPosition(position));
        }).named("Set Position " + position + "m");
    }

    /**
     * @return the position of the elevator, in meters
     */
    public double getPosition() {
        return positionSignal.getValueAsDouble() * Constants.PULLEY_CIRCUMFERENCE;
    }

    /**
     * @return the velocity of the elevator, in meters per second
     */
    public double getVelocity() {
        return velocitySignal.getValueAsDouble() * Constants.PULLEY_CIRCUMFERENCE;
    }

    /**
     * @return the current voltage being applied to the arm motor
     */
    public double getAppliedVoltage() {
        return appliedVoltageSignal.getValueAsDouble();
    }

    /**
     * @param position the position to compare against, in meters
     * @return whether the elevator is at that position
     */
    public boolean isAtPosition(double position) {
        return Math.abs(getPosition() - position) < Constants.POSITION_TOLERANCE;
    }

    /**
     * @return whether the elevator is at its current setpoint
     */
    public boolean isAtSetpoint() {
        return isAtPosition(setpoint);
    }
}
