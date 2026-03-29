package com.shiji.api.modules.auth.service;

public interface SmsGateway {

    void sendCode(String phone, String code);
}
