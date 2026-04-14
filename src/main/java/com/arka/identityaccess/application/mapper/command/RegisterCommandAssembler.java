package com.arka.identityaccess.application.mapper.command;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommandAssembler {
    public EmailAddress toEmailAddress(RegisterCommand command) {
        return EmailAddress.of(command.email());
    }
}
