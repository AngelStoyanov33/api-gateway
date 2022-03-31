package me.angelstoyanov.sporton.apigw.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Exchange;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("CORSInterceptor")
@RegisterForReflection
@ApplicationScoped
public class CORSInterceptor {

    public static final String ALLOWED_ORIGINS = "*";
    public static final String ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS";
    public static final String ALLOWED_HEADERS = "X-Requested-With,Content-Type,Accept,Origin,Authorization";

    public void allowCorsInput(Exchange exchange) {
        exchange.getIn().setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGINS);
        exchange.getIn().setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        exchange.getIn().setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
    }

    public void allowCorsOutput(Exchange exchange) {
        exchange.getIn().setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGINS);
    }
}
