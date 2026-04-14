package com.arka.identityaccess.domain.session.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public record ClientDevice(String deviceId, String deviceName, String deviceType) {

    public ClientDevice {
        if (deviceId == null || deviceId.isBlank()) {
            throw new DomainInvariantViolationException("deviceId is required");
        }
        if (deviceName == null || deviceName.isBlank()) {
            throw new DomainInvariantViolationException("deviceName is required");
        }
        if (deviceType == null || deviceType.isBlank()) {
            throw new DomainInvariantViolationException("deviceType is required");
        }
        deviceId = deviceId.trim();
        deviceName = deviceName.trim();
        deviceType = deviceType.trim();
    }

    public static ClientDevice of(String deviceId, String deviceName, String deviceType) {
        return new ClientDevice(deviceId, deviceName, deviceType);
    }
}
