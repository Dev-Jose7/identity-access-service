package com.arka.identityaccess.application.mapper.command;

import com.arka.identityaccess.application.command.LoginCommand;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LoginCommandAssembler {

    public EmailAddress toEmailAddress(LoginCommand command) {
        return EmailAddress.of(command.email());
    }

    public ClientDevice toClientDevice(LoginCommand command) {
        String userAgent = normalizeUserAgent(command.userAgent());
        String deviceType = inferDeviceType(userAgent);
        String deviceName = "unknown".equals(userAgent) ? "Unknown Device" : userAgent;
        String deviceIdSource = command.email().trim().toLowerCase(Locale.ROOT)
                + "|"
                + command.ipAddress().trim()
                + "|"
                + userAgent;
        String deviceId = UUID.nameUUIDFromBytes(deviceIdSource.getBytes(StandardCharsets.UTF_8)).toString();
        return ClientDevice.of(deviceId, deviceName, deviceType);
    }

    public ClientIp toClientIp(LoginCommand command) {
        return ClientIp.of(command.ipAddress());
    }

    private String normalizeUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "unknown";
        }
        return userAgent.trim();
    }

    private String inferDeviceType(String userAgent) {
        String normalized = userAgent.toLowerCase(Locale.ROOT);
        if (normalized.contains("mobile") || normalized.contains("android") || normalized.contains("iphone")) {
            return "mobile";
        }
        if (normalized.contains("ipad") || normalized.contains("tablet")) {
            return "tablet";
        }
        if ("unknown".equals(userAgent)) {
            return "unknown";
        }
        return "desktop";
    }
}
