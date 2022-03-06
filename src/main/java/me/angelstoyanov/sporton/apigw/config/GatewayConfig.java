package me.angelstoyanov.sporton.apigw.config;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import javax.enterprise.context.ApplicationScoped;

@StaticInitSafe
@ConfigMapping(prefix = "sporton.apigw")
@ApplicationScoped
@RegisterForReflection
public interface GatewayConfig {

    @WithName("authentication.svc.url")
    String getAuthenticationServiceUri();

    @WithName("user.management.svc.url")
    String getUserManagementServiceUri();

    @WithName("pitch.management.svc.url")
    String getPitchManagementServiceUri();

    @WithName("session.management.svc.url")
    String getSessionManagementServiceUri();

    @WithName("feedback.management.svc.url")
    String getFeedbackManagementServiceUri();

    @WithName("azure.storage.adapter.svc.url")
    String getAzureStorageAdapterServiceUri();

    @WithName("common.svc.url")
    String getCommonServiceUri();

    @WithName("http.user.agent.name")
    String getHttpUserAgentName();

    @WithName("http.user.agent.version")
    String getHttpUserAgentVersion();

    @WithName("camel.route.http.quota.per-second")
    String getCamelRouteHttpQuotaPerSecond();

    @WithName("authentication.user.id.header.name")
    String getRequestingUserIdHeaderName();

    @WithName("authentication.user.role.header.name")
    String getRequestingUserRoleHeaderName();
}
