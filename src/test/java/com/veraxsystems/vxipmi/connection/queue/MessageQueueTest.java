/*
 * MessageQueueTest.java
 * Created on 2011-08-24
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.connection.queue;

import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessageQueueTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Connection connection;

    private MessageQueue messageQueue;
    private final int minSequenceNumber = 1;
    private final int maxSequenceNumber = 63;

    @Before
    public void setUp() throws Exception {
        this.messageQueue = new MessageQueue(connection, 10000, minSequenceNumber, maxSequenceNumber);
    }

    @Test
    public void getTimeoutReturnsValuePassedInConstructor() throws Exception {
        int timeout = 20;
        messageQueue = new MessageQueue(connection, timeout, minSequenceNumber, maxSequenceNumber);

        assertEquals(timeout, messageQueue.getTimeout());
    }

    @Test
    public void setTimeoutChangesPreviouslySetTimeout() throws Exception {
        int timeout = 111;
        messageQueue.setTimeout(timeout);

        assertEquals(timeout, messageQueue.getTimeout());
    }

    @Test
    public void addWhenQueueEmpty() throws Exception {
        int sequenceNumber = messageQueue.add(mock(IpmiCommandCoder.class));

        assertEquals(minSequenceNumber, sequenceNumber);
    }

    @Test
    public void addWhenSomeMessagesAlreadyInQueue() throws Exception {
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.add(mock(IpmiCommandCoder.class));

        int sequenceNumber = messageQueue.add(mock(IpmiCommandCoder.class));

        assertEquals(3, sequenceNumber);
    }

    @Test
    public void addWhenSequenceNumberOverflowed() throws Exception {
        messageQueue.setTimeout(-1);

        for (int i = 0; i < maxSequenceNumber; ++i) {
            messageQueue.add(mock(IpmiCommandCoder.class));
        }

        int sequenceNumber = messageQueue.add(mock(IpmiCommandCoder.class));
        assertEquals(minSequenceNumber, sequenceNumber);
    }

    @Test
    public void addWhenQueueIsFull() throws Exception {
        for (int i = 0; i < 8; ++i) {
            messageQueue.add(mock(IpmiCommandCoder.class));
        }

        int sequenceNumber = messageQueue.add(mock(IpmiCommandCoder.class));

        assertEquals(-1, sequenceNumber);
    }

    @Test
    public void addWhenSomeMessagesAddedAndSequenceNumbersReturned() throws Exception {
        messageQueue.getSequenceNumber();
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.getSequenceNumber();
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.getSequenceNumber();

        int sequenceNumber = messageQueue.add(mock(IpmiCommandCoder.class));
        assertEquals(7, sequenceNumber);
    }

    @Test
    public void removeOneMessageByTag() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.remove(tag);

        assertNull("Message that was removed, should not be found", messageQueue.getMessageFromQueue(tag));
    }

    @Test
    public void removeOneMessageByIndex() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));
        int index = messageQueue.getMessageIndexFromQueue(tag);
        messageQueue.removeAt(index);

        assertNull("Message that was removed, should not be found", messageQueue.getMessageFromQueue(tag));
    }

    @Test
    public void removeOneMessageByIndexWhenMessageNotExists() throws Exception {
        expectedException.expect(IndexOutOfBoundsException.class);

        messageQueue.removeAt(17);
    }

    @Test
    public void containsIdWhenMessageDoesntExist() throws Exception {
        assertFalse("Empty queue should not contain any message", messageQueue.containsId(20));
    }

    @Test
    public void containsIdWhenMessageExists() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));

        assertTrue(messageQueue.containsId(tag));
    }

    @Test
    public void getSequenceNumberWhenEmptyQueue() throws Exception {
        assertEquals(minSequenceNumber, messageQueue.getSequenceNumber());
    }

    @Test
    public void getSequenceNumberWhenSomeMessagesAdded() throws Exception {
        int lastSequenceNumber = 0;

        for (int i = 0; i < 4; ++i) {
            lastSequenceNumber = messageQueue.add(mock(IpmiCommandCoder.class));
        }

        int expectedSequenceNumber = lastSequenceNumber + 1;
        assertEquals(expectedSequenceNumber, messageQueue.getSequenceNumber());
    }

    @Test
    public void getSequenceNumberWhenSequenceNumberOverflow() throws Exception {
        for (int i = 0; i < maxSequenceNumber; ++i) {
            messageQueue.getSequenceNumber();
        }

        int sequenceNumber = messageQueue.getSequenceNumber();
        assertEquals(minSequenceNumber, sequenceNumber);
    }

    @Test
    public void getSequenceNumberWhenSomeNumbersAlreadyReturned() throws Exception {
        int lastSequenceNumber = 0;

        for (int i = 0; i < 7; ++i) {
            lastSequenceNumber = messageQueue.getSequenceNumber();
        }

        int expectedSequenceNumber = lastSequenceNumber + 1;
        assertEquals(expectedSequenceNumber, messageQueue.getSequenceNumber());
    }

    @Test
    public void getSequenceNumberWhenSomeMessagesAddedAndSequenceNumbersReturned() throws Exception {
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.getSequenceNumber();
        messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.getSequenceNumber();

        int sequenceNumber = messageQueue.getSequenceNumber();
        assertEquals(6, sequenceNumber);
    }

    @Test
    public void getMessageFromQueueWhenQueueIsEmpty() throws Exception {
        assertNull("Message should be returned from empty queue", messageQueue.getMessageFromQueue(10));
    }

    @Test
    public void getMessageFromQueueWhenNoSuchMessage() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));
        int notExistingTag = tag + 5;

        assertNull("Message that was not added to queue, should not be returned from it", messageQueue.getMessageFromQueue(notExistingTag));
    }

    @Test
    public void getMessageFromQueueWhenMessageExists() throws Exception {
        PayloadCoder message = mock(IpmiCommandCoder.class);
        int tag = messageQueue.add(message);

        assertSame(message, messageQueue.getMessageFromQueue(tag));
    }

    @Test
    public void getMessageIndexFromQueueWhenQueueIsEmpty() throws Exception {
        assertEquals(-1, messageQueue.getMessageIndexFromQueue(15));
    }

    @Test
    public void getMessageIndexFromQueueWhenNoSuchMessage() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));
        int notExistingTag = tag + 10;

        assertEquals(-1, messageQueue.getMessageIndexFromQueue(notExistingTag));
    }

    @Test
    public void getMessageIndexFromQueueWhenMessageExists() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));

        assertEquals(0, messageQueue.getMessageIndexFromQueue(tag));
    }

    @Test
    public void messageIsRemovedAfterTimeout() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.setTimeout(-1);
        messageQueue.run();

        assertFalse("Message that timed out should be removed", messageQueue.containsId(tag));
    }

    @Test
    public void connectionIsNotifiedWhenMessageTimedOut() throws Exception {
        int tag = messageQueue.add(mock(IpmiCommandCoder.class));
        messageQueue.setTimeout(-1);
        messageQueue.run();

        verify(connection).notifyResponseListeners(anyInt(), eq(tag), isNull(ResponseData.class), any(ConnectionException.class));
    }
}