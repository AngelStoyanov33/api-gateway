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
}
