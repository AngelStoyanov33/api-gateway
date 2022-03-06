package me.angelstoyanov.sporton.apigw.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("StorageAdapter")
@ApplicationScoped
@RegisterForReflection
public class StorageAdapter {

    final String incomingStorageUri = "/apigw/rest/api/v1/blob";

    public void doCommentFetch(Exchange exchange){
        String entityId = StringUtils.substringAfter(
                String.valueOf(exchange.getMessage().getHeader("CamelHttpPath")), incomingStorageUri + "/c/");

        setHeaders(exchange, entityId, StorageEntity.IMAGE_COMMENT);

    }

    public void doPitchFetch(Exchange exchange){
        String entityId = StringUtils.substringAfter(
                String.valueOf(exchange.getMessage().getHeader("CamelHttpPath")), incomingStorageUri + "/p/");

        setHeaders(exchange, entityId, StorageEntity.IMAGE_PITCH);
    }

    private void setHeaders(Exchange exchange, String entityId, StorageEntity entityType){
        exchange.getMessage().setHeader("CamelHttpQuery",
                "entityId=" + entityId + "&entityType=" + entityType);
        exchange.getMessage().setHeader("CamelHttpRawQuery",
                "entityId=" + entityId + "&entityType=" + entityType);
        exchange.getMessage().setHeader("entityId", entityId);
        exchange.getMessage().setHeader("entityType", entityType);
    }

    private enum StorageEntity {
        IMAGE_PITCH, IMAGE_COMMENT
    }

}
