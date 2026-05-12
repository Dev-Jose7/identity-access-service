package io.identityaccess.application.mapper.command;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommandAssembler {
    public EmailAddress toEmailAddress(RegisterCommand command) {
        return EmailAddress.of(command.email());
    }
}
