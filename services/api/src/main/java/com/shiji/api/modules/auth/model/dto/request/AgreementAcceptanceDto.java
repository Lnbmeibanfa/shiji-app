package com.shiji.api.modules.auth.model.dto.request;

import com.shiji.api.modules.auth.model.enums.AgreementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgreementAcceptanceDto {

    @NotNull
    private AgreementType agreementType;

    @NotBlank
    private String agreementVersion;

    @NotNull
    private Boolean accepted;
}
