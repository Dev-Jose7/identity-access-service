package com.arka.identityaccess.application.result;

public record BlockUserResult(
        String userId,
        String status,
        boolean changed) {}
