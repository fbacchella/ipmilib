/*
 * QueueElement.java 
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
import com.veraxsystems.vxipmi.coding.commands.ResponseData;

import java.util.Date;

public class QueueElement {
    private int id;
    /**
     * @deprecated retries on message level are deprecated
     */
    @Deprecated
    private int retries;
    private boolean timedOut;

    private PayloadCoder request;
    private ResponseData response;
    private Date timestamp;

    public QueueElement(int id, PayloadCoder request) {
        this.id = id;
        this.request = request;
        timestamp = new Date();
        retries = 0;
        this.timedOut = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @deprecated retries on message level are deprecated
     */
    @Deprecated
    public int getRetries() {
        return retries;
    }

    /**
     * @deprecated retries on message level are deprecated
     */
    @Deprecated
    public void setRetries(int retries) {
        this.retries = retries;
    }

    public PayloadCoder getRequest() {
        return request;
    }

    public void setRequest(PayloadCoder request) {
        this.request = request;
    }

    public ResponseData getResponse() {
        return response;
    }

    public void setResponse(ResponseData response) {
        this.response = response;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void refreshTimestamp() {
        timestamp = new Date();
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void makeTimedOut() {
        this.timedOut = true;
    }
}
