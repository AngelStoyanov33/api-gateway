package me.angelstoyanov.sporton.apigw.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Exchange;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;

@Named("StorageAdapter")
@ApplicationScoped
@RegisterForReflection
public class StorageAdapter {

    final String incomingStorageUri = "/apigw/rest/api/v1/blob";

    public void doCommentFetch(Exchange exchange) {
        String entityId = StringUtils.substringAfter(
                String.valueOf(exchange.getMessage().getHeader("CamelHttpPath")), incomingStorageUri + "/c/");

        setHeaders(exchange, entityId, StorageEntity.IMAGE_COMMENT);
    }

    public void doPitchFetch(Exchange exchange) {
        String entityId = StringUtils.substringAfter(
                String.valueOf(exchange.getMessage().getHeader("CamelHttpPath")), incomingStorageUri + "/p/");

        setHeaders(exchange, entityId, StorageEntity.IMAGE_PITCH);
    }

    public void prepareMultipartBody(Exchange exchange) throws IOException {

        String userId = (String) exchange.getIn().getHeader("user_id");
        String pitchId = (String) exchange.getIn().getHeader("pitch_id");
        String content = (String) exchange.getIn().getHeader("content");

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (exchange.getIn().getBody() != null) {
            byte[] attachmentBytes = exchange.getIn().getBody(byte[].class);
            File tempFile = File.createTempFile("tmp", "tmp", null);
            FileUtils.writeByteArrayToFile(tempFile, attachmentBytes);
            if (tempFile != null) {
                multipartEntityBuilder.addPart("attachment", new FileBody(tempFile));
            }
        }
        multipartEntityBuilder.addPart("user_id", new StringBody(userId, ContentType.TEXT_PLAIN));
        multipartEntityBuilder.addPart("pitch_id", new StringBody(pitchId, ContentType.TEXT_PLAIN));
        multipartEntityBuilder.addPart("content", new StringBody(content, ContentType.TEXT_PLAIN));
        exchange.getIn().setBody(multipartEntityBuilder.build());
    }

    private void setHeaders(Exchange exchange, String entityId, StorageEntity entityType) {
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
