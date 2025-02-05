// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.ArmConstants;

public class Arm extends SubsystemBase {
    private final CANSparkMax extendingMotor, pivotMotor;
    private final PIDController extenderController, pivotController;
    private final AnalogInput potReading;
    private double extenderSetpoint, pivotSetpoint;
    private final ShuffleboardLayout PIVOT, EXTENDER;

    public Arm() {
        extendingMotor = new CANSparkMax(ArmConstants.EXTENDING_MOTOR, MotorType.kBrushless);
        pivotMotor = new CANSparkMax(ArmConstants.PIVOT_MOTOR, MotorType.kBrushless);

        extenderController = ArmConstants.EXTENDER_CONTROLLER;
        pivotController = ArmConstants.PIVOT_CONTROLLER;

        extenderController.setTolerance(.5);

        extendingMotor.setIdleMode(IdleMode.kBrake);

        pivotMotor.getEncoder().setPosition(0);
        pivotMotor.setIdleMode(IdleMode.kBrake);

        potReading = new AnalogInput(Constants.ArmConstants.STRING_POT_CHANNEL);

        PIVOT = ArmConstants.PIVOT;
        EXTENDER = ArmConstants.EXTENDER;

        restPivot();
        restExtender();

        shuffleboardInit();
    }

    public void setPivotSetpoint(double change) {
        pivotSetpoint = change;
    }

    public void setExtenderSetpoint(double change) {
        extenderSetpoint = change;
    }

    public void restPivot() {
        pivotSetpoint = 0;
    }

    public void restExtender() {
        extenderSetpoint = -2;
    }

    private double getExtenderLength() {
        // 595 is the length pulled out by default, 78 ohms per inch
        return (((double) potReading.getAverageValue() - 584.0) / 78.0);
    }

    public void shuffleboardInit() {
        EXTENDER.addDouble("String Pot Reading UNmodified", () -> potReading.getAverageValue());
        EXTENDER.addDouble("String Pot Reading modified", () -> getExtenderLength());

        EXTENDER.addDouble("Setpoint", () -> extenderSetpoint);

        EXTENDER.addBoolean("At setPoint", () -> extenderController.atSetpoint());
        EXTENDER.addDouble("Setpoint Acording to the PID controller", () -> extenderController.getSetpoint());
        EXTENDER.addDouble("Temp", () -> extendingMotor.getMotorTemperature());

        PIVOT.addDouble("Position", () -> pivotMotor.getEncoder().getPosition());
        PIVOT.addDouble("Setpoint", () -> pivotSetpoint);
    }

    @Override
    public void periodic() {
        // limitSetpoint();

        extendingMotor.set(
                Math.min(Math.max(extenderController.calculate(getExtenderLength(), this.extenderSetpoint), -.5), .5));

        pivotMotor.set(
                Math.min(Math.max(
                        pivotController.calculate(this.pivotMotor.getEncoder().getPosition(), this.pivotSetpoint),
                        -.45), .45));
    }
}
