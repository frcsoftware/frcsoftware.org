package first.robot.mechanisms;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

public class Superstructure implements Mechanism {

    private final Elevator elevator;
    private final Arm arm;

    public Superstructure(Elevator elevator, Arm arm) {
        this.elevator = elevator;
        this.arm = arm;
    }

    public Command setPosition(double elevatorPosition, double armPosition) {
        return run((coro) -> {
            coro.awaitAll(elevator.setPosition(elevatorPosition),
                    arm.setPosition(armPosition));
        }).named("Set Position");
    }

    public Command setPosition(Positions position) {
        return setPosition(position.elevatorSetpoint, position.armSetpoint);
    }

    public enum Positions {
        STOW(0, 0.75),
        HANDOFF_PREP(0.5, 0.25),
        HANDOFF(0.38, 0.25),
        L4_PREP(1.2, 0.6),
        L4_SCORE(1.2, 0.525),
        L3_PREP(0.5, 0.6),
        L3_SCORE(0.5, 0.55),
        L2_PREP(0.1, 0.6),
        L2_SCORE(0.1, 0.55),
        ;

        private double elevatorSetpoint;
        private double armSetpoint;
        Positions(double elevatorSetpoint, double armSetpoint) {
            this.elevatorSetpoint = elevatorSetpoint;
            this.armSetpoint = armSetpoint;
        }
    }
}
