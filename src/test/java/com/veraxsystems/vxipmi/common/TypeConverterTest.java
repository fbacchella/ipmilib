/*
 * TypeConverterTest.java
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.swing.text.TextAction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeConverterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void isBitSetOnPositionWhenValueIs0() {
        byte value = 0;

        testBitSetsOnPositions(value, false, false, false, false, false, false, false, false);
    }

    @Test
    public void isBitSetOnPositionWhenValueIsGreaterThan0() {
        byte value = 97;

        testBitSetsOnPositions(value, true, false, false, false, false, true, true, false);
    }

    @Test
    public void isBitSetOnPositionWhenValue255() {
        byte value = TypeConverter.intToByte(255);

        testBitSetsOnPositions(value, true, true, true, true, true, true, true, true);
    }

    @Test
    public void isBitSetOnPositionOutOfBounds() {
        byte value = TypeConverter.intToByte(255);

        expectedException.expect(ArrayIndexOutOfBoundsException.class);

        TypeConverter.isBitSetOnPosition(10, value);
    }

    @Test
    public void setBitOnPositionWhenBitIsNotSet() {
        byte value = 0;
        byte convertedValue = TypeConverter.setBitOnPosition(0, value);
        byte expectedValue = 1;

        assertEquals(expectedValue, convertedValue);
    }

    @Test
    public void setBitOnPositionWhenBitIsAlreadySet() {
        byte value = 1;
        byte convertedValue = TypeConverter.setBitOnPosition(0, value);

        assertEquals(value, convertedValue);
    }

    @Test
    public void setBitOnPositionTestMoreBits() {
        byte value = 0;
        byte expectedValue = TypeConverter.intToByte(202);

        byte convertedValue = TypeConverter.setBitOnPosition(1, value);
        convertedValue = TypeConverter.setBitOnPosition(3, convertedValue);
        convertedValue = TypeConverter.setBitOnPosition(6, convertedValue);
        convertedValue = TypeConverter.setBitOnPosition(7, convertedValue);

        assertEquals(expectedValue, convertedValue);
    }

    @Test
    public void setBitOnPositionStartingFromLastBit() {
        byte value = 0;
        byte expectedValue = TypeConverter.intToByte(192);

        byte convertedValue = TypeConverter.setBitOnPosition(7, value);
        convertedValue = TypeConverter.setBitOnPosition(6, convertedValue);

        assertEquals(expectedValue, convertedValue);
    }

    @Test
    public void wordToIntNullInputArray() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.wordToInt(null);
    }

    @Test
    public void wordToIntEmptyInputArray() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.wordToInt(new byte[0]);
    }

    @Test
    public void wordToInt0() {
        byte[] word = new byte[] {0, 0};
        int expectedResult = 0;
        int convertedValue = TypeConverter.wordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void wordToIntFirstByteNon0() {
        byte[] word = new byte[] {10, 0};
        int expectedResult = 2560;
        int convertedValue = TypeConverter.wordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void wordToIntSecondByteNon0() {
        byte[] word = new byte[] {0, 7};
        int expectedResult = 7;
        int convertedValue = TypeConverter.wordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void wordToIntBothBytesNon0() {
        byte[] word = new byte[] {1, 3};
        int expectedResult = 259;
        int convertedValue = TypeConverter.wordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void wordToIntMoreThan2Bytes() {
        byte[] word = new byte[] {4, 7, 8};

        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.wordToInt(word);
    }

    @Test
    public void intToWord0() {
        byte[] expectedWord = new byte[] {0, 0};
        byte[] actualWord = TypeConverter.intToWord(0);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToWordReturnsNonZeroFirstByte() {
        byte[] expectedWord = new byte[] {10, 0};
        byte[] actualWord = TypeConverter.intToWord(2560);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToWordReturnsNonZeroSecondByte() {
        byte[] expectedWord = new byte[] {0, 7};
        byte[] actualWord = TypeConverter.intToWord(7);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToWordReturnsBothBytesNonZero() {
        byte[] expectedWord = new byte[] {1, 3};
        byte[] actualWord = TypeConverter.intToWord(259);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToWordParamLessThan0() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.intToWord(-1);
    }

    @Test
    public void intToWordParamGreaterThanMax() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.intToWord(65536);
    }

    @Test
    public void littleEndianWordToIntNullInputArray() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.littleEndianWordToInt(null);
    }

    @Test
    public void littleEndianWordToIntEmptyInputArray() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.littleEndianWordToInt(new byte[0]);
    }

    @Test
    public void littleEndianWordToInt0() {
        byte[] word = new byte[] {0, 0};
        int expectedResult = 0;
        int convertedValue = TypeConverter.littleEndianWordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void littleEndianWordToIntFirstByteNon0() {
        byte[] word = new byte[] {10, 0};
        int expectedResult = 10;
        int convertedValue = TypeConverter.littleEndianWordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void littleEndianWordToIntSecondByteNon0() {
        byte[] word = new byte[] {0, 7};
        int expectedResult = 1792;
        int convertedValue = TypeConverter.littleEndianWordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void littleEndianWordToIntBothBytesNon0() {
        byte[] word = new byte[] {1, 3};
        int expectedResult = 769;
        int convertedValue = TypeConverter.littleEndianWordToInt(word);

        assertEquals(expectedResult, convertedValue);
    }

    @Test
    public void littleEndianWordToIntMoreThan2Bytes() {
        byte[] word = new byte[] {4, 7, 8};

        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.littleEndianWordToInt(word);
    }

    @Test
    public void intToLittleEndianWord0() {
        byte[] expectedWord = new byte[] {0, 0};
        byte[] actualWord = TypeConverter.intToLittleEndianWord(0);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToLittleEndianWordReturnsNonZeroFirstByte() {
        byte[] expectedWord = new byte[] {10, 0};
        byte[] actualWord = TypeConverter.intToLittleEndianWord(10);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToLittleEndianWordReturnsNonZeroSecondByte() {
        byte[] expectedWord = new byte[] {0, 7};
        byte[] actualWord = TypeConverter.intToLittleEndianWord(1792);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToLittleEndianWordReturnsBothBytesNonZero() {
        byte[] expectedWord = new byte[] {1, 3};
        byte[] actualWord = TypeConverter.intToLittleEndianWord(769);

        assertArrayEquals(expectedWord, actualWord);
    }

    @Test
    public void intToLittleEndianWordParamLessThan0() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.intToLittleEndianWord(-1);
    }

    @Test
    public void intToLittleEndianWordParamGreaterThanMax() {
        expectedException.expect(IllegalArgumentException.class);

        TypeConverter.intToLittleEndianWord(65536);
    }

    private void testBitSetsOnPositions(byte value, boolean... bits) {
        for (int i = 0; i < bits.length; i++) {
            boolean shouldBeSet = bits[i];

            if (shouldBeSet) {
                assertTrue("Bit on position " + i + " should be set for byte " + value, TypeConverter.isBitSetOnPosition(i, value));
            } else {
                assertFalse("Bit on position" + i + " should not be set for byte " + value, TypeConverter.isBitSetOnPosition(i, value));
            }
        }
    }
}