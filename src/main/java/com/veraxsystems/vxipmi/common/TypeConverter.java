/*
 * TypeConverter.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.common;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

/**
 * Tool class for converting types.
 */
public final class TypeConverter {

    /**
     * Array that maps bit position to its value in a single little-endian byte.
     */
    private static final int[] SINGLE_BIT_MASKS = new int[] {1, 2, 4, 8, 16, 32, 64, 128};

    private static final int MAX_WORD_SIZE = 65535;

    private TypeConverter() {
    }

    /**
     * Converts int to byte array in BigEndian convention. Encodes unsigned byte
     * in Java byte representation.
     *
     * @see TypeConverter#intToByte(int)
     * @param value
     * @return int converted to byte array
     */
    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = intToByte(((value >>> offset) & 0xFF));
        }
        return b;
    }

    /**
     * Converts byte array in BigEndian convention to int. Encodes unsigned
     * byte in Java byte representation.
     *
     * @see TypeConverter#intToByte(int)
     * @param value
     *            Byte array holding values.
     * @return Byte array converted to int in a big endian convention.
     * @throws IndexOutOfBoundsException
     *             when value's length is not 4.
     */
    public static int byteArrayToInt(byte[] value) {
        if (value.length != 4) {
            throw new IndexOutOfBoundsException("Value's length must be 4.");
        }
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int offset = (value.length - 1 - i) * 8;
            result |= byteToInt(value[i]) << offset;
        }
        return result;
    }

    /**
     * Converts int to byte array in LittleEndian convention. Encodes unsigned
     * byte in Java byte representation.
     *
     * @see TypeConverter#intToByte(int)
     * @param value
     * @return int converted to byte array
     */
    public static byte[] intToLittleEndianByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            int offset = i * 8;
            b[i] = intToByte(((value >>> offset) & 0xFF));
        }
        return b;
    }

    /**
     * Converts byte array in LittleEndian convention to int. Encodes unsigned
     * byte in Java byte representation.
     *
     * @see TypeConverter#intToByte(int)
     * @param value
     *            Byte array holding values.
     * @return Byte array converted to int in a little endian convention.
     * @throws IndexOutOfBoundsException
     *             when value's length is not 4.
     */
    public static int littleEndianByteArrayToInt(byte[] value) {
        if (value.length != 4) {
            throw new IndexOutOfBoundsException("Value's length must be 4.");
        }
        int result = 0;
        for (int i = 3; i >= 0; --i) {
            int offset = 8 * i;
            result |= byteToInt(value[i]) << offset;
        }
        return result;
    }

    /**
     * Because Java doesn't support unsigned byte values, simple type casting
     * leads to overflows and wrong values. This function casts int to pseudo
     * unsigned byte.
     *
     * @param value
     *            int value (must be in range 0-255)
     * @return byte value
     * @throws IllegalArgumentException
     *             when value is out of range.
     */
    public static byte intToByte(int value) {
        if (value > 255 || value < 0) {
            throw new IllegalArgumentException("Value must be in range 0-255.");
        }
        // if value is less than 128 everything is fine
        // if value would be out of Java byte's range, subtract 255,
        // so it falls into negative byte's range and 2's compliment
        // turns it into proper byte code
        return value < 128 ? (byte) value : (byte) (value - 256);
    }

    /**
     * Because Java doesn't support unsigned byte values, simple type casting
     * leads to overflows and wrong values. This function casts pseudo unsigned
     * byte to int.
     *
     * @param value
     *            byte value
     * @return int value
     */
    public static int byteToInt(byte value) {
        // if value is not less than 0 everything is fine
        // if value is lesser than 0, it means that we encoded there a number
        // greater than 127
        return value >= 0 ? (int) value : ((int) value) + 256;
    }

    /**
     * Converts int to BigEndian word (double bytes array).
     *
     * @param value
     *             integer value to be converted to word (2-bytes array). Must be between 0 and {@link TypeConverter#MAX_WORD_SIZE}.
     * @return double bytes array
     */
    public static byte[] intToWord(int value) {
        if (value < 0 || value > MAX_WORD_SIZE) {
            throw new IllegalArgumentException("Integer to convert to word must be between 0 and " + MAX_WORD_SIZE);
        }

        byte[] tmpBytArray = intToByteArray(value);
        return Arrays.copyOfRange(tmpBytArray, 2, tmpBytArray.length);
    }

    /**
     * Converts int to LittleEndian word (double bytes array).
     *
     * @param value
     *             integer value to be converted to word (2-bytes array). Must be between 0 and {@link TypeConverter#MAX_WORD_SIZE}.
     * @return double bytes array
     */
    public static byte[] intToLittleEndianWord(int value) {
        if (value < 0 || value > MAX_WORD_SIZE) {
            throw new IllegalArgumentException("Integer to convert to word must be between 0 and " + MAX_WORD_SIZE);
        }

        byte[] tmpBytArray = intToLittleEndianByteArray(value);
        return Arrays.copyOf(tmpBytArray, 2);
    }

    /**
     * Converts BigEndian word (double bytes array) to int.
     *
     * @param word
     *             double bytes array.
     * @return int value
     */
    public static int wordToInt(byte[] word) {
        if (word == null || word.length != 2) {
            throw new IllegalArgumentException("Word must consists of 2 bytes");
        }

        byte[] tmpBytArray = new byte[4];
        System.arraycopy(word, 0, tmpBytArray, 2, 2);

        return TypeConverter.byteArrayToInt(tmpBytArray);
    }

    /**
     * Converts LittleEndian word (double bytes array) to int.
     *
     * @param word
     *             double bytes array.
     * @return int value
     */
    public static int littleEndianWordToInt(byte[] word) {
        if (word == null || word.length != 2) {
            throw new IllegalArgumentException("Word must consists of 2 bytes");
        }

        byte[] tmpBytArray = new byte[4];
        System.arraycopy(word, 0, tmpBytArray, 0, 2);

        return TypeConverter.littleEndianByteArrayToInt(tmpBytArray);
    }

    /**
     * Converts BCD encoded byte with bits 7:4 holding the Least Significant
     * digit of the revision and bits 3:0 holding the Most Significant bits.
     *
     * @param value
     *            decoded value
     * @return decoded value
     */
    public static int littleEndianBcdByteToInt(byte value) {
        int lower = (byteToInt(value) & 0xf0) >> 4;
        int higher = byteToInt(value) & 0x0f;

        return higher * 10 + lower;
    }

    /**
     * Decodes 2's complement value that is encoded on lesser than 16 number of
     * bits.
     *
     * @param value
     *            - value to be decoded
     * @param msb
     *            - 0-based index at which the encoded value begins
     * @return decoded value
     */
    public static int decode2sComplement(int value, int msb) {
        int result = value;
        boolean base = false;
        if ((value & (0x1 << msb)) != 0) {
            base = true;
        }
        for (int i = 31; i > msb; --i) {
            int mask = 0x1 << i;
            if (!base) {
                result &= ~mask;
            } else {
                result |= mask;
            }
        }
        return result;
    }

    /**
     * Decodes 1's complement value that is encoded on lesser than 16 number of
     * bits.
     *
     * @param value
     *            - value to be decoded
     * @param msb
     *            - 0-based index at which the encoded value begins
     * @return decoded value
     */
    public static int decode1sComplement(int value, int msb) {
        int result = value;
        boolean base = false;
        if ((value & (0x1 << msb)) != 0) {
            base = true;
        }
        if (base) {

            for (int i = 31; i > msb; --i) {
                int mask = 0x1 << i;
                result |= mask;
            }
            result = -(~result);
        }
        return result;
    }

    /**
     * Decodes text encoded in BCD plus format.
     */
    public static String decodeBcdPlus(byte[] text) {
        char[] result = new char[text.length * 2];

        for (int i = 0; i < text.length; ++i) {
            result[2 * i] = decodeBcdPlusChar(TypeConverter
                    .intToByte((TypeConverter.byteToInt(text[i]) & 0xf0) >> 4));
            result[2 * i + 1] = decodeBcdPlusChar(TypeConverter
                    .intToByte(TypeConverter.byteToInt(text[i]) & 0xf));
        }

        return new String(result);
    }

    private static char decodeBcdPlusChar(byte ch) {
        switch (TypeConverter.byteToInt(ch)) {
        case 0x0:
            return '0';
        case 0x1:
            return '1';
        case 0x2:
            return '2';
        case 0x3:
            return '3';
        case 0x4:
            return '4';
        case 0x5:
            return '5';
        case 0x6:
            return '6';
        case 0x7:
            return '7';
        case 0x8:
            return '8';
        case 0x9:
            return '9';
        case 0xa:
            return ' ';
        case 0xb:
            return '-';
        case 0xc:
            return '.';
        case 0xd:
            return ':';
        case 0xe:
            return ',';
        case 0xf:
            return '_';
        default:
            throw new IllegalArgumentException("Invalid ch value");
        }
    }

    public static String decode6bitAscii(byte[] text) {
        int cnt = text.length;
        if (cnt % 3 != 0) {
            cnt += 3 - cnt % 3;
        }

        byte[] newText = new byte[cnt / 3 * 4];

        int index = 0;
        for (int i = 0; i < text.length; ++i) {
            switch (i % 3) {
            case 0:
                newText[index++] = TypeConverter.intToByte(TypeConverter
                        .byteToInt(text[i]) & 0x3f);
                newText[index] = TypeConverter.intToByte((TypeConverter
                        .byteToInt(text[i]) & 0xc0) >> 6);
                break;
            case 1:
                newText[index++] |= TypeConverter.intToByte((TypeConverter
                        .byteToInt(text[i]) & 0xf) << 2);
                newText[index] = TypeConverter.intToByte((TypeConverter
                        .byteToInt(text[i]) & 0xf0) >> 4);
                break;
            case 2:
                newText[index++] |= TypeConverter.intToByte((TypeConverter
                        .byteToInt(text[i]) & 0x3) << 4);
                newText[index++] = TypeConverter.intToByte((TypeConverter
                        .byteToInt(text[i]) & 0xfc) >> 2);
                break;
            default:
                break;
            }
        }

        for (int i = 0; i < newText.length; ++i) {
            newText[i] = TypeConverter.intToByte(TypeConverter
                    .byteToInt(newText[i]) + 0x20);
        }

        return new String(newText, Charset.forName("US-ASCII"));
    }

    /**
     * Decodes date encoded as number of seconds from 00:00:00, January 1, 1970
     * GMT.
     */
    public static Date decodeDate(int date) {
        return new Date((long) date * 1000);
    }

    /**
     * Checks whether single bit on given position is set in given little-endian byte.
     *
     * @param position
     *             - position of bit to check.
     *               Allowed values are 0 - 7, where 0 is first (less significant) bit and 7 is last (most significant) bit.
     * @param value
     *             - byte in which we check the bytes.
     * @return
     */
    public static boolean isBitSetOnPosition(int position, byte value) {
        return ((value & SINGLE_BIT_MASKS[position]) > 0);
    }

    public static byte setBitOnPosition(int position, byte value) {
        return (byte) (value | SINGLE_BIT_MASKS[position]);
    }
}
