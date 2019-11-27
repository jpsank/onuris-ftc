package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

abstract class Component {
    OpMode opMode;

    abstract void update();
    abstract void go();
    abstract void addData();

}


class StackerComponent extends Component {

    double power = 0;

    DcMotor motor;

    StackerComponent(OpMode op, DcMotor motor) {
        opMode = op;
        this.motor = motor;

        this.motor.setDirection(DcMotor.Direction.FORWARD);
    }

    void update() {
        power = 0;
        if (opMode.gamepad1.right_bumper) {
            power = 1;
        }
        if (opMode.gamepad1.left_bumper) {
            power = -1;
        }
    }

    void go() {
        motor.setPower(power);
    }

    void addData() {
        opMode.telemetry.addData("Stacker Component",
                "power: (%.2f)",
                power);
    }
}


class Vector {
    double left;
    double right;
    Vector(double left, double right) {
        this.left = left;
        this.right = right;
    }
}


class StiltComponent extends Component {

    // Left, right
    Vector[] MODES = {
            new Vector(0, 0),
            new Vector(0, -600),
            new Vector(-13000, 2700),
            new Vector(-13000, 950),
            new Vector(-7000, 950),
            new Vector(-12000, 950)
    };


    int currentModeIdx = 0;
    boolean atMode = true;

    double leftPower = 0;
    double rightPower = 0;

    CRServo left_servo;
    DcMotor left_encoder;
    CRServo right_servo;
    DcMotor right_encoder;

    StiltComponent(OpMode op, CRServo left_servo, DcMotor left_encoder, CRServo right_servo, DcMotor right_encoder) {
        opMode = op;
        this.left_servo = left_servo;
        this.left_encoder = left_encoder;
        this.right_servo = right_servo;
        this.right_encoder = right_encoder;

        this.left_servo.setDirection(DcMotor.Direction.FORWARD);
        this.right_servo.setDirection(DcMotor.Direction.FORWARD);
    }

    private double getEncoderHeight(DcMotor encoder) {
        return encoder.getCurrentPosition();
    }
    private Vector currentHeightVector() {
        return new Vector(getEncoderHeight(this.left_encoder), getEncoderHeight(this.right_encoder));
    }

    private void adjustPower() {
        double toleranceLeft = 50;
        double slowdownFactorLeft = 500.;
        double toleranceRight = 100;
        double slowdownFactorRight = 300.;

        Vector current_vector = currentHeightVector();
        Vector target_vector = MODES[currentModeIdx];

        double current_left = current_vector.left;
        double target_left = target_vector.left;
        double current_right = current_vector.right;
        double target_right = target_vector.right;

        double left_diff = target_left-current_left;
        double right_diff = target_right-current_right;
        if (Math.abs(left_diff) < toleranceLeft) {
            leftPower = 0;
        } else {
            leftPower = Range.clip(left_diff / slowdownFactorLeft, -1, 1);
        }
        if (Math.abs(right_diff) < toleranceRight) {
            rightPower = 0;
        } else {
            rightPower = Range.clip(right_diff / slowdownFactorRight, -1, 1);
        }
        atMode = (rightPower == 0 && leftPower == 0);
    }

    void update() {
        adjustPower();
        if (atMode) {
            if (opMode.gamepad1.dpad_up && currentModeIdx < MODES.length - 1)
                currentModeIdx += 1;
            if (opMode.gamepad1.dpad_down && currentModeIdx > 0)
                currentModeIdx -= 1;
        }

    }

    void go() {
        left_servo.setPower(leftPower);
        right_servo.setPower(rightPower);
    }

    void addData() {
        Vector height_vector = currentHeightVector();
        opMode.telemetry.addData("Stilt Component",
                "left power: (%.2f), left height: (%.2f), right power: (%.2f), right height: (%.2f), \nMODE: %s, atMode: %s",
                leftPower, height_vector.left, rightPower, height_vector.right, currentModeIdx, atMode);
    }

}


class IntakeComponent extends Component {

    double intakePower = 0;
    double verticalPower = 0;

    DcMotor motor1;  // Left intake motor
    DcMotor motor2;  // Right intake motor
    DcMotor motor3;  // Vertical movement motor

    IntakeComponent(OpMode op, DcMotor motor1, DcMotor motor2, DcMotor motor3) {
        opMode = op;
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.motor3 = motor3;

        this.motor1.setDirection(DcMotor.Direction.FORWARD);
        this.motor2.setDirection(DcMotor.Direction.FORWARD);
        this.motor3.setDirection(CRServo.Direction.FORWARD);
    }

    void update() {
        intakePower = 0;
        if (opMode.gamepad1.x)
            intakePower += 1;
        if (opMode.gamepad1.b)
            intakePower -= 1;

        verticalPower = 0;
        if (opMode.gamepad1.y)
            verticalPower += 1;
        if (opMode.gamepad1.a)
            verticalPower -= 1;
    }

    void go() {
        motor1.setPower(intakePower);
        motor2.setPower(-intakePower);
        motor3.setPower(verticalPower);
    }

    void addData() {
        opMode.telemetry.addData("Intake Component",
                "intakePower: (%f), verticalPower: (%.2f)",
                intakePower, verticalPower);
    }

}


class SwerveDrive extends Component {

    private final double TWO_PI = 2*Math.PI;
    private final int FORWARD = 0;
    private final int REVERSE = 1;


    DcMotor motor1;  // Motor with encoder
    DcMotor motor2;
    CRServo servo;


    double drivePower;
    double servoPower;
    int motorDirection = FORWARD;

    SwerveDrive(OpMode op, DcMotor motor1, DcMotor motor2, CRServo servo) {
        opMode = op;
        this.motor1  = motor1;
        this.motor2 = motor2;
        this.servo = servo;

        this.servo.setDirection(CRServo.Direction.FORWARD);
        this.motor1.setDirection(DcMotor.Direction.FORWARD);
        this.motor2.setDirection(DcMotor.Direction.FORWARD);
    }

    private void setMotorDirection(int direction) {
        motorDirection = direction;
    }

    private void reverseMotorDirection() {
        if (motorDirection == FORWARD) {
            setMotorDirection(REVERSE);
        } else {
            setMotorDirection(FORWARD);
        }
    }

    /**
     * Constrains radians to an angle from 0 to TWO_PI
     */
    private double constrainRad(double r) {
        return r % TWO_PI;
    }

    /**
     * Calculate rotation of the swerve drive servo, in radians (0 - TWO_PI)
     */
    private double currentRotation() {
        double rot = constrainRad(TWO_PI * this.motor1.getCurrentPosition()/8192.);
        if (motorDirection == FORWARD) {
            return rot;
        } else {
            return constrainRad(rot+Math.PI);
        }
    }

    /**
     * Calculate target rotation based on direction of joystick (0 - TWO_PI)
     */
    private double targetRotation(double y, double x) {
        return constrainRad(Math.atan2(y, x));
    }

    /**
     * The amount of rotation, in radians (-PI to PI), needed to reach a target angle from the current angle
     */
    private double calculateTurn(double current, double target) {
        double diff_ccw = constrainRad(target - current);
        double diff_cw = TWO_PI - diff_ccw;
        double turn;
        if (diff_ccw < diff_cw) {
            turn = diff_ccw;
        } else {
            turn = -diff_cw;
        }
        return turn;
    }

    void update() {

//        if (opMode.gamepad1.right_stick_button) {
//            // add offset to make current rotation the forward position
//        }

        double x = opMode.gamepad1.right_stick_x;
        double y = opMode.gamepad1.right_stick_y;

        // MOTORS

        double magnitude = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
        drivePower = Range.clip(magnitude, -1.0, 1.0);  // Clip not necessary

        // SERVO

        if (magnitude == 0) {
            servoPower = 0;
        } else {
            double targetRotation = targetRotation(y, x);
            double currentRotation = currentRotation();
            double aboutFaceRotation = constrainRad(currentRotation + Math.PI);

            double turnCurrent = calculateTurn(currentRotation, targetRotation);
            double turnAboutFace = calculateTurn(aboutFaceRotation, targetRotation);

            if (Math.abs(turnAboutFace) < Math.abs(turnCurrent)) {
                reverseMotorDirection();
                servoPower = -Range.clip(turnAboutFace, -1, 1);
            } else {
                servoPower = -Range.clip(turnCurrent, -1, 1);
            }
        }

        if (motorDirection == REVERSE)
            drivePower *= -1;
    }

    void go() {
        this.motor1.setPower(drivePower);
        this.motor2.setPower(drivePower);
        this.servo.setPower(servoPower);
    }

    void addData() {
        String drc = "null";
        if (motorDirection == FORWARD) drc = "forward";
        else if (motorDirection == REVERSE) drc = "reverse";

        opMode.telemetry.addData("Swerve Drive",
                "currentRotation: (%f), direction: (%s), servoPower: (%.2f), drivePower: (%.2f)",
                currentRotation(), drc, servoPower, drivePower);
    }
}