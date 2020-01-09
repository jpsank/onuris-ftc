
package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.AbstractOpMode;


@TeleOp(name="Julian's Second Op Mode", group="Iterative Opmode")
public class TeleOpMode extends AbstractOpMode
{
    boolean useGyro = false;

    @Override
    public void loop() {
        super.loop();

        if (gamepad2.b)
            useGyro = true;
        if (gamepad2.x)
            useGyro = false;
        if (gamepad2.y)
            robot.gyro.resetHeading();

        telemetry.addData("Gyro", "Using gyro? %s", useGyro);

        boolean intakeUp = gamepad1.y;
        boolean intakeDown = gamepad1.a;
        boolean intakeIn = gamepad1.dpad_down;
        boolean intakeOut = gamepad1.dpad_up;

        double driveStick_X = gamepad2.right_stick_x;
        double driveStick_Y = gamepad2.right_stick_y;
        double rotateStick_X = gamepad2.left_stick_x;

        telemetry.addData("Controller Inputs",
                "driveStick_X: (%f), driveStick_Y: (%f), rotateStick_X: (%f)",
                driveStick_X, driveStick_Y, rotateStick_X);

        // Intake Component

        if (robot.intake != null) {

            robot.intake.setIntakeOff();
            if (intakeIn)
                robot.intake.setIntakeIn();
            else if (intakeOut)
                robot.intake.setIntakeOut();

            if (intakeUp)
                robot.intake.liftUp();
            if (intakeDown)
                robot.intake.liftDown();
            robot.intake.setLiftPower();

            robot.intake.addData(telemetry);
        }

        // Swerve Drives

        if (robot.swerveDrive != null) {
            if (useGyro)
                robot.swerveDrive.drive(driveStick_X, driveStick_Y, rotateStick_X, robot.gyro.getAdjustedHeading());
            else
                robot.swerveDrive.drive(driveStick_X, driveStick_Y, rotateStick_X);
            robot.swerveDrive.addData(telemetry);
        }

        // Update telemetry

        telemetry.update();

    }

}
