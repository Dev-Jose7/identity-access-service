package io.identityaccess.application.command;

public record RefreshSessionCommand(String refreshToken, String ipAddress) {

    public RefreshSessionCommand(String refreshToken) {
        this(refreshToken, "127.0.0.1");
    }
}
