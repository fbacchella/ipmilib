/*
 * MessageComposerTest.java
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

public class MessageComposerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createMessageComposerWhenNegatveMessageSize() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        MessageComposer.get(-2);
    }

    @Test
    public void getMessageWhenNoFieldsAppended() throws Exception {
        MessageComposer composer = MessageComposer.get(5);

        assertArrayEquals(new byte[5], composer.getMessage());
    }

    @Test
    public void getMessageWhenSizeIs0() throws Exception {
        MessageComposer composer = MessageComposer.get(0);

        assertArrayEquals(new byte[0], composer.getMessage());
    }

    @Test
    public void getMessageWhenSomeFieldsAppended() throws Exception {
        MessageComposer composer = MessageComposer.get(5)
            .appendField((byte) 1)
            .appendField((byte) 4)
            .appendField((byte) 2);

        byte[] expectedMessage = new byte[] {1, 4, 2, 0, 0};
        byte[] actualMessage = composer.getMessage();

        assertArrayEquals(expectedMessage, actualMessage);
    }

    @Test
    public void getMessageWhenAllFieldsAppended() throws Exception {
        MessageComposer composer = MessageComposer.get(7)
            .appendField((byte) 2)
            .appendField((byte) 2)
            .appendField((byte) 2)
            .appendField((byte) 10)
            .appendField((byte) 16)
            .appendField((byte) 10)
            .appendField((byte) 33);

        byte[] expectedMessage = new byte[] {2, 2, 2, 10, 16, 10, 33};
        byte[] actualMessage = composer.getMessage();

        assertArrayEquals(expectedMessage, actualMessage);
    }

    @Test
    public void appendFieldWhenMessageEmpty() throws Exception {
        MessageComposer composer = MessageComposer.get(3)
                .appendField((byte) 6);

        byte[] expectedMessage = new byte[] {6, 0, 0};
        byte[] actualMessage = composer.getMessage();

        assertArrayEquals(expectedMessage, actualMessage);
    }

    @Test
    public void appendFieldSingleAndArrays() throws Exception {
        MessageComposer composer = MessageComposer.get(8)
            .appendField(new byte[] {4, 5, 0})
            .appendField((byte) 9)
            .appendField(new byte[] {20})
            .appendField(new byte[] {78, 98})
            .appendField((byte) 3);

        byte[] expectedMessage = new byte[] {4, 5, 0, 9, 20, 78, 98, 3};
        byte[] actualMessage = composer.getMessage();

        assertArrayEquals(expectedMessage, actualMessage);
    }

    @Test
    public void appendFieldSingleByteWhenMessageFull() throws Exception {
        MessageComposer composer = MessageComposer.get(3)
            .appendField(new byte[3]);

        expectedException.expect(ArrayIndexOutOfBoundsException.class);

        composer.appendField((byte) 5);
    }

    @Test
    public void appendFieldArrayWhenMessageFull() throws Exception {
        MessageComposer composer = MessageComposer.get(20)
            .appendField(new byte[20]);

        expectedException.expect(ArrayIndexOutOfBoundsException.class);

        composer.appendField(new byte[] {1, 2, 3});
    }

    @Test
    public void appendFieldArrayWhenFieldExcedesMessageLength() throws Exception {
        MessageComposer composer = MessageComposer.get(15)
            .appendField(new byte[10]);

        expectedException.expect(ArrayIndexOutOfBoundsException.class);

        composer.appendField(new byte[] {7, 8, 23, 55, 66, 98});
    }

    @Test
    public void appendFieldWhenFieldIsEmpty() throws Exception {
        MessageComposer composer = MessageComposer.get(2)
            .appendField(new byte[0]);

        assertArrayEquals(new byte[2], composer.getMessage());
    }
}