/*
 * ByteBuffer.java
 * Created on 25.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.common;

/**
 * {@link ByteBuffer} is a wrapper for byte[], and allows to safely read and write from the buffer.
 * {@link ByteBuffer} is thread safe and guarantees proper handling of subsequent reads and writes, if they are within availabla buffer size.
 */
public class ByteBuffer {

    /**
     * Main buffer, in which all data is stored.
     */
    private byte[] buffer;

    /**
     * Current position of read marker.
     */
    private int readMarker;

    /**
     * Current position of write marker.
     */
    private int writeMarker;

    /**
     * Allocate new {@link ByteBuffer} with given size.
     *
     * @param capacity
     *          max capacity of constructed buffer.
     */
    public ByteBuffer(int capacity) {
        if (capacity <= 0) {
           throw new IllegalArgumentException("Buffer must have positive capacity");
        }

        buffer = new byte[capacity];
        readMarker = 0;
        writeMarker = 0;
    }

    /**
     * Attempts to write given byte array to this {@link ByteBuffer}.
     * Writes as many bytes as it can, so that partial data from given array can be written
     * if not available space for whole array is found in buffer.
     *
     * @param bytes
     *          bytes to write
     *
     * @return number of bytes that were actualy written
     */
    public synchronized int write(byte[] bytes) {
        if (shouldRewindBuffer(bytes)) {
            rewind();
        }

        int actualBytesToWrite = Math.min(bytes.length, buffer.length - writeMarker);

        System.arraycopy(bytes, 0, buffer, writeMarker, actualBytesToWrite);

        writeMarker += actualBytesToWrite;

        return actualBytesToWrite;
    }

    /**
     * Attempts to read given number of bytes from this {@link ByteBuffer}.
     * If buffer currently contains less bytes than requested, this method reads only available number of bytes.
     *
     * @param numberOfBytes
     *          requested number of bytes to read
     * @return actual bytes that could be read from this buffer.
     */
    public synchronized byte[] read(int numberOfBytes) {
        int actualNumberOfBytes = Math.min(numberOfBytes, size());
        byte[] result = new byte[actualNumberOfBytes];

        System.arraycopy(buffer, readMarker, result, 0, result.length);

        readMarker += actualNumberOfBytes;

        return result;
    }

    /**
     * Returns current size of the buffer (number of available data to read).
     *
     * @return size of the buffer in bytes
     */
    public synchronized int size() {
        return writeMarker - readMarker;
    }

    /**
     * Returns max capacity of the buffer (number of total data that can be stored in the buffer).
     *
     * @return capacity of the buffer in bytes
     */
    public synchronized int capacity() {
        return buffer.length;
    }

    /**
     * Returns remainig space in the buffer (number of bytes that can still be written to this buffer until it gets full).
     *
     * @return remaining free space in this buffer
     */
    public synchronized int remainingSpace() {
        return capacity() - size();
    }

    /**
     * Check if buffer should be rewind in order to write given byte array.
     *
     * @param bytesToWrite
     *          byte array that we want to write to a buffer
     * @return true if buffer can and should be rewind, false otherwise
     */
    private boolean shouldRewindBuffer(byte[] bytesToWrite) {
        return bytesToWrite.length > buffer.length - writeMarker && readMarker > 0;
    }

    /**
     * Moves all unread bytes to the beginning of the buffer, removing bytes that are already read and freeing space for new writes.
     */
    private void rewind() {
        int currentSize = size();

        byte[] newBuffer = new byte[capacity()];
        System.arraycopy(buffer, readMarker, newBuffer, 0, currentSize);
        buffer = newBuffer;

        readMarker = 0;
        writeMarker = currentSize;
    }
}
