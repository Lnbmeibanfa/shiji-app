package com.shiji.api.modules.auth.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsCodeLoginRequest {

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^\\d{4,8}$", message = "验证码格式不正确")
    private String code;

    private String deviceId;

    @Valid
    @NotEmpty
    private List<AgreementAcceptanceDto> agreements;
}
