/*
 * Copyright (c) 2020 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

 /*
  * PID controller and IMU codes are copied from
  * https://stemrobotics.cs.pdx.edu/node/7268%3Froot=4196.html
  */

package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="TeleopDualDrivers", group="Concept")
//@Disabled
public class TeleopDualDrivers extends LinearOpMode {

    // Declare OpMode members.
    static final double MAX_WAIT_TIME = 20; // in seconds
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor FrontLeftDrive = null;
    private DcMotor FrontRightDrive = null;
    private DcMotor BackLeftDrive = null;
    private DcMotor BackRightDrive = null;
    private BNO055IMU imu = null;


    // Driving motor variables
    static final double HIGH_SPEED_POWER = 0.6;  // used to adjust driving sensitivity.
    static final double SLOW_DOWN_POWER = 0.2;
    static final double CORRECTION_POWER = 0.1;
    static final double MIN_ROTATE_POWER = 0.11;
    static final double AUTO_DRIVE_POWER = 0.5; // used for auto driving
    static final double AUTO_ROTATE_POWER = 0.5; // used for auto driving

    // slider motor variables
    private DcMotor RightSliderMotor = null;
    private DcMotor LeftSliderMotor = null;
    static final double SLIDER_MOTOR_POWER = 0.8; // slider string gets loose with too high speed
    static final int COUNTS_PER_INCH = 120; // verified by testing.
    static final int FOUR_STAGE_SLIDER_MAX_POS = 4200;  // with 312 RPM motor.
    static final int SLIDER_MIN_POS = 0;
    static final int GROUND_JUNCTION_POS = COUNTS_PER_INCH; // 1 inch
    static final int coneStack5th = (int)(COUNTS_PER_INCH * 5.2); // the 5th cone position in the cone stack. The lowest cone is the 1th one.

    // 10inch for low junction, 20inch for medium, and 30 for high
    static final int WALL_POSITION = (int)(COUNTS_PER_INCH * 7);
    static final int LOW_JUNCTION_POS = (int)(COUNTS_PER_INCH * 13.5); // 13.5 inch
    static final int MEDIUM_JUNCTION_POS = (int)(COUNTS_PER_INCH * 23.5);
    static final int HIGH_JUNCTION_POS = (int)(COUNTS_PER_INCH * 33.5);
    static final int SLIDER_MOVE_DOWN_POSITION = COUNTS_PER_INCH * 3; // move down 6 inch to unload cone
    static final int POSITION_COUNTS_FOR_ONE_REVOLUTION = 538; // for 312 rpm motor
    int motorPositionInc = POSITION_COUNTS_FOR_ONE_REVOLUTION / 3; // set value based on testing
    int sliderTargetPosition = 0;


    // claw servo motor variables
    private Servo clawServo = null;
    static final double CLAW_INCREMENT = -0.24;  // amount to slew servo each CYCLE_MS cycle
    static final double CLAW_OPEN_POS = 0.31;     // Maximum rotational position
    static final double CLAW_CLOSE_POS = 0.08;
    static final double CLAW_MAX_POS = CLAW_OPEN_POS;
    static final double CLAW_MIN_POS = CLAW_CLOSE_POS;  // Minimum rotational position
    double clawServoPosition = CLAW_OPEN_POS;


    // arm servo variables, not used in current prototype version.
    private Servo armServo = null;
    static final double ARM_INCREMENT = 0.0015;     // amount to slew servo each CYCLE_MS cycle
    static final double ARM_MAX_POS = 0.6;     // Maximum rotational position
    static final double ARM_MIN_POS = 0.1;     // Minimum rotational position
    static final double ARM_LOAD_POSITION = 0.3;
    static final double ARM_UNLOAD_POSITION = 0.3;
    double armServoPosition = ARM_LOAD_POSITION;


    // variables for auto load and unload cone
    static final int COUNTS_PER_INCH_DRIVE = 45; // robot drive 1 INCH. Back-forth moving
    static final int COUNTS_PER_INCH_STRAFE = 55; // robot strafe 1 INCH. Left-right moving. need test
    double robotAutoLoadMovingDistance = 1.0; // in INCH
    double robotAutoUnloadMovingDistance = 3.5; // in INCH


    // IMU related
    Orientation lastAngles = new Orientation();
    double globalAngle = 0.0;
    double correction = 0.0;
    double rotation = 0.0;
    PIDController pidRotate, pidDrive;
    boolean resetAngleFlag = false;
    static final int INERTIA_WAIT_TIME = 500; // in ms

    // sensors
    private DistanceSensor distanceSensor;
    static final double CLOSE_DISTANCE = 8.0; // Inch
    private ColorSensor colorSensor;// best collected within 2cm of the target
    double redRatioBg, greenRatioBg, blueRatioBg;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        FrontLeftDrive  = hardwareMap.get(DcMotor.class, "FrontLeft");
        FrontRightDrive = hardwareMap.get(DcMotor.class, "FrontRight");
        BackLeftDrive = hardwareMap.get(DcMotor.class,"BackLeft");
        BackRightDrive = hardwareMap.get(DcMotor.class,"BackRight");
        RightSliderMotor = hardwareMap.get(DcMotor.class,"RightSlider");
        LeftSliderMotor = hardwareMap.get(DcMotor.class,"LeftSlider");
        armServo = hardwareMap.get(Servo.class, "ArmServo");
        clawServo = hardwareMap.get(Servo.class, "ClawServo");
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        distanceSensor = hardwareMap.get(DistanceSensor.class, "DistanceSensor");
        colorSensor = hardwareMap.get(ColorSensor.class, "ColorSensor");

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        FrontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        FrontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        BackLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        BackRightDrive.setDirection(DcMotor.Direction.FORWARD);

        FrontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FrontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BackLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BackRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //reset encode number to zero
        FrontLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FrontRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BackLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BackRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robotRunWithPositionModeOn(false); // turn off encoder mode as default

        /* slider motor control */
        RightSliderMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        LeftSliderMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        setSliderPosition(sliderTargetPosition);
        // Reset slider motor encoder counts kept by the motor
        RightSliderMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        LeftSliderMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // Set motor to run to target encoder position and top with brakes on.
        RightSliderMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        LeftSliderMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);


        // claw servo motor initial
        clawServoPosition = CLAW_OPEN_POS;
        clawServo.setPosition(clawServoPosition);

        // sensors
        boolean distanceSensorEnabled = true;
        int redBackground = 1, greenBackground = 1, blueBackground = 1;

        // IMU
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.mode                = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled      = false;
        imu.initialize(parameters);
        double timeMS = 0.0;

        // Set PID proportional value to start reducing power at about 50 degrees of rotation.
        // P by itself may stall before turn completed so we add a bit of I (integral) which
        // causes the PID controller to gently increase power if the turn is not completed.
        pidRotate = new PIDController(.015, .0, 0);

        // Set PID proportional value to produce non-zero correction value when robot veers off
        // straight line. P value controls how sensitive the correction is.
        pidDrive = new PIDController(.03, 0, 0);

        // make sure the imu gyro is calibrated before continuing.
        while (!isStopRequested() && !imu.isGyroCalibrated()) {
            sleep(500);
            idle();
        }
        telemetry.addData("imu calib status", imu.getCalibrationStatus().toString());

        // Set up parameters for driving in a straight line.
        pidDrive.setInputRange(-90, 90);
        pidDrive.setSetpoint(0); // be sure input range has been set before
        pidDrive.setOutputRange(0, CORRECTION_POWER);
        pidDrive.enable();

        //game pad setting
        float robotMovingBackForth;
        float robotMovingRightLeft;
        float robotTurn;
        float sliderUpDown;
        boolean sliderWallPosition;
        boolean sliderLowJunctionPosition;
        boolean sliderMediumJunctionPosition;
        boolean sliderHighJunctionPosition;
        boolean clawClose;
        boolean clawOpen;
        boolean armTurnLeft;
        boolean armTurnRight;
        boolean autoLoadConeOn;
        boolean autoUnloadConeOn;
        boolean distanceSensorOn;

        boolean dualDriverMode = true;
        Gamepad myGamePad;

        // autonomous testing
        double parkingLocation = 0.0; // distance between cone loading area to parking area, in inch

        // Wait for the game to start (driver presses PLAY)
        telemetry.addData("Mode", "waiting for start");
        telemetry.update();
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            // Game pad buttons design
            if (gamepad1.start && (gamepad1.left_trigger > 0)) {
                dualDriverMode = false;
            }

            if (gamepad1.start && (gamepad1.right_trigger > 0)) {
                dualDriverMode = true;
            }

            if (dualDriverMode) {
                myGamePad = gamepad2;
            }
            else {
                myGamePad = gamepad1;
            }

            //gamepad1 buttons
            robotMovingBackForth = gamepad1.left_stick_y;
            robotMovingRightLeft = gamepad1.left_stick_x;
            robotTurn            = gamepad1.right_stick_x;
            distanceSensorOn     = gamepad1.back;
            autoLoadConeOn       = gamepad1.left_bumper;

            // gamepad1 or gamepad2 buttons
            sliderUpDown                = myGamePad.right_stick_y;
            sliderWallPosition          = myGamePad.x;
            sliderLowJunctionPosition   = myGamePad.a;
            sliderMediumJunctionPosition= myGamePad.b;
            sliderHighJunctionPosition  = myGamePad.y;
            clawClose                   = myGamePad.dpad_up;
            clawOpen                    = myGamePad.dpad_down;
            armTurnLeft                 = myGamePad.dpad_left;
            armTurnRight                = myGamePad.dpad_right;
            autoUnloadConeOn            = myGamePad.right_bumper;

            // sensors
            if (distanceSensorOn) {
                distanceSensorEnabled = !distanceSensorEnabled;
            }
            telemetry.addData("distance sensor: ", distanceSensorEnabled? "On" : "Off");
            telemetry.addData("Dual driver mode: ", dualDriverMode? "On" : "Off");


            // Setup a variable for each drive wheel to save power level for telemetry
            double FrontLeftPower;
            double FrontRightPower;
            double BackLeftPower;
            double BackRightPower;
            double maxDrivePower = HIGH_SPEED_POWER;

            //distance sensor control
            if ((distanceSensor.getDistance(DistanceUnit.INCH) < CLOSE_DISTANCE) &&
                    distanceSensorEnabled) {
                maxDrivePower = SLOW_DOWN_POWER;
            }

            //color sensor control
            /*
            if (distanceSensor.getDistance(DistanceUnit.INCH) > CLOSE_DISTANCE) {
                redBackground = colorSensor.red();
                greenBackground = colorSensor.green();
                blueBackground = colorSensor.blue();
            }
            redRatioBg = (double)redBackground / (redBackground + greenBackground + blueBackground);
            greenRatioBg = (double)greenBackground / (redBackground + greenBackground + blueBackground);
            blueRatioBg = (double)blueBackground / (redBackground + greenBackground + blueBackground);

            double tmp = (double)colorSensor.red() + colorSensor.green() + colorSensor.blue();
            double redRatioSleeve = colorSensor.red() / tmp;
            double greenRatioSleeve = colorSensor.green() / tmp;
            double blueRatioSleeve = colorSensor.blue() / tmp;

            // send the info back to driver station using telemetry function.
            telemetry.addLine()
                    .addData("Clear", colorSensor.alpha())
                    .addData("Red  ", colorSensor.red())
                    .addData("Green", colorSensor.green())
                    .addData("Blue ", colorSensor.blue());

            telemetry.addData("red ratios bg = ", "%.2f", redRatioBg);
            telemetry.addData("red ratios sleeve = ", "%.2f", redRatioSleeve);
            telemetry.addData("green ratios bg = ", "%.2f", greenRatioBg);
            telemetry.addData("green ratios sleeve = ", "%.2f", greenRatioSleeve);
            telemetry.addData("blue ratios bg = ", "%.2f", blueRatioBg);
            telemetry.addData("blue ratios sleeve =", "%.2f", blueRatioSleeve);
            telemetry.addData("red ", (redRatioSleeve>redRatioBg)? "yes" : "no");
            telemetry.addData("green ", (greenRatioSleeve>greenRatioBg)? "yes" : "no");
            telemetry.addData("blue ", (blueRatioSleeve>blueRatioBg)? "yes" : "no");
            */
            double drive = maxDrivePower * robotMovingBackForth;
            double turn  =  maxDrivePower * (-robotTurn);
            double strafe = maxDrivePower * (-robotMovingRightLeft);

            // only enable correction when the turn button is not pressed.
            if (Math.abs(turn) > Math.ulp(0)) {
                pidDrive.reset();
                timeMS = runtime.milliseconds();
                resetAngleFlag = true;
                resetAngle(); // Resets the cumulative angle tracking to zero.
            }

            // turn on PID after a duration time to avoid robot inertia after turning.
            if (((runtime.milliseconds() - timeMS) > INERTIA_WAIT_TIME) && resetAngleFlag) {
                resetAngleFlag = false;
                resetAngle(); // Resets the cumulative angle tracking to zero.
                pidDrive.enable();
            }

            // Use PID with imu input to drive in a straight line.
            if ((Math.abs(drive) > Math.ulp(0)) || (Math.abs(strafe) > Math.ulp(0))) {
                correction = pidDrive.performPID(getAngle());
            }
            else {
                correction = 0.0;
            }
            telemetry.addData("imu heading ","%.2f", lastAngles.firstAngle);
            telemetry.addData("global heading ", "%.2f", globalAngle);
            telemetry.addData("Correction  ", "%.2f", correction);
            telemetry.addData("Turn rotation ", "%.2f", rotation);
            telemetry.addData("Max driving poiwer ", "%.2f", maxDrivePower);
            telemetry.addData("Distance sensor = ", "%.2f", distanceSensor.getDistance(DistanceUnit.INCH));
            telemetry.addData("Sleeve signal", "%.2f", parkingLocation);

            FrontLeftPower  = Range.clip(-drive - turn - strafe - correction, -1, 1);
            FrontRightPower = Range.clip(-drive + turn + strafe + correction, -1, 1);
            BackLeftPower   = Range.clip(-drive - turn + strafe - correction, -1, 1);
            BackRightPower  = Range.clip(-drive + turn - strafe + correction, -1, 1);


            // Send calculated power to wheels
            FrontLeftDrive.setPower(FrontLeftPower);
            FrontRightDrive.setPower(FrontRightPower);
            BackLeftDrive.setPower(BackLeftPower);
            BackRightDrive.setPower(BackRightPower);

            // use Y button to lift up the slider reaching high junction
            if (sliderHighJunctionPosition) {
                sliderTargetPosition = HIGH_JUNCTION_POS;
            }

            // use B button to lift up the slider reaching medium junction
            if (sliderMediumJunctionPosition) {
                sliderTargetPosition = MEDIUM_JUNCTION_POS;
            }

            // use A button to lift up the slider reaching low junction
            if (sliderLowJunctionPosition) {
                sliderTargetPosition = LOW_JUNCTION_POS;
            }

            // use X button to move the slider for ground junction position
            if (sliderWallPosition) {
                sliderTargetPosition = WALL_POSITION;
            }

            // use right stick_Y to lift or down slider continuously
            sliderTargetPosition -= (int)((sliderUpDown) * motorPositionInc);
            sliderTargetPosition = Range.clip(sliderTargetPosition, SLIDER_MIN_POS,
                    FOUR_STAGE_SLIDER_MAX_POS);
            telemetry.addData("Status", "slider motor Target position %d",
                    sliderTargetPosition);

            setSliderPosition(sliderTargetPosition);
            RightSliderMotor.setPower(SLIDER_MOTOR_POWER); // slider motor start movement
            LeftSliderMotor.setPower(SLIDER_MOTOR_POWER);
            telemetry.addData("Status", "Right slider motor current position %d",
                    RightSliderMotor.getCurrentPosition());
            telemetry.addData("Status", "Left slider motor current position %d",
                    LeftSliderMotor.getCurrentPosition());

            // Keep stepping up until we hit the max value.
            if (clawClose) {
                clawServoPosition += CLAW_INCREMENT;
            }
            if (clawOpen) {
                clawServoPosition -= CLAW_INCREMENT;
            }
            clawServoPosition = Range.clip(clawServoPosition, CLAW_MIN_POS, CLAW_MAX_POS);
            clawServo.setPosition(clawServoPosition);
            telemetry.addData("Status", "Claw Servo position %.2f", clawServoPosition);

            // arm servo motor control. Keep stepping up until we hit the max value.
            if (armTurnLeft) {
                armServoPosition += ARM_INCREMENT;
            }
            if (armTurnRight) {
                armServoPosition -= ARM_INCREMENT;
            }
            armServoPosition = Range.clip(armServoPosition, ARM_MIN_POS, ARM_MAX_POS);
            armServo.setPosition(armServoPosition);
            telemetry.addData("Status", "Arm Servo position %.2f", armServoPosition);

            //  auto driving, grip cone, and lift slider
            if(autoLoadConeOn) {
                autoLoadCone(SLIDER_MIN_POS); // Always on ground during teleop mode
                // set arm, claw, slider position after grep.
                armServoPosition = armServo.getPosition();
                // left motor has same position with right one
                sliderTargetPosition = LOW_JUNCTION_POS;
            }

            //  auto driving, unload cone
            if(autoUnloadConeOn) {
                autoUnloadCone();
                // set arm, claw, slider position after grep.
                armServoPosition = armServo.getPosition();
                clawServoPosition = clawServo.getPosition();
                sliderTargetPosition = getSliderPosition();
            }

            // Show the elapsed game time and wheel power, positions.

            telemetry.addData("Motors power", "Frontleft (%.2f), Frontright (%.2f)," +
                            " Backleft (%.2f), Backright (%.2f)", FrontLeftPower, FrontRightPower,
                    BackLeftPower,BackRightPower);

            telemetry.addData("Motors Positions:",
                    "Frontleft (%d), Frontright (%d)," + " Backleft (%d), Backright (%d)",
                    FrontLeftDrive.getCurrentPosition(), FrontRightDrive.getCurrentPosition(),
                    BackLeftDrive.getCurrentPosition(), BackRightDrive.getCurrentPosition());

            // use gamepad2 to test autonomous code
            if (!dualDriverMode) {
                autonomousCore();
            }
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.update(); // update message at the end of while loop
        }

        // The motor stop on their own but power is still applied. Turn off motor.
        RightSliderMotor.setPower(0.0);
        LeftSliderMotor.setPower(0.0);
        setPowerToWheels(0.0);
        FrontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        FrontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        BackLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        BackRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        RightSliderMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        LeftSliderMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    /**
     * 1. Robot moving back to aim at junction for unloading cone
     * 2. Slider moving down a little bit to put cone in junction pole
     * 3. Open claw to fall down cone
     * 4. Lift slider from junction pole
     * 5. Robot moving back to leave junction
     * 6. Slider moving down to get ready to grip another cone
     */
    private void autoUnloadCone() {
        armServo.setPosition(ARM_UNLOAD_POSITION);
        robotRunToPosition(-robotAutoUnloadMovingDistance, true); // moving back in inch

        // move down slider a little bit to unload cone
        sliderTargetPosition = getSliderPosition();
        int moveSlider = sliderTargetPosition - SLIDER_MOVE_DOWN_POSITION;
        moveSlider = Math.max(moveSlider, SLIDER_MIN_POS);
        setSliderPosition(moveSlider);
        waitSliderRun();

        clawServo.setPosition(CLAW_OPEN_POS); // unload  cone
        sleep(100); // wait 0.4 sec to make sure clawServo is at grep position
        clawServoPosition = clawServo.getPosition(); // keep claw position

        robotRunToPosition(-robotAutoUnloadMovingDistance, true); // move out from junction
        armServo.setPosition(ARM_LOAD_POSITION);
        waitRobotRun();
        sliderTargetPosition = WALL_POSITION;
        //sleep(100);
    }

    /**
     * During autonomous, cone may be located with different height position
     * 1. Lift slider and open claw to get read to load a cone
     * 2. Robot moving back to aim at cone for loading
     * 2. Slider moving down to load the cone
     * 3. Close claw to grip the cone
     * 4. Lift slider to low junction position for unloading
     * 5. Robot moving back to leave junction
     * 6. Slider moving down to get ready to grip another cone
     * @param coneLocation: the target cone high location.
     */
    private void autoLoadCone(int coneLocation) {
        clawServo.setPosition(CLAW_OPEN_POS);
        armServo.setPosition(ARM_LOAD_POSITION);
        robotRunToPosition(-robotAutoLoadMovingDistance, true); // moving to loading position
        setSliderPosition(coneLocation);
        waitSliderRun();
        clawServo.setPosition(CLAW_CLOSE_POS);
        sleep(400); // wait 0.4 sec to make sure clawServo is at grep position
        clawServoPosition = clawServo.getPosition(); // keep claw position
        armServo.setPosition(ARM_UNLOAD_POSITION);
    }

    /**
     * Set target position for every wheel motor, and set power to motors to move the robot.
     * Turn off encode mode after moving.
     * @param targetDistance: Input value for the target distance in inch.
     * @param isBackForth: flag for back-forth (true) moving, or left-right moving (false)
     */
    private void robotRunToPosition(double targetDistance, boolean isBackForth) {
        int countsPerInch = isBackForth? COUNTS_PER_INCH_DRIVE : COUNTS_PER_INCH_STRAFE;
        int targetPosition = (int)(targetDistance * countsPerInch);
        telemetry.addData("Moving", "auto driving target position %d", targetPosition);
        setTargetPositionsToWheels(targetPosition, isBackForth);
        robotRunWithPositionModeOn(true); // turn on encoder mode
        int tSign = (int)Math.copySign(1, targetDistance);
        robotDriveWithPIDControl(AUTO_DRIVE_POWER, tSign, isBackForth);
        robotRunWithPositionModeOn(false); // turn off encoder mode
    }

    /**
     * Set wheels motors to stop and reset encode to set the current encoder position to zero.
     * And then set to run to position mode if withPositionMode is on.
     * Otherwise, set to run without encode mode.
     * @param withPositionMode: flag for wheels motors run with position mode on,
     *                       or off(run without encode)
     */
    private void robotRunWithPositionModeOn(boolean withPositionMode) {
        if (withPositionMode) {
            FrontLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            FrontLeftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            FrontRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            FrontRightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            BackLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            BackLeftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            BackRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            BackRightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }
        else {
            // set back to WITHOUT ENCODER mode
            FrontLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            FrontRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            BackLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            BackRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    /**
     * Set wheels motors target positions according to back-forward moving flag
     * @param tPos: target position values for motors
     * @param isBF: flag for back-forward moving or left-right moving.
     *            Back forward(1), or left right (0)
     */
    private void setTargetPositionsToWheels(int tPos, boolean isBF) {
        if (isBF) {
            FrontLeftDrive.setTargetPosition( tPos );
            FrontRightDrive.setTargetPosition( tPos );
            BackLeftDrive.setTargetPosition( tPos );
            BackRightDrive.setTargetPosition( tPos );
        }
        else {// move left or right, positive for right
            FrontLeftDrive.setTargetPosition( tPos );
            FrontRightDrive.setTargetPosition( -tPos );
            BackLeftDrive.setTargetPosition( -tPos );
            BackRightDrive.setTargetPosition( tPos );
        }
    }

    /**
     * Set wheels motors power
     * @param p: the power value set to motors (0.0 ~ 1.0)
     */
    private void setPowerToWheels(double p) {
        FrontLeftDrive.setPower(p);
        FrontRightDrive.setPower(p);
        BackLeftDrive.setPower(p);
        BackRightDrive.setPower(p);
    }

    /**
     * Wait until the motor complete action.
     * The MAXIMUM waiting time is MAX_WAIT_TIME to avoid death.
     * @param mot: the motor which be checked if it is in active.
     */
    private void waitMotorActionComplete(@NonNull DcMotor mot) {
        double curTime = runtime.seconds();
        while((mot.isBusy()) && ((runtime.seconds() - curTime) < MAX_WAIT_TIME)) {
            idle();
        }
    }


    /**
     * Resets the cumulative angle tracking to zero.
     */
    private void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        globalAngle = 0;
    }

    /**
     * Get current cumulative angle rotation from last reset.
     * @return Angle in degrees. + = left, - = right from zero point.
     */
    private double getAngle() {
        // We experimentally determined the Z axis is the axis we want to use for heading angle.
        // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    /**
     * Rotate left or right the number of degrees. Does not support turning more than 359 degrees.
     * @param degrees Degrees to turn, + is left - is right
     */
    private void rotate(double degrees, double power) {
        resetAngle();

        // if degrees > 359 we cap at 359 with same sign as original degrees.
        if (Math.abs(degrees) > 359.99)
            degrees = Math.copySign(359.99, degrees);

        // start pid controller. PID controller will monitor the turn angle with respect to the
        // target angle and reduce power as we approach the target angle. This is to prevent the
        // robots momentum from overshooting the turn after we turn off the power. The PID controller
        // reports onTarget() = true when the difference between turn angle and target angle is within
        // 1% of target (tolerance) which is about 1 degree. This helps prevent overshoot. Overshoot is
        // dependant on the motor and gearing configuration, starting power, weight of the robot and the
        // on target tolerance. If the controller overshoots, it will reverse the sign of the output
        // turning the robot back toward the setpoint value.

        pidRotate.reset();
        pidRotate.setInputRange(0, degrees);
        pidRotate.setSetpoint(degrees); // be sure input range has been set before
        pidRotate.setOutputRange(MIN_ROTATE_POWER, power);
        pidRotate.setTolerance(1);
        pidRotate.enable();

        // getAngle() returns + when rotating counter clockwise (left) and - when rotating
        // clockwise (right).

        // rotate until turn is completed.

        if (degrees < 0) {
            // On right turn we have to get off zero first.
            while (opModeIsActive() && getAngle() == 0) {
                leftMotorSetPower(power);
                rightMotorSetPower(-power);
                sleep(100);
            }

            do {
                power = pidRotate.performPID(getAngle()); // power will be - on right turn.
                leftMotorSetPower(-power);
                rightMotorSetPower(power);
                telemetry.addData("Rotate", "rotate power: %.4f", power);
                telemetry.update();
            } while (opModeIsActive() && !pidRotate.onAbsTarget());
        }
        else    // left turn.
            do {
                power = pidRotate.performPID(getAngle()); // power will be + on left turn.
                leftMotorSetPower(-power);
                rightMotorSetPower(power);
                telemetry.addData("Rotate", "rotate power: %.4f", power);
                telemetry.update();
            } while (opModeIsActive() && !pidRotate.onAbsTarget());

        // turn the motors off.
        rightMotorSetPower(0);
        leftMotorSetPower(0);

        rotation = getAngle();

        // wait for rotation to stop.
        sleep(500);

        // reset angle tracking on new heading.
        resetAngle();
    }

    /**
     * Set left side motors power.
     * @param p the power set to front left motor and back left motor
     */
    private void leftMotorSetPower(double p) {
        FrontLeftDrive.setPower(p);
        BackLeftDrive.setPower(p);
    }

    /**
     * Set right side motors power.
     * @param p the power set to front left right motor and back right motor
     */
    private void rightMotorSetPower(double p) {
        FrontRightDrive.setPower(p);
        BackRightDrive.setPower(p);
    }

    /**
     * Set motors power and drive or strafe robot straightly with run_to_position mode by PID control.
     * @param p the power set to the robot motors
     * @param targetSign: Input value for the target distance sign to indicate drive directions. Disable PID if it is zero.
     * @param isBF: flag for back-forth (true) moving, or left-right moving (false)
     */
    private void robotDriveWithPIDControl(double p, int targetSign, boolean isBF ) {
        double curTime = runtime.seconds();
        correction = 0.0;
        setPowerToWheels(p); // p is always positive for RUN_TO POSITION mode.
        while(robotIsBusy() && ((runtime.seconds() - curTime) < MAX_WAIT_TIME)) {
            if (0 == targetSign) { // no pid if sign = 0;
                correction = pidDrive.performPID(getAngle());
            }

            if (isBF) { // left motors have same power
                leftMotorSetPower(p - correction * targetSign);
                rightMotorSetPower(p + correction * targetSign);
            }
            else { // front motors have same power
                FrontLeftDrive.setPower(p - correction * targetSign);
                FrontRightDrive.setPower(p - correction * targetSign);
                BackLeftDrive.setPower(p + correction * targetSign);
                BackRightDrive.setPower(p + correction * targetSign);
            }
        }
        setPowerToWheels(0.0); //stop moving
    }

    /**
     * Check if robot motors are busy. Return ture if yes, false otherwise.
     */
    private boolean robotIsBusy() {
        return (FrontRightDrive.isBusy() || FrontLeftDrive.isBusy() ||
                BackLeftDrive.isBusy() || BackRightDrive.isBusy());
    }

    /** code for autonomous
     * 1. take a picture, recognize the color on sleeve signal
     * 2. Move robot the high junction
     * 3. Unload cone on high junction
     * 4. Move robot to cone loading area
     * 5. Load cone
     * 6. Move robot to parking area
     */
    private void autonomousCore() {
        double backgroundColor[] = {1.0, 1.0, 1.0};
        double sleeveColor[] = {1.0, 1.0, 1.0};
        double parkingLocation = 0.0; // distance between cone loading area to parking area, in inch

        telemetry.update();

        /* code for autonomous driving, must starting from right position.    */
        if (gamepad2.a) {
            //preload cone
            clawServo.setPosition(CLAW_CLOSE_POS);
            sleep(200); // wait 0.4 sec to make sure clawServo is at grep position
            setSliderPosition(LOW_JUNCTION_POS);
            waitSliderRun();
            sliderTargetPosition = getSliderPosition(); // set target position to current position
            clawServoPosition = clawServo.getPosition(); // keep claw position
            readColorSensor(backgroundColor);
            // drive robot to sleeve cone
            robotRunToPosition(20.0, true);
            readColorSensor(sleeveColor); // reading sleeve signal
            robotRunToPosition(36.0, true); // drive robot to the center of 3rd mat
            robotRunToPosition(-4.0, true); // throw off sleeve cone
            parkingLocation = calculateParkingLocation(sleeveColor, backgroundColor);

            telemetry.addData("Sleeve","moving distance (%.1f) inch", parkingLocation);
            telemetry.update();
        }

        // move up slider
        if (gamepad2.b) {
            robotRunToPosition(-12.0, false); // strafe robot half mat to left side
            setSliderPosition(HIGH_JUNCTION_POS);
            waitSliderRun();
            sliderTargetPosition = getSliderPosition(); // set target position to current position
        }

        if (gamepad2.y) {
            robotRunToPosition(8.0, true); // drive robot half mat to high junction
        }
        if (gamepad2.right_bumper) {
            autoUnloadCone();
            setSliderPosition(WALL_POSITION);
            waitSliderRun(); // make sure left and right motor are complete actions
            sliderTargetPosition = getSliderPosition(); // set target position to current position
        }

        if (gamepad2.dpad_right) {
            Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            rotate(-angles.firstAngle - 90, AUTO_ROTATE_POWER); // turn robot 90 degree to right
        }

        if (gamepad2.dpad_down) {
            robotRunToPosition(38.5, true); // drive robot to loading area
        }
        if (gamepad2.left_bumper) {
            autoLoadCone(coneStack5th); // need update to input cone height position
            setSliderPosition(LOW_JUNCTION_POS);
            waitSliderRun();
            sliderTargetPosition = getSliderPosition(); // set target position to current position
        }

        if (gamepad2.dpad_left) {
            Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            rotate(-angles.firstAngle + 90, AUTO_ROTATE_POWER); // turn robot 180 degree to right
        }

        if (gamepad2.dpad_up) {
            robotRunToPosition(parkingLocation, true); // drive robot to parking
            robotRunToPosition(24.0, false); // strafe robot to parking mat
        }

        if (gamepad2.x) {
            robotRunToPosition(48, false); // strafe testing 48 inch
        }

    }

    /**
     * Calculate the destination parking area according to sleeve color.
     * @param s three ratios values of sleeve signal color reading from color sensor
     * @param b three ratios values of background color reading from color sensor
     * return the distance between cone stack and parking area, in inch.
     */
    private double calculateParkingLocation(@NonNull double[] s, @NonNull double[] b) {
        int channel = 0;
        double location;
        double ratio[] = {s[0]/b[0], s[1]/b[1], s[2]/b[2]};

        // find the maximum value in ratio[]
        double maxV = ratio[0];
        for (int i = 1; i < 3; i++) {
            if (ratio[i] > maxV) {
                channel = i;
                maxV = ratio[i];
            }
        }

        String color = "";
        switch (channel) {
            case 0: // red
                location = 5.0 * 12; // parking lot #1 (red), third mat
                color = "red";
                break;
            case 1: // green
                location = 3.0 * 12; // parking lot #2 (green), third mat
                color = "green";
                break;
            case 2: // blue
                location = 1.0 * 12; // parking lot #3 (blue), third mat
                color = "blue";
                break;
            default:
                location = 0.0;
        }
        telemetry.addLine()
                .addData("channal ", channel)
                .addData("Color is ", color)
                .addData("location ", location);
        telemetry.addData("color", "rgb ratio, %.2f, %.2f, %.2f", ratio[0], ratio[1], ratio[2]);
        return location;
    }

    /**
     * Read color from color sensor and translate three values to relative ratio.
     * @param colorRatio three ratios values of color reading from color sensor
     */
    private void readColorSensor(@NonNull double[] colorRatio ) {
        //color sensor control
        int r = 1, g = 1, b = 1, total = 3;

        r = colorSensor.red();
        g = colorSensor.green();
        b = colorSensor.blue();

        total = r + g + b;
        colorRatio[0] = (double)r / total;
        colorRatio[1] = (double)g / total;
        colorRatio[2] = (double)b / total;

        telemetry.addLine()
                .addData("Red  ", colorSensor.red())
                .addData("Green", colorSensor.green())
                .addData("Blue ", colorSensor.blue());
        telemetry.addData("color", "rgb ratio, %.2f, %.2f, %.2f", colorRatio[0], colorRatio[1], colorRatio[2]);
    }

    /**
     * Wait until robot wheel motors complete actions.
     */
    private void waitRobotRun() {
        waitMotorActionComplete(FrontLeftDrive);
        waitMotorActionComplete(FrontRightDrive);
        waitMotorActionComplete(BackLeftDrive);
        waitMotorActionComplete(BackRightDrive);
    }

    /**
     * Wait until slider motors complete actions.
     */
    private void waitSliderRun() {
        waitMotorActionComplete(RightSliderMotor);
        waitMotorActionComplete(LeftSliderMotor);
    }

    /**
     * Set slider motors position.
     * @param sliderMotorPosition the target position for slider left motor and right motor.
     */
    private void setSliderPosition(int sliderMotorPosition) {
        RightSliderMotor.setTargetPosition(sliderMotorPosition);
        LeftSliderMotor.setTargetPosition(sliderMotorPosition);
    }

    /**
     * Read current slider motors position. Return the mean value of left and right motor positions.
     * return slider motor position.
     */
    private int getSliderPosition() {
        int r = RightSliderMotor.getCurrentPosition();
        int l = LeftSliderMotor.getCurrentPosition();
        return (r+l)/2;
    }

}