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
import java.util.Date;

/**
 * Tool class for converting types.
 */
public final class TypeConverter {

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
	public static int littleEndianByteArrayToInt(byte[] value)
			throws IndexOutOfBoundsException {
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
	public static byte intToByte(int value)
			throws IllegalArgumentException {
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
}
