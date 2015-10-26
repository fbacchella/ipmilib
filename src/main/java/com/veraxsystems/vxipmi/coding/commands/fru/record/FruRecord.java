/*
 * FruRecord.java 
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

import java.nio.charset.Charset;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * General class for FRU Inventory area records.
 */
public abstract class FruRecord {

	/**
	 * Creates and populates record
	 */
	public FruRecord() {

	}

	/**
	 * Decodes string using the type/length field specification.
	 * 
	 * @param typeFormat
	 *            - 00 - binary or unspecified 01 - BCD plus (see below) 10 -
	 *            6-bit ASCII, packed (overrides Language Codes) 11 -
	 *            Interpretation depends on Language Codes. 11b indicates 8-bit
	 *            ASCII + Latin 1 if the Language Code is English for the area
	 *            or record containing the field, or 2-byte UNICODE (least
	 *            significant byte first) if the Language Code is not English.
	 * @param data
	 *            - encoded data
	 * @param isEnglishLanguageCode
	 *            - true if the Language Code is English
	 * @return - decoded string
	 */
	protected static String decodeString(int typeFormat, byte[] data,
			boolean isEnglishLanguageCode) {
		// TODO: Test different encoding types
		switch (typeFormat) {
		case 0:
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < data.length; ++i) {
				int num = TypeConverter.byteToInt(data[i]);
				if (num < 0x10) {
					sb.append("0");
				}
				sb.append(Integer.toHexString(num));
			}

			return sb.toString();
		case 1:
			return TypeConverter.decodeBcdPlus(data);
		case 2:
			return TypeConverter.decode6bitAscii(data);
		case 3:
			System.arraycopy(data, 0, data, 0, data.length);
			if (isEnglishLanguageCode) {
				return new String(data, Charset.forName("ISO-8859-1")).trim();
			} else {
				return new String(data, Charset.forName("UTF-8")).trim();
			}
		default:
			throw new IllegalArgumentException("Invalid type format");
		}
	}
}
