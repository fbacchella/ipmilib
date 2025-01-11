package com.veraxsystems.vxipmi.sm.actions;

public class ExceptionAction<T extends Throwable> extends StateMachineAction{

    private final T exception;

    protected ExceptionAction(T e) {
        exception = e;
    }

    public T getException() {
        return exception;
    }
}
