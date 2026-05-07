package com.arka.identityaccess.application.command;

import com.arka.identityaccess.domain.model.user.RegistrationMode;

public record RegisterCommand(
        String email,
        String rawPassword,
        RegistrationMode registrationMode,
        String requestedRoleCode,
        String actorId,
        String userAgent,
        String ipAddress) {}
