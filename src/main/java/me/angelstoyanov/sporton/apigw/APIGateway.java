package me.angelstoyanov.sporton.apigw;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.configuration.ProfileManager;
import me.angelstoyanov.sporton.apigw.bean.Authenticator;
import me.angelstoyanov.sporton.apigw.bean.StorageAdapter;
import me.angelstoyanov.sporton.apigw.bean.TransitAppender;
import me.angelstoyanov.sporton.apigw.config.GatewayConfig;
import me.angelstoyanov.sporton.apigw.exception.ServiceDownException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.http.HttpStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("APIGateway")
@RegisterForReflection(targets = {HttpOperationFailedException.class, APIGateway.class, HttpStatus.class}, serialization = true)
@ApplicationScoped
public class APIGateway extends RouteBuilder {

    @Inject
    GatewayConfig gatewayConfig;

    @Inject
    CamelContext camelContext;

    @Override
    public void configure() throws Exception {

        this.getCamelContext().setManagementName("sporton-apigw");
        final String userAgent = gatewayConfig.getHttpUserAgentName() + "/" + gatewayConfig.getHttpUserAgentVersion();
        final int throttle = Integer.parseInt(gatewayConfig.getCamelRouteHttpQuotaPerSecond());

        onException(HttpOperationFailedException.class)
                .process(exchange -> {
                    HttpOperationFailedException e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    int errorCode = e.getStatusCode();
                    if (errorCode != HttpStatus.SC_FORBIDDEN && errorCode != HttpStatus.SC_UNAUTHORIZED) {
                        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, errorCode);
                        exchange.getIn().setBody(e.getResponseBody());
                    }
                }).handled(true);

        onException(ServiceDownException.class)
                .process(exchange -> {
                    ServiceDownException e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ServiceDownException.class);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.SC_GATEWAY_TIMEOUT);
                    exchange.getIn().setBody(e.getMessage());
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

        from("direct:sessionManagement")
                .routeId("[API Gateway] [DIRECT] [SESSION-MANAGEMENT-SVC]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/session/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .toD(gatewayConfig.getSessionManagementServiceUri() + "/session/${header.*}?bridgeEndpoint=true")
                .otherwise()
                .toD(gatewayConfig.getSessionManagementServiceUri() + "/session?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("direct:commonService")
                .routeId("[API Gateway] [DIRECT] [COMMON-SVC]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/admin/common/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .toD(gatewayConfig.getCommonServiceUri() + "/common/${header.*}?bridgeEndpoint=true")
                .otherwise()
                .toD(gatewayConfig.getCommonServiceUri() + "/common?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("direct:commentManagement")
                .routeId("[API Gateway] [DIRECT] [FEEDBACK-MANAGEMENT-SVC][COMMENT]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/comment/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .toD(gatewayConfig.getFeedbackManagementServiceUri() + "/comment/${header.*}?bridgeEndpoint=true")
                .otherwise()
                .toD(gatewayConfig.getFeedbackManagementServiceUri() + "/comment?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("direct:ratingManagement")
                .routeId("[API Gateway] [DIRECT] [FEEDBACK-MANAGEMENT-SVC][RATING]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/rating/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .toD(gatewayConfig.getFeedbackManagementServiceUri() + "/rating/${header.*}?bridgeEndpoint=true")
                .otherwise()
                .toD(gatewayConfig.getFeedbackManagementServiceUri() + "/rating?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("direct:storageCommentManagement")
                .routeId("[API Gateway] [DIRECT] [AZURE-STORAGE-ADAPTER-SVC][C]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/blob/c/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .bean(StorageAdapter.class, "doCommentFetch")
                .toD(gatewayConfig.getAzureStorageAdapterServiceUri() + "/fetch?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .otherwise()
                .process(exchange -> {
                    exchange.getIn().setBody(null);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("direct:storagePitchManagement")
                .routeId("[API Gateway] [DIRECT] [AZURE-STORAGE-ADAPTER-SVC][P]")
                .choice()
                .when(simple("${header.CamelHttpUri} regex '^/apigw/rest/api/v1/blob/p/(.*)$'"))
                .removeHeaders("CamelHttpU*").removeHeaders("Host*")
                .bean(StorageAdapter.class, "doPitchFetch")
                .toD(gatewayConfig.getAzureStorageAdapterServiceUri() + "/fetch?${header.CamelHttpRawQuery}&bridgeEndpoint=true")
                .otherwise()
                .process(exchange -> {
                    exchange.getIn().setBody(null);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));


        from("platform-http:/apigw/rest/api/v1/user*")
                .routeId("[API Gateway] [REST] [USER-MANAGEMENT-SVC]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:userManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));


        from("platform-http:/apigw/rest/api/v1/pitch*")
                .routeId("[API Gateway] [REST] [PITCH-MANAGEMENT-SVC]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:pitchManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("platform-http:/apigw/rest/api/v1/session*")
                .routeId("[API Gateway] [REST] [SESSION-MANAGEMENT-SVC]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:sessionManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("platform-http:/apigw/rest/api/v1/comment*")
                .routeId("[API Gateway] [REST] [FEEDBACK-MANAGEMENT-SVC][COMMENT]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .convertBodyTo(byte[].class)
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:commentManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("platform-http:/apigw/rest/api/v1/rating*")
                .routeId("[API Gateway] [REST] [FEEDBACK-MANAGEMENT-SVC][RATING]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:ratingManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("platform-http:/apigw/rest/api/v1/blob/c*")
                .routeId("[API Gateway] [REST] [AZURE-STORAGE-ADAPTER-SVC][C]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:storageCommentManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("platform-http:/apigw/rest/api/v1/blob/p*")
                .routeId("[API Gateway] [REST] [AZURE-STORAGE-ADAPTER-SVC][P]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401"))
                .to("direct:storagePitchManagement")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        from("platform-http:/apigw/rest/api/v1/admin/common*")
                .routeId("[API Gateway] [REST] [COMMON-SVC]")
                .throttle(throttle)
                .setHeader("User-Agent", constant(userAgent))
                .bean(TransitAppender.class, "appendCorrelationID")
                .bean(Authenticator.class, "authenticate")
                .choice()
                .when(simple("${header.CamelHttpResponseCode} != 401" +
                        " && ${header.X-Requesting-User-Role} =~ 'ADMIN'"))
                .to("direct:commonService")
                .end()
                .bean(Authenticator.class, "cleanInnerAuthorization")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));

        // Simple Authentication Service call to test the API Gateway (DEV Mode Only)
        if (ProfileManager.getActiveProfile().equalsIgnoreCase("dev")) {
            from("platform-http:/apigw/authenticate")
                    .routeId("[API Gateway] [Authentication Service]")
                    .setHeader("User-Agent", constant(userAgent))
                    .bean(TransitAppender.class, "appendCorrelationID")
                    .bean(Authenticator.class, "authenticate")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, header("statusCode"));
        }

    }
}
