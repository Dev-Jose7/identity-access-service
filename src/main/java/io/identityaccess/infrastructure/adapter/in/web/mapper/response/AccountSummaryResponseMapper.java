package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.AccountSummaryResult;
import io.identityaccess.infrastructure.adapter.in.web.response.AccountSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountSummaryResponseMapper {

    public AccountSummaryResponse toResponse(AccountSummaryResult result) {
        return new AccountSummaryResponse(
                result.userId(),
                result.email(),
                result.status(),
                result.failedLoginCount(),
                result.createdAt(),
                result.updatedAt(),
                result.roles());
    }
}
