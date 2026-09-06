package first.robot.simulation;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.DCMotorSim;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.simulation.SingleJointedArmSim;

public class ClawSim {

    private final TalonFX motor;

    private final TalonFXSimState motorSim;

    private final FlywheelSim sim;

    private final double GEAR_RATIO = (18.0 / 12.0) * (54.0 / 18.0) * (22.0 / 18.0) * (22.0 / 18.0);
    private final double J = 0.00001;

    public ClawSim(TalonFX motor) {
        this.motor = motor;

        motorSim = motor.getSimState();

        motorSim.Orientation = ChassisReference.Clockwise_Positive;

        sim = new FlywheelSim(Models.flywheelFromPhysicalConstants(DCMotor.getKrakenX44(1), J, GEAR_RATIO), DCMotor.getKrakenX44(1));
    }

    public void periodic() {
        sim.setInputVoltage(motorSim.getMotorVoltage());
        sim.update(0.02);

        motorSim.setRotorVelocity(Units.radiansToRotations(sim.getAngularVelocity()) * GEAR_RATIO);
    }
}
