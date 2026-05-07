package com.arka.identityaccess.infrastructure.adapter.in.web.response;

public record BlockUserResponse(
        String userId,
        String status,
        boolean changed) {}
