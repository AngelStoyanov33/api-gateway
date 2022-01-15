package me.angelstoyanov.sporton.apigw.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import me.angelstoyanov.sporton.apigw.config.GatewayConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.language.bean.Bean;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
        ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();

        try {
            producerTemplate.requestBodyAndHeader("vertx-http:" + gatewayConfig.getAuthenticationServiceUri() + "/authenticate?httpMethod=GET", null, "Authorization", exchange.getMessage().getHeader("Authorization"), String.class);
        }catch (Exception e){
            if(e.getCause() instanceof HttpOperationFailedException){
                int statusCode = ((HttpOperationFailedException) e.getCause()).getStatusCode();
                if(statusCode == 401){
                    exchange.getIn().removeHeader("Authorization");
                }
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
                exchange.getIn().setHeader("statusCode", statusCode);
                return;
            }
        }

    }
}