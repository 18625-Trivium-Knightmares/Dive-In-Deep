package org.firstinspires.ftc.teamcode.opmodes.createmechanism;

import com.acmerobotics.dashboard.FtcDashboard;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandScheduler;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.commands.arm.SetArmLevel;
import org.firstinspires.ftc.teamcode.commands.webcam.DetectTSEPosition;
import org.firstinspires.ftc.teamcode.commands.webcam.MockDetectTSEPosition;
import org.firstinspires.ftc.teamcode.commands.webcam.StreamToDashboard;
import org.firstinspires.ftc.teamcode.cv.OpenCvShippingElementDetector;
import org.firstinspires.ftc.teamcode.subsystems.arm.ArmSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.webcam.WebCamSubsystem;

import java.util.function.Consumer;

public class CreateWebCam {

    private final Telemetry telemetry;
    private WebCamSubsystem subsystem;
    private ArmSubsystem armSubsystem;
    private final HardwareMap hwMap;
    private final String deviceName;
    private final FtcDashboard dashboard;

    private DetectTSEPosition detectTSEPosition;
    private MockDetectTSEPosition mockDetectTSEPosition;
    private StreamToDashboard streamToDashboard;
    private SetArmLevel setArmLevel;

    public CreateWebCam(final HardwareMap hwMap, String webCamName, final FtcDashboard dashboard, final Telemetry telemetry){

        this.hwMap = hwMap;
        deviceName = webCamName;
        this.dashboard = dashboard;
        this.telemetry = telemetry;

    }

    public CreateWebCam(final HardwareMap hwMap, final String webCamName, final FtcDashboard dashboard, final Telemetry telemetry, Boolean autoCreate){
        this.hwMap = hwMap;
        deviceName = webCamName;
        this.dashboard = dashboard;
        this.telemetry = telemetry;

        if (autoCreate) create();
    }

    public CreateWebCam(final HardwareMap hwMap, final String webCamName, final FtcDashboard dashboard, ArmSubsystem arm, final Telemetry telemetry, Boolean autoCreate){
        this.hwMap = hwMap;
        deviceName = webCamName;
        this.dashboard = dashboard;
        armSubsystem = arm;
        this.telemetry = telemetry;

        if (autoCreate) create();
    }

    public void create(){
        subsystem = new WebCamSubsystem(hwMap,deviceName,new OpenCvShippingElementDetector(640,480,telemetry));

        detectTSEPosition = new DetectTSEPosition(subsystem, telemetry);
        streamToDashboard = new StreamToDashboard(subsystem,dashboard);
        setArmLevel = new SetArmLevel(armSubsystem,armSubsystem.getLevel(subsystem.getLevel()),telemetry);
        streamToDashboard.schedule();

        if(armSubsystem != null){
            SequentialCommandGroup sequentialCommandGroup = new SequentialCommandGroup(detectTSEPosition, new InstantCommand( () -> armSubsystem.setArmTargetPosition(armSubsystem.getLevel(subsystem.getLevel()))));
            sequentialCommandGroup.schedule();
        }
        else{
            detectTSEPosition.schedule();
        }

        //CommandScheduler.getInstance().onCommandFinish( detectTSEPosition -> telemetry.addData("got position", subsystem.getLocation()));

    }

    public void createAuto(){
        subsystem = new WebCamSubsystem(hwMap,deviceName,new OpenCvShippingElementDetector(640,480,telemetry));

        mockDetectTSEPosition = new MockDetectTSEPosition(subsystem, telemetry);

        setArmLevel = new SetArmLevel(armSubsystem,armSubsystem.getLevel(subsystem.getLevel()),telemetry);


        if(armSubsystem != null){
            SequentialCommandGroup sequentialCommandGroup = new SequentialCommandGroup(mockDetectTSEPosition, new InstantCommand( () -> armSubsystem.setArmTargetPosition(armSubsystem.getLevel(subsystem.getLevel()))));
            sequentialCommandGroup.schedule();
        }
        else{
            detectTSEPosition.schedule();
        }

        //CommandScheduler.getInstance().onCommandFinish( detectTSEPosition -> telemetry.addData("got position", subsystem.getLocation()));

    }

    public DetectTSEPosition getDetectTSEPositionCommand(){
        return detectTSEPosition;

    }


}
