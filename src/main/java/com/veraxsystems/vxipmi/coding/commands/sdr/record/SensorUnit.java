/*
 * SensorUnit.java 
 * Created on 2011-08-04
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr.record;

import org.apache.log4j.Logger;

/**
 * Specifies available units for sensors' measurements.
 */
public enum SensorUnit {
    Nits(SensorUnit.NITS), Lumen(SensorUnit.LUMEN), Lux(SensorUnit.LUX), Candela(SensorUnit.CANDELA), kPa(
            SensorUnit.KPA), Psi(SensorUnit.PSI), Newton(SensorUnit.NEWTON), CuIn(SensorUnit.CUIN), Cfm(SensorUnit.CFM), Cufeet(
            SensorUnit.CUFEET), Rpm(SensorUnit.RPM), mm(SensorUnit.MM), Hz(SensorUnit.HZ), cm(SensorUnit.CM), m(
            SensorUnit.M), CuCm(SensorUnit.CUCM), CuM(SensorUnit.CUM), Henry(SensorUnit.HENRY), Liters(
            SensorUnit.LITERS), Millihenry(SensorUnit.MILLIHENRY), FluidOunce(SensorUnit.FLUIDOUNCE), Farad(
            SensorUnit.FARAD), Radians(SensorUnit.RADIANS), Microfarad(SensorUnit.MICROFARAD), Ohms(SensorUnit.OHMS), Siemens(
            SensorUnit.SIEMENS), Unspecified(SensorUnit.UNSPECIFIED), Mole(SensorUnit.MOLE), DegreesC(
            SensorUnit.DEGREESC), Becquerel(SensorUnit.BECQUEREL), Byte(SensorUnit.BYTE), DegreesF(SensorUnit.DEGREESF), PartsPerMilion(
            SensorUnit.PARTSPERMILION), DegreesK(SensorUnit.DEGREESK), Kilobyte(SensorUnit.KILOBYTE), Megabyte(
            SensorUnit.MEGABYTE), Volts(SensorUnit.VOLTS), Gigabyte(SensorUnit.GIGABYTE), Amps(SensorUnit.AMPS), Word(
            SensorUnit.WORD), Watts(SensorUnit.WATTS), Dword(SensorUnit.DWORD), Joules(SensorUnit.JOULES), Qword(
            SensorUnit.QWORD), Coulombs(SensorUnit.COULOMBS), UncorrectableError(SensorUnit.UNCORRECTABLEERROR), Line(
            SensorUnit.LINE), Va(SensorUnit.VA), FatalError(SensorUnit.FATALERROR), Hit(SensorUnit.HIT), Grams(
            SensorUnit.GRAMS), Miss(SensorUnit.MISS), Microsecond(SensorUnit.MICROSECOND), Millisecond(
            SensorUnit.MILLISECOND), Second(SensorUnit.SECOND), Minute(SensorUnit.MINUTE), Hour(SensorUnit.HOUR), Day(
            SensorUnit.DAY), Week(SensorUnit.WEEK), Mil(SensorUnit.MIL), Steradians(SensorUnit.STERADIANS), Inches(
            SensorUnit.INCHES), Revolutions(SensorUnit.REVOLUTIONS), Feet(SensorUnit.FEET), Cycles(SensorUnit.CYCLES), Gravities(
            SensorUnit.GRAVITIES), Ounce(SensorUnit.OUNCE), Pound(SensorUnit.POUND), FtLb(SensorUnit.FTLB), Decibels(
            SensorUnit.DECIBELS), OzIn(SensorUnit.OZIN), DbA(SensorUnit.DBA), Gauss(SensorUnit.GAUSS), DbC(
            SensorUnit.DBC), Gilberts(SensorUnit.GILBERTS), Gray(SensorUnit.GRAY), Sievert(SensorUnit.SIEVERT), ColorTempDegK(
            SensorUnit.COLORTEMPDEGK), bit(SensorUnit.BIT), kilobit(SensorUnit.KILOBIT), Retry(SensorUnit.RETRY), Reset(
            SensorUnit.RESET), Megabit(SensorUnit.MEGABIT), OverrunOverflow(SensorUnit.OVERRUNOVERFLOW), Gigabit(
            SensorUnit.GIGABIT), Underrun(SensorUnit.UNDERRUN), Collision(SensorUnit.COLLISION), Packets(
            SensorUnit.PACKETS), Messages(SensorUnit.MESSAGES), Characters(SensorUnit.CHARACTERS), Error(
            SensorUnit.ERROR), CorrectableError(SensorUnit.CORRECTABLEERROR), Other(SensorUnit.OTHER), ;
    private static final int NITS = 10;

    private static final int LUMEN = 11;

    private static final int LUX = 12;

    private static final int CANDELA = 13;

    private static final int KPA = 14;

    private static final int PSI = 15;

    private static final int NEWTON = 16;

    private static final int CUIN = 30;

    private static final int CFM = 17;

    private static final int CUFEET = 31;

    private static final int RPM = 18;

    private static final int MM = 32;

    private static final int HZ = 19;

    private static final int CM = 33;

    private static final int M = 34;

    private static final int CUCM = 35;

    private static final int CUM = 36;

    private static final int HENRY = 50;

    private static final int LITERS = 37;

    private static final int MILLIHENRY = 51;

    private static final int FLUIDOUNCE = 38;

    private static final int FARAD = 52;

    private static final int RADIANS = 39;

    private static final int MICROFARAD = 53;

    private static final int OHMS = 54;

    private static final int SIEMENS = 55;

    private static final int UNSPECIFIED = 0;

    private static final int MOLE = 56;

    private static final int DEGREESC = 1;

    private static final int BECQUEREL = 57;

    private static final int BYTE = 70;

    private static final int DEGREESF = 2;

    private static final int PARTSPERMILION = 58;

    private static final int DEGREESK = 3;

    private static final int KILOBYTE = 71;

    private static final int MEGABYTE = 72;

    private static final int VOLTS = 4;

    private static final int GIGABYTE = 73;

    private static final int AMPS = 5;

    private static final int WORD = 74;

    private static final int WATTS = 6;

    private static final int DWORD = 75;

    private static final int JOULES = 7;

    private static final int QWORD = 76;

    private static final int COULOMBS = 8;

    private static final int UNCORRECTABLEERROR = 90;

    private static final int LINE = 77;

    private static final int VA = 9;

    private static final int FATALERROR = 91;

    private static final int HIT = 78;

    private static final int GRAMS = 92;

    private static final int MISS = 79;

    private static final int MICROSECOND = 20;

    private static final int MILLISECOND = 21;

    private static final int SECOND = 22;

    private static final int MINUTE = 23;

    private static final int HOUR = 24;

    private static final int DAY = 25;

    private static final int WEEK = 26;

    private static final int MIL = 27;

    private static final int STERADIANS = 40;

    private static final int INCHES = 28;

    private static final int REVOLUTIONS = 41;

    private static final int FEET = 29;

    private static final int CYCLES = 42;

    private static final int GRAVITIES = 43;

    private static final int OUNCE = 44;

    private static final int POUND = 45;

    private static final int FTLB = 46;

    private static final int DECIBELS = 60;

    private static final int OZIN = 47;

    private static final int DBA = 61;

    private static final int GAUSS = 48;

    private static final int DBC = 62;

    private static final int GILBERTS = 49;

    private static final int GRAY = 63;

    private static final int SIEVERT = 64;

    private static final int COLORTEMPDEGK = 65;

    private static final int BIT = 66;

    private static final int KILOBIT = 67;

    private static final int RETRY = 80;

    private static final int RESET = 81;

    private static final int MEGABIT = 68;

    private static final int OVERRUNOVERFLOW = 82;

    private static final int GIGABIT = 69;

    private static final int UNDERRUN = 83;

    private static final int COLLISION = 84;

    private static final int PACKETS = 85;

    private static final int MESSAGES = 86;

    private static final int CHARACTERS = 87;

    private static final int ERROR = 88;

    private static final int CORRECTABLEERROR = 89;

    private static final int OTHER = 0;

    private static Logger logger = Logger.getLogger(SensorUnit.class);

    private int code;

    SensorUnit(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static SensorUnit parseInt(int value) {
        switch (value) {
        case NITS:
            return Nits;
        case LUMEN:
            return Lumen;
        case LUX:
            return Lux;
        case CANDELA:
            return Candela;
        case KPA:
            return kPa;
        case PSI:
            return Psi;
        case NEWTON:
            return Newton;
        case CUIN:
            return CuIn;
        case CFM:
            return Cfm;
        case CUFEET:
            return Cufeet;
        case RPM:
            return Rpm;
        case MM:
            return mm;
        case HZ:
            return Hz;
        case CM:
            return cm;
        case M:
            return m;
        case CUCM:
            return CuCm;
        case CUM:
            return CuM;
        case HENRY:
            return Henry;
        case LITERS:
            return Liters;
        case MILLIHENRY:
            return Millihenry;
        case FLUIDOUNCE:
            return FluidOunce;
        case FARAD:
            return Farad;
        case RADIANS:
            return Radians;
        case MICROFARAD:
            return Microfarad;
        case OHMS:
            return Ohms;
        case SIEMENS:
            return Siemens;
        case UNSPECIFIED:
            return Unspecified;
        case MOLE:
            return Mole;
        case DEGREESC:
            return DegreesC;
        case BECQUEREL:
            return Becquerel;
        case BYTE:
            return Byte;
        case DEGREESF:
            return DegreesF;
        case PARTSPERMILION:
            return PartsPerMilion;
        case DEGREESK:
            return DegreesK;
        case KILOBYTE:
            return Kilobyte;
        case MEGABYTE:
            return Megabyte;
        case VOLTS:
            return Volts;
        case GIGABYTE:
            return Gigabyte;
        case AMPS:
            return Amps;
        case WORD:
            return Word;
        case WATTS:
            return Watts;
        case DWORD:
            return Dword;
        case JOULES:
            return Joules;
        case QWORD:
            return Qword;
        case COULOMBS:
            return Coulombs;
        case UNCORRECTABLEERROR:
            return UncorrectableError;
        case LINE:
            return Line;
        case VA:
            return Va;
        case FATALERROR:
            return FatalError;
        case HIT:
            return Hit;
        case GRAMS:
            return Grams;
        case MISS:
            return Miss;
        case MICROSECOND:
            return Microsecond;
        case MILLISECOND:
            return Millisecond;
        case SECOND:
            return Second;
        case MINUTE:
            return Minute;
        case HOUR:
            return Hour;
        case DAY:
            return Day;
        case WEEK:
            return Week;
        case MIL:
            return Mil;
        case STERADIANS:
            return Steradians;
        case INCHES:
            return Inches;
        case REVOLUTIONS:
            return Revolutions;
        case FEET:
            return Feet;
        case CYCLES:
            return Cycles;
        case GRAVITIES:
            return Gravities;
        case OUNCE:
            return Ounce;
        case POUND:
            return Pound;
        case FTLB:
            return FtLb;
        case DECIBELS:
            return Decibels;
        case OZIN:
            return OzIn;
        case DBA:
            return DbA;
        case GAUSS:
            return Gauss;
        case DBC:
            return DbC;
        case GILBERTS:
            return Gilberts;
        case GRAY:
            return Gray;
        case SIEVERT:
            return Sievert;
        case COLORTEMPDEGK:
            return ColorTempDegK;
        case BIT:
            return bit;
        case KILOBIT:
            return kilobit;
        case RETRY:
            return Retry;
        case RESET:
            return Reset;
        case MEGABIT:
            return Megabit;
        case OVERRUNOVERFLOW:
            return OverrunOverflow;
        case GIGABIT:
            return Gigabit;
        case UNDERRUN:
            return Underrun;
        case COLLISION:
            return Collision;
        case PACKETS:
            return Packets;
        case MESSAGES:
            return Messages;
        case CHARACTERS:
            return Characters;
        case ERROR:
            return Error;
        case CORRECTABLEERROR:
            return CorrectableError;
        default:
            logger.error("Invalid value: " + value);
            return Other;
        }
    }
}