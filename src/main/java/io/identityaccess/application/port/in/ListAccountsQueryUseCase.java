package io.identityaccess.application.port.in;

import io.identityaccess.application.result.AccountSummaryResult;
import reactor.core.publisher.Flux;

public interface ListAccountsQueryUseCase {

    Flux<AccountSummaryResult> handle();
}
