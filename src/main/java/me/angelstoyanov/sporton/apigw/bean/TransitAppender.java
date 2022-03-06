package me.angelstoyanov.sporton.apigw.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import me.angelstoyanov.sporton.apigw.constant.LoggerAdditionalProperties;
import org.apache.camel.Exchange;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.UUID;

@Named("TransitAppender")
@ApplicationScoped
@RegisterForReflection
public class TransitAppender {

    public String generateCorrelationID() {
        return UUID.randomUUID().toString();
    }

    public void appendCorrelationID(Exchange exchange) {
        exchange.getMessage().setHeader(LoggerAdditionalProperties.CORRELATION_ID_HEADER_NAME, generateCorrelationID());
    }
    public String extractCorrelationID(Exchange exchange) {
        return exchange.getMessage().getHeader(LoggerAdditionalProperties.CORRELATION_ID_HEADER_NAME, String.class);
    }
}
