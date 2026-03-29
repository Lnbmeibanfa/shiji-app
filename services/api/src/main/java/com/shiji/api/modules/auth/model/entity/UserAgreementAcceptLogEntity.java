package com.shiji.api.modules.auth.model.entity;

import com.shiji.api.modules.auth.model.enums.AgreementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_agreement_accept_log")
public class UserAgreementAcceptLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "phone", length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 32)
    private AgreementType agreementType;

    @Column(name = "agreement_version", nullable = false, length = 32)
    private String agreementVersion;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted;

    @Column(name = "accept_time", nullable = false)
    private LocalDateTime acceptTime;

    @Column(name = "request_ip", length = 64)
    private String requestIp;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
