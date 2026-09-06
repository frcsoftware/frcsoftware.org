package first.robot.mechanisms;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import first.robot.simulation.ClawSim;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.telemetry.Telemetry;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public class Claw implements Mechanism {
    private final TalonFX motor;

    private final ClawSim sim;

    private VoltageOut voltageRequest;

    private StatusSignal<Voltage> appliedVoltageSignal;
    private StatusSignal<Current> statorCurrentSignal;
    private StatusSignal<Current> supplyCurrentSignal;

    private BaseStatusSignal[] signals;

    public Claw() {
        motor = new TalonFX(23, CANBus.systemcore(2));

        sim = new ClawSim(motor);

        voltageRequest = new VoltageOut(0);

        appliedVoltageSignal = motor.getMotorVoltage();
        statorCurrentSignal = motor.getStatorCurrent();
        supplyCurrentSignal = motor.getSupplyCurrent();

        signals = new BaseStatusSignal[] {appliedVoltageSignal, statorCurrentSignal, supplyCurrentSignal};

        Scheduler.getDefault().addPeriodic(this::periodic);
    }

    public void periodic() {
        sim.periodic();

        BaseStatusSignal.refreshAll(signals);

        Telemetry.log("Claw/Applied Voltage", appliedVoltageSignal.getValueAsDouble());
        Telemetry.log("Claw/Stator Current", statorCurrentSignal.getValueAsDouble());
        Telemetry.log("Claw/Supply Current", supplyCurrentSignal.getValueAsDouble());
        Telemetry.log("Claw/Active Commands", getRunningCommands().toString());
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
}
