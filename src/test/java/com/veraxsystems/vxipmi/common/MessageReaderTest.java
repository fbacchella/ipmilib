/*
 * MessageReaderTest.java
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

import static org.junit.Assert.assertArrayEquals;

public class MessageReaderTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createMessageReaderWhenMessageNull() throws Exception {
        expectedException.expect(NullPointerException.class);

        new MessageReader(null);
    }

    @Test
    public void readNextFieldWhenMessageEmpty() throws Exception {
        MessageReader messageReader = new MessageReader(new byte[0]);

        expectedException.expect(ArrayIndexOutOfBoundsException.class);

        messageReader.readNextField(1);
    }

    @Test
    public void readNextFieldWhenFieldExcedingMessageLength() throws Exception {
        MessageReader messageReader = new MessageReader(new byte[10]);

        expectedException.expect(ArrayIndexOutOfBoundsException.class);

        messageReader.readNextField(15);
    }

    @Test
    public void readNextFieldWhenFieldLengthMessThanMessageLength() throws Exception {
        MessageReader messageReader = new MessageReader(new byte[] {1, 2, 3, 4});
        byte[] expectedFieldData = new byte[] {1, 2};
        byte[] actualFieldData = messageReader.readNextField(2);

        assertArrayEquals(expectedFieldData, actualFieldData);
    }

    @Test
    public void readNextFieldWhenSomeFieldsWereRead() throws Exception {
        MessageReader messageReader = new MessageReader(new byte[] {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5});
        messageReader.readNextField(3);
        messageReader.readNextField(3);
        messageReader.readNextField(2);

        byte[] expectedFieldData = new byte[] {4, 4, 4};
        byte[] actualFieldData = messageReader.readNextField(3);

        assertArrayEquals(expectedFieldData, actualFieldData);
    }
}