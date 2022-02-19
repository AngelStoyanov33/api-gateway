package me.angelstoyanov.sporton.apigw.exception;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ServiceDownException extends RuntimeException {

    public ServiceDownException(String message) {
        super(message);
    }
    public ServiceDownException(String message, Throwable cause) {
        super(message, cause);
    }
}
