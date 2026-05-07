package com.arka.identityaccess.domain.model.session.valueobject;

public record ClientDevice(String deviceId, String deviceName, String deviceType) {

    public ClientDevice {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId is required");
        }
        if (deviceName == null || deviceName.isBlank()) {
            throw new IllegalArgumentException("deviceName is required");
        }
        if (deviceType == null || deviceType.isBlank()) {
            throw new IllegalArgumentException("deviceType is required");
        }
        deviceId = deviceId.trim();
        deviceName = deviceName.trim();
        deviceType = deviceType.trim();
    }

    public static ClientDevice of(String deviceId, String deviceName, String deviceType) {
        return new ClientDevice(deviceId, deviceName, deviceType);
    }
}
