package me.angelstoyanov.sporton.apigw.constant;

import java.lang.annotation.Native;

public class LoggerAdditionalProperties {
    @Native public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    @Native public static final String REQUEST_ID_LOGGING_NAME = "requestId";
    @Native public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
    @Native public static final String CORRELATION_ID_LOGGING_NAME = "correlationId";
}
