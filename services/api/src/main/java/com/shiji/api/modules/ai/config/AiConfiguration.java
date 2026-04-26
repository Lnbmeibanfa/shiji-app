package com.shiji.api.modules.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({DashScopeProperties.class, RecognitionTaskProperties.class})
public class AiConfiguration {}
