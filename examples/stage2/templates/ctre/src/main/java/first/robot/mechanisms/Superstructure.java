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
}
