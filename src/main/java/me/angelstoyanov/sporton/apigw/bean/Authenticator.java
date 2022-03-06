package me.angelstoyanov.sporton.apigw.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import me.angelstoyanov.sporton.apigw.config.GatewayConfig;
import me.angelstoyanov.sporton.apigw.exception.ServiceDownException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.http.HttpStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("Authenticator")
@RegisterForReflection
@ApplicationScoped
public class Authenticator {

    @Inject
    protected GatewayConfig gatewayConfig;

    public void authenticate(Exchange exchange) {

        try (ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate()) {
            Exchange authenticationExchange = producerTemplate.send("vertx-http:" + gatewayConfig.getAuthenticationServiceUri() + "/authenticate?httpMethod=GET", innerExchange -> {
                innerExchange.getIn().setHeader("Authorization", exchange.getMessage().getHeader("Authorization"));
                innerExchange.getIn().setBody(null);
            });
            if (authenticationExchange.getException() != null) {
                throw authenticationExchange.getException();
            }
            exchange.getMessage().setHeader(gatewayConfig.getRequestingUserIdHeaderName(),
                    authenticationExchange.getMessage().getHeader(gatewayConfig.getRequestingUserIdHeaderName()));
            exchange.getMessage().setHeader(gatewayConfig.getRequestingUserRoleHeaderName(),
                    authenticationExchange.getMessage().getHeader(gatewayConfig.getRequestingUserRoleHeaderName()));
        } catch (Exception e) {
            if (e instanceof HttpOperationFailedException) {
                int statusCode = ((HttpOperationFailedException) e).getStatusCode();
                if (statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN) {
                    exchange.getIn().removeHeader("Authorization");
                }
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
                exchange.getIn().setBody("");
                exchange.getIn().setHeader("statusCode", statusCode);
                return;
            } else {
                throw new ServiceDownException("We are experiencing difficulties right now, please try again later.");
            }
        }
    }

    public void cleanInnerAuthorization(Exchange exchange) {
        exchange.getIn().removeHeader("Authorization");
        exchange.getIn().removeHeader(gatewayConfig.getRequestingUserRoleHeaderName());
        exchange.getIn().removeHeader(gatewayConfig.getRequestingUserIdHeaderName());
    }
}
