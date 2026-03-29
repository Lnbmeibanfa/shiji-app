package com.shiji.api.modules.auth.service.impl;

import com.shiji.api.modules.auth.service.SmsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 开发环境短信占位：验证码见日志。集成测试使用 {@code test} profile 下的替代 Bean，不加载此类。
 */
@Service
@Profile("!test")
public class SmsGatewayStub implements SmsGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsGatewayStub.class);

    @Override
    public void sendCode(String phone, String code) {
        LOGGER.info("SMS stub sent: phone={}, code={}", phone, code);
    }
}
