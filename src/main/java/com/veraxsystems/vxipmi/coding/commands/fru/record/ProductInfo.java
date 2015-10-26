/*
 * ProductInfo.java 
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

import java.util.ArrayList;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * FRU record containing Board info.<br>
 * This area is present if the FRU itself is a separate product. This is
 * typically seen when the FRU is an add-in card. When this area is provided in
 * the FRU Information Device that contains the Chassis Info Area, the product
 * info is for the overall system, as initially manufactured.
 */
public class ProductInfo extends FruRecord {

	private String manufacturerName = "";

	private String productName = "";

	private String productModelNumber = "";

	private String productVersion = "";

	private String productSerialNumber = "";

	private String assetTag = "";

	private byte[] fruFileId = new byte[0];

	private String[] customProductInfo = new String[0];

	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 */
	public ProductInfo(byte[] fruData, int offset) {
		super();

		if (fruData[offset] != 0x1) {
			throw new IllegalArgumentException("Invalid format version");
		}

		int languageCode = TypeConverter.byteToInt(fruData[offset + 2]);

		int partNumber = TypeConverter.byteToInt(fruData[offset + 3]);

		offset += 4;

		int index = 0;

		ArrayList<String> customInfo = new ArrayList<String>();

		while (partNumber != 0xc1 && offset < fruData.length) {

			int partType = (partNumber & 0xc0) >> 6;

			int partDataLength = (partNumber & 0x3f);

			if (partDataLength > 0 && partDataLength + offset < fruData.length) {

				byte[] partNumberData = new byte[partDataLength];

				System.arraycopy(fruData, offset, partNumberData, 0,
						partDataLength);

				offset += partDataLength;

				switch (index) {
				case 0:
					setManufacturerName(FruRecord.decodeString(partType,
							partNumberData, languageCode != 0
									&& languageCode != 25));
					break;
				case 1:
					setProductName(FruRecord.decodeString(partType,
							partNumberData, languageCode != 0
									&& languageCode != 25));
					break;
				case 2:
					setProductModelNumber(FruRecord.decodeString(partType,
							partNumberData, languageCode != 0
									&& languageCode != 25));
					break;
				case 3:
					setProductVersion(FruRecord.decodeString(partType,
							partNumberData, languageCode != 0
									&& languageCode != 25));
					break;
				case 4:
					setProductSerialNumber(FruRecord.decodeString(partType,
							partNumberData, true));
					break;
				case 5:
					setAssetTag(FruRecord.decodeString(partType,
							partNumberData, languageCode != 0
									&& languageCode != 25));
					break;
				case 6:
					setFruFileId(partNumberData);
					break;
				default:
					if (partDataLength == 0) {
						partNumber = TypeConverter.byteToInt(fruData[offset]);
						++offset;
						continue;
					}
					customInfo.add(FruRecord.decodeString(partType,
							partNumberData, languageCode != 0
									&& languageCode != 25));
					break;
				}
			}

			partNumber = TypeConverter.byteToInt(fruData[offset]);

			++offset;

			++index;
		}

		customProductInfo = new String[customInfo.size()];
		customProductInfo = customInfo.toArray(customProductInfo);
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductModelNumber() {
		return productModelNumber;
	}

	public void setProductModelNumber(String productModelNumber) {
		this.productModelNumber = productModelNumber;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public void setProductSerialNumber(String productSerialNumber) {
		this.productSerialNumber = productSerialNumber;
	}

	public String getProductSerialNumber() {
		return productSerialNumber;
	}

	public String getAssetTag() {
		return assetTag;
	}

	public void setAssetTag(String assetTag) {
		this.assetTag = assetTag;
	}

	public byte[] getFruFileId() {
		return fruFileId;
	}

	public void setFruFileId(byte[] fruFileId) {
		this.fruFileId = fruFileId;
	}

	public String[] getCustomProductInfo() {
		return customProductInfo;
	}

	public void setCustomProductInfo(String[] customProductInfo) {
		this.customProductInfo = customProductInfo;
	}

}
