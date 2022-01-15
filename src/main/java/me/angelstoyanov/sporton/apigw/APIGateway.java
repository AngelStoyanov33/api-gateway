package me.angelstoyanov.sporton.apigw;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.configuration.ProfileManager;
import me.angelstoyanov.sporton.apigw.bean.Authenticator;
import me.angelstoyanov.sporton.apigw.bean.TransitAppender;
import me.angelstoyanov.sporton.apigw.config.GatewayConfig;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("APIGateway")
@RegisterForReflection
@ApplicationScoped
public class APIGateway extends RouteBuilder {

    @Inject
    GatewayConfig gatewayConfig;

    @Inject
    CamelContext camelContext;

    @Override
    public void configure() throws Exception {

        onException(HttpOperationFailedException.class)
                .process(exchange -> {
                    HttpOperationFailedException e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, e.getStatusCode());
                    exchange.getIn().setBody(e.getResponseBody());
                }).handled(true);

        from("direct:userManagement")
                .routeId("[API Gateway] [DIRECT] [USER-MANAGEMENT-SVC]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/user/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .toD(gatewayConfig.getUserManagementServiceUri() + "/user/${header.*}?bridgeEndpoint=true")
                .otherwise()
                .toD(gatewayConfig.getUserManagementServiceUri() + "/user?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("direct:pitchManagement")
                .routeId("[API Gateway] [DIRECT] [PITCH-MANAGEMENT-SVC]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/pitch/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .toD(gatewayConfig.getPitchManagementServiceUri() + "/pitch/${header.*}?bridgeEndpoint=true")
                .otherwise()
                .toD(gatewayConfig.getPitchManagementServiceUri() + "/pitch?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));


        from("platform-http:/apigw/rest/api/v1/user*")
                .routeId("[API Gateway] [REST] [USER-MANAGEMENT-SVC]")
                .throttle(20)
                .setHeader("User-Agent", constant("Sporton-API-Gateway/0.0.1-SNAPSHOT"))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:userManagement")
                .end()
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));


        from("platform-http:/apigw/rest/api/v1/pitch*")
                .routeId("[API Gateway] [REST] [PITCH-MANAGEMENT-SVC]")
                .throttle(20)
                .setHeader("User-Agent", constant("Sporton-API-Gateway/0.0.1-SNAPSHOT"))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:pitchManagement")
                .end()
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        // Simple Authentication Service call to test the API Gateway (DEV Mode Only)
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
