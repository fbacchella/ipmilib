package com.veraxsystems.vxipmi.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by swin on 25.05.2017.
 */
public class ByteBufferTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createWithNegativeSize() {
        expectedException.expect(IllegalArgumentException.class);
        new ByteBuffer(-2);
    }

    @Test
    public void createEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        new ByteBuffer(0);
    }

    @Test
    public void getCapacityAfterConstructed() {
        int capacity = 7;

        ByteBuffer buffer = new ByteBuffer(capacity);
        assertEquals(capacity, buffer.capacity());
    }

    @Test
    public void writeNull() {
        ByteBuffer buffer = new ByteBuffer(2);

        expectedException.expect(NullPointerException.class);
        buffer.write(null);
    }

    @Test
    public void writeZeroBytes() {
        ByteBuffer buffer = new ByteBuffer(5);

        int bytesWritten = buffer.write(new byte[0]);

        assertEquals(0, buffer.size());
        assertEquals(0, bytesWritten);
    }

    @Test
    public void writeMoreBytesThanCapacityAtOnce() {
        ByteBuffer buffer = new ByteBuffer(5);

        int bytesWritten = buffer.write(new byte[7]);

        assertEquals(buffer.capacity(), buffer.size());
        assertEquals(buffer.capacity(), bytesWritten);
    }

    @Test
    public void writeMoreBytesThanCapacityInFewSteps() {
        ByteBuffer buffer = new ByteBuffer(5);

        int bytesWritten = buffer.write(new byte[2]);
        assertEquals(2, bytesWritten);

        bytesWritten = buffer.write(new byte[2]);
        assertEquals(2, bytesWritten);

        bytesWritten = buffer.write(new byte[3]);
        assertEquals(1, bytesWritten);
    }

    @Test
    public void writeLessBytesThanCapacityAtOnce() {
        ByteBuffer buffer = new ByteBuffer(6);
        byte[] bytesToWrite = new byte[4];

        int bytesWritten = buffer.write(bytesToWrite);

        assertEquals(bytesToWrite.length, buffer.size());
        assertEquals(bytesToWrite.length, bytesWritten);
    }

    @Test
    public void writeLessBytesThanCapacityInFewSteps() {
        ByteBuffer buffer = new ByteBuffer(10);
        byte[] firstBytesToWrite = new byte[3];
        int bytesWritten = buffer.write(firstBytesToWrite);

        byte[] secondBytesToWrite = new byte[4];
        bytesWritten += buffer.write(secondBytesToWrite);

        byte[] thirdBytesToWrite = new byte[3];
        bytesWritten += buffer.write(thirdBytesToWrite);

        assertEquals(firstBytesToWrite.length + secondBytesToWrite.length + thirdBytesToWrite.length, buffer.size());
        assertEquals(firstBytesToWrite.length + secondBytesToWrite.length + thirdBytesToWrite.length, bytesWritten);
    }

    @Test
    public void writeExactlyAsManyBytesAsPossible() {
        ByteBuffer buffer = new ByteBuffer(4);

        int bytesWritten = buffer.write(new byte[buffer.capacity()]);

        assertEquals(buffer.capacity(), bytesWritten);
    }

    @Test
    public void readWhenEmpty() {
        ByteBuffer buffer = new ByteBuffer(10);

        byte[] expectedResult = new byte[0];
        assertArrayEquals(expectedResult, buffer.read(5));
    }

    @Test
    public void readWhenFull() {
        ByteBuffer buffer = new ByteBuffer(3);
        buffer.write(new byte[] {1, 2, 3});

        byte[] expectedResult = new byte[] {1, 2, 3};
        byte[] result = buffer.read(3);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void readZeroBytes() {
        ByteBuffer buffer = new ByteBuffer(6);
        buffer.write(new byte[6]);

        byte[] expectedResult = new byte[0];
        byte[] result = buffer.read(0);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void readMoreBytesThanSizeAtOnce() {
        ByteBuffer buffer = new ByteBuffer(12);
        buffer.write(new byte[] {1, 2, 3, 4});

        byte[] expectedResult = new byte[] {1, 2, 3, 4};
        byte[] result = buffer.read(7);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void readMoreBytesThanSizeInFewSteps() {
        ByteBuffer buffer = new ByteBuffer(15);
        buffer.write(new byte[6]);

        byte[] expectedResult = new byte[3];
        byte[] result = buffer.read(3);

        assertArrayEquals(expectedResult, result);

        expectedResult = new byte[3];
        result = buffer.read(5);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void readMoreBytesThanCapacityAtOnce() {
        ByteBuffer buffer = new ByteBuffer(8);
        buffer.write(new byte[buffer.capacity()]);

        byte[] expectedResult = new byte[buffer.capacity()];
        byte[] result = buffer.read(buffer.capacity() + 3);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void readMoreBytesThanCapacityInFewSteps() {
        ByteBuffer buffer = new ByteBuffer(5);
        buffer.write(new byte[buffer.capacity()]);

        byte[] expectedResult = new byte[4];
        byte[] result =  buffer.read(4);

        assertArrayEquals(expectedResult, result);

        expectedResult = new byte[1];
        result =  buffer.read(3);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void readExactlyAsManyBytesAsAvailable() {
        ByteBuffer buffer = new ByteBuffer(12);
        buffer.write(new byte[] {1, 2, 3});

        byte[] expectedResult = new byte[] {1, 2, 3};
        byte[] result = buffer.read(3);

        assertArrayEquals(expectedResult, result);
    }

    @Test
    public void writeAndReadSubsequently() {
        ByteBuffer buffer = new ByteBuffer(10);
        buffer.write(new byte[] {1, 2, 3});
        buffer.read(2);
        buffer.write(new byte[] {4, 5});
        buffer.read(1);
        buffer.write(new byte[] {6, 7, 8});
        buffer.write(new byte[] {9});
        buffer.read(2);

        byte[] expectedResult = new byte[] {6, 7, 8, 9};
        byte[] result = buffer.read(4);

        assertArrayEquals(expectedResult, result);
        assertEquals(0, buffer.size());
    }

    @Test
    public void writeAndReadOverBufferCapacity() {
        ByteBuffer buffer = new ByteBuffer(8);
        buffer.write(new byte[3]);
        buffer.read(3);
        buffer.write(new byte[4]);
        buffer.read(4);
        int bytesWritten = buffer.write(new byte[] {1, 2, 3, 4, 5, 6});
        byte[] bytesRead = buffer.read(6);

        int expectedBytesWritten = 6;
        byte[] expectedBytesRead = new byte[] {1, 2, 3, 4, 5, 6};

        assertEquals(expectedBytesWritten, bytesWritten);
        assertArrayEquals(expectedBytesRead, bytesRead);
    }

    @Test
    public void getSizeAfterFewWritesAndReads() {
        ByteBuffer buffer = new ByteBuffer(5);
        buffer.write(new byte[2]);
        buffer.read(1);
        buffer.write(new byte[3]);
        buffer.read(2);

        int expectedSize = 2;
        assertEquals(expectedSize, buffer.size());
    }

    @Test
    public void getRemainingSpaceWhenBufferEmpty() {
        ByteBuffer buffer = new ByteBuffer(9);

        int expectedRemainingSpace = buffer.capacity();

        assertEquals(expectedRemainingSpace, buffer.remainingSpace());
    }

    @Test
    public void getRemainingSpaceWhenBufferFull() {
        ByteBuffer buffer = new ByteBuffer(18);
        buffer.write(new byte[buffer.capacity()]);

        assertEquals(0, buffer.remainingSpace());
    }

    @Test
    public void getRemainingSpaceAfterFewWritesAndReads() {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.write(new byte[10]);
        buffer.read(5);
        buffer.write(new byte[6]);
        buffer.read(4);
        buffer.write(new byte[8]);
        buffer.read(3);

        assertEquals(4, buffer.remainingSpace());
    }
}