package io.identityaccess.application.usecase.query;

import io.identityaccess.application.port.in.ListAccountsQueryUseCase;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.AccountSummaryResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListAccountsUseCase implements ListAccountsQueryUseCase {

    private final UserPersistencePort userPersistencePort;

    public ListAccountsUseCase(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Flux<AccountSummaryResult> handle() {
        return userPersistencePort.listAccounts()
                .map(account -> new AccountSummaryResult(
                        account.userId(),
                        account.email(),
                        account.status(),
                        account.failedLoginCount(),
                        account.createdAt(),
                        account.updatedAt(),
                        account.roles()));
    }
}
