package first.robot.simulation;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import org.wpilib.math.system.DCMotor;

public class ElevatorSim {

    private TalonFX leader;
    private TalonFX follower;

    private TalonFXSimState leaderSim;
    private TalonFXSimState followerSim;

    private org.wpilib.simulation.ElevatorSim sim;

    private final double GEAR_RATIO = (44.0 / 14.0) * (42.0 / 22.0); // Two stage, 14:44, 22:44 reductions
    private final double PULLEY_CIRCUMFERENCE = 24 * 0.005; // Convert 24t pulley to circumference, in m
    private final double PULLEY_RADIUS = PULLEY_CIRCUMFERENCE / (2 * Math.PI);
    private final double CARRIAGE_MASS = 11.24; // kg
    private final double MAX_HEIGHT = 1.347; // m

    public ElevatorSim(TalonFX leader, TalonFX follower) {
        this.leader = leader;
        this.follower = follower;

        leaderSim = leader.getSimState();
        followerSim = follower.getSimState();

        leaderSim.Orientation = ChassisReference.Clockwise_Positive;


        sim = new org.wpilib.simulation.ElevatorSim(DCMotor.getKrakenX60(2), GEAR_RATIO, CARRIAGE_MASS,
                PULLEY_RADIUS, 0, MAX_HEIGHT, true, 0);
    }

    public void periodic() {
        sim.setInputVoltage(leaderSim.getMotorVoltage());
        sim.update(0.02);

        double motorPosition = sim.getPosition() / PULLEY_CIRCUMFERENCE * GEAR_RATIO;
        leaderSim.setRawRotorPosition(motorPosition);
        followerSim.setRawRotorPosition(motorPosition);

        double motorVelocity = sim.getVelocity() / PULLEY_CIRCUMFERENCE * GEAR_RATIO;
        leaderSim.setRotorVelocity(motorVelocity);
        followerSim.setRotorVelocity(motorVelocity);
    }
}
