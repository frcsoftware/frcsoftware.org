package first.robot.simulation;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.SingleJointedArmSim;

public class ArmSim {

    private final TalonFX motor;

    private final TalonFXSimState motorSim;

    private final SingleJointedArmSim sim;

    private final double GEAR_RATIO = (56.0 / 16.0) * (84.0 / 10.0); // Two stage, 56:16, 84:10 reductions
    private final double J = SingleJointedArmSim.estimateMOI(Units.inchesToMeters(15.2), 3.6);
    private final double ARM_LENGTH = Units.inchesToMeters(15.2);
    private final double MIN_ANGLE = 0;
    private final double MAX_ANGLE = 1.5 * Math.PI * 2;

    public ArmSim(TalonFX motor) {
        this.motor = motor;

        motorSim = motor.getSimState();

        motorSim.Orientation = ChassisReference.Clockwise_Positive;

        sim = new SingleJointedArmSim(DCMotor.getKrakenX60(1), GEAR_RATIO, J, ARM_LENGTH, MIN_ANGLE, MAX_ANGLE, true, -Math.PI/2);
    }

    public void periodic() {
        sim.setInputVoltage(motorSim.getMotorVoltage());
        sim.update(0.02);

        double motorPosition = sim.getAngle() / (Math.PI * 2) * GEAR_RATIO;
        motorSim.setRawRotorPosition(motorPosition);

        double motorVelocity = sim.getVelocity() / (Math.PI * 2) * GEAR_RATIO;
        motorSim.setRotorVelocity(motorVelocity);
    }
}
