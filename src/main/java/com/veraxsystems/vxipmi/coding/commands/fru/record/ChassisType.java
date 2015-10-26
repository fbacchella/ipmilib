/*
 * ChassisType.java 
 * Created on 2011-08-16
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.fru.record;

import org.apache.log4j.Logger;

/**
 * Specifies possible types of chassis.
 */
public enum ChassisType {

    Other(ChassisType.OTHER), Notebook(ChassisType.NOTEBOOK), HandHeld(ChassisType.HANDHELD), DockingStation(
            ChassisType.DOCKINGSTATION), AllInOne(ChassisType.ALLINONE), SubNotebook(ChassisType.SUBNOTEBOOK), SpaceSaving(
            ChassisType.SPACESAVING), LunchBox(ChassisType.LUNCHBOX), MainServerChassis(ChassisType.MAINSERVERCHASSIS), ExpansionChassis(
            ChassisType.EXPANSIONCHASSIS), SubChassis(ChassisType.SUBCHASSIS), Unknown(ChassisType.UNKNOWN), BusExpansionChassis(
            ChassisType.BUSEXPANSIONCHASSIS), PeripheralChassis(ChassisType.PERIPHERALCHASSIS), RaidChassis(
            ChassisType.RAIDCHASSIS), RackMountChassis(ChassisType.RACKMOUNTCHASSIS), Desktop(ChassisType.DESKTOP), Low(
            ChassisType.LOW), Pizza(ChassisType.PIZZA), Mini(ChassisType.MINI), Tower(ChassisType.TOWER), Portable(
            ChassisType.PORTABLE), LapTop(ChassisType.LAPTOP), ;
    private static final int OTHER = 1;

    private static final int NOTEBOOK = 10;

    private static final int HANDHELD = 11;

    private static final int DOCKINGSTATION = 12;

    private static final int ALLINONE = 13;

    private static final int SUBNOTEBOOK = 14;

    private static final int SPACESAVING = 15;

    private static final int LUNCHBOX = 16;

    private static final int MAINSERVERCHASSIS = 17;

    private static final int EXPANSIONCHASSIS = 18;

    private static final int SUBCHASSIS = 19;

    private static final int UNKNOWN = 2;

    private static final int BUSEXPANSIONCHASSIS = 20;

    private static final int PERIPHERALCHASSIS = 21;

    private static final int RAIDCHASSIS = 22;

    private static final int RACKMOUNTCHASSIS = 23;

    private static final int DESKTOP = 3;

    private static final int LOW = 4;

    private static final int PIZZA = 5;

    private static final int MINI = 6;

    private static final int TOWER = 7;

    private static final int PORTABLE = 8;

    private static final int LAPTOP = 9;

    private int code;

    private static Logger logger = Logger.getLogger(ChassisType.class);

    ChassisType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ChassisType parseInt(int value) {
        switch (value) {
        case OTHER:
            return Other;
        case NOTEBOOK:
            return Notebook;
        case HANDHELD:
            return HandHeld;
        case DOCKINGSTATION:
            return DockingStation;
        case ALLINONE:
            return AllInOne;
        case SUBNOTEBOOK:
            return SubNotebook;
        case SPACESAVING:
            return SpaceSaving;
        case LUNCHBOX:
            return LunchBox;
        case MAINSERVERCHASSIS:
            return MainServerChassis;
        case EXPANSIONCHASSIS:
            return ExpansionChassis;
        case SUBCHASSIS:
            return SubChassis;
        case UNKNOWN:
            return Unknown;
        case BUSEXPANSIONCHASSIS:
            return BusExpansionChassis;
        case PERIPHERALCHASSIS:
            return PeripheralChassis;
        case RAIDCHASSIS:
            return RaidChassis;
        case RACKMOUNTCHASSIS:
            return RackMountChassis;
        case DESKTOP:
            return Desktop;
        case LOW:
            return Low;
        case PIZZA:
            return Pizza;
        case MINI:
            return Mini;
        case TOWER:
            return Tower;
        case PORTABLE:
            return Portable;
        case LAPTOP:
            return LapTop;
        default:
            logger.error("Invalid value: " + value);
            return Other;
        }
    }
}