package com.arka.identityaccess.application.mapper.result;

import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import org.springframework.stereotype.Component;

@Component
public class RegisterResultMapper {
    public RegisterResult toResult(AccountAggregate account) {
        return new RegisterResult(
                account.id().value(),
                account.email().value(),
                account.status().name());
    }
}
