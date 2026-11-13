package com.projectmanagement.task.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.internal")
public class InternalTokenProperties {

    private String token;
}