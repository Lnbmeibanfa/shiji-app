package com.shiji.api.modules.auth.model.jpa;

import com.shiji.api.modules.auth.model.enums.SessionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 与遗留库 {@code user_session.session_status}（tinyint）对齐。
 */
@Converter(autoApply = false)
public class SessionStatusConverter implements AttributeConverter<SessionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SessionStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return switch (attribute) {
            case ACTIVE -> 1;
            case INVALIDATED -> 2;
            case LOGGED_OUT -> 3;
            case EXPIRED -> 4;
        };
    }

    @Override
    public SessionStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return switch (dbData) {
            case 1 -> SessionStatus.ACTIVE;
            case 2 -> SessionStatus.INVALIDATED;
            case 3 -> SessionStatus.LOGGED_OUT;
            case 4 -> SessionStatus.EXPIRED;
            default -> SessionStatus.ACTIVE;
        };
    }
}
