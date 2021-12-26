package me.angelstoyanov.sporton.apigw;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.configuration.ProfileManager;
import me.angelstoyanov.sporton.apigw.bean.Authenticator;
import me.angelstoyanov.sporton.apigw.bean.TransitAppender;
import me.angelstoyanov.sporton.apigw.config.GatewayConfig;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Named;

@Named("APIGateway")
@RegisterForReflection
public class APIGateway extends RouteBuilder {

    @Inject
    GatewayConfig gatewayConfig;

    @Inject
    CamelContext camelContext;

    @Override
    public void configure() throws Exception {

        // Simple Authentication Service call to test the API Gateway
        if (ProfileManager.getActiveProfile().equalsIgnoreCase("dev")) {
            from("platform-http:/apigw/authenticate")
                    .routeId("[API Gateway] [Authentication Service]")
                    .setHeader("User-Agent", constant("Sporton-API-Gateway/0.0.1-SNAPSHOT"))
                    .bean(TransitAppender.class, "appendCorrelationID")
                    .bean(Authenticator.class, "authenticate")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));
        }

    }
}
