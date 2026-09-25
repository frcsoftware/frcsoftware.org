package first.robot.mechanisms;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import first.robot.Robot;
import first.robot.simulation.ClawSim;
import first.robot.simulation.CoralSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.hardware.discrete.DigitalInput;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public class Indexer implements Mechanism {
    private final TalonFX motor;
    private final DigitalInput sensor;

    private final TalonFXSimState simState;

    private final FlywheelSim sim;

    private final VoltageOut voltageRequest;

    private final StatusSignal<Voltage> appliedVoltageSignal;
    private final StatusSignal<Current> statorCurrentSignal;
    private final StatusSignal<Current> supplyCurrentSignal;

    private BaseStatusSignal[] signals;

    private static class Constants {
        static final int MOTOR_ID = 35;
        static final CANBus BUS = CANBus.systemcore(2);

        static final double GEAR_RATIO = (48.0 / 12.0); // One stage, 48:12 reduction

        static final double J = 0.00001;
    }

    public Indexer() {
        motor = new TalonFX(Constants.MOTOR_ID, Constants.BUS);
        sensor = new DigitalInput(0);

        simState = motor.getSimState();

        sim = new FlywheelSim(Models.flywheelFromPhysicalConstants(DCMotor.getKrakenX44(2), Constants.J, Constants.GEAR_RATIO), DCMotor.getKrakenX44(1));

        voltageRequest = new VoltageOut(0);

        appliedVoltageSignal = motor.getMotorVoltage();
        statorCurrentSignal = motor.getStatorCurrent();
        supplyCurrentSignal = motor.getSupplyCurrent();

        signals = new BaseStatusSignal[] {appliedVoltageSignal, statorCurrentSignal, supplyCurrentSignal};

        Scheduler.getDefault().addPeriodic(this::periodic);
    }

    public void periodic() {
        if (Robot.isSimulation()) {
            simPeriodic();
        }

        BaseStatusSignal.refreshAll(signals);

        Telemetry.log("Indexer/Applied Voltage", appliedVoltageSignal.getValueAsDouble());
        Telemetry.log("Indexer/Stator Current", statorCurrentSignal.getValueAsDouble());
        Telemetry.log("Indexer/Supply Current", supplyCurrentSignal.getValueAsDouble());
        Telemetry.log("Indexer/Active Commands", getRunningCommands().toString());

        Telemetry.log("Indexer/Has Coral", sensor.get());
    }

    public void simPeriodic() {
        sim.setInputVoltage(simState.getMotorVoltage());
        sim.update(0.02);

        simState.setRotorVelocity(Units.radiansToRotations(sim.getAngularVelocity()) * Constants.GEAR_RATIO);
    }

    /**
     * @param voltage the voltage to apply to the motors
     * @return a command
     */
    public Command setVoltage(double voltage) {
        return run((coro) -> {
            motor.setControl(voltageRequest.withOutput(voltage));
        }).named("Set Voltage: " + voltage + "V");
    }

    public boolean hasCoral() {
        return sensor.get();
    }
}
