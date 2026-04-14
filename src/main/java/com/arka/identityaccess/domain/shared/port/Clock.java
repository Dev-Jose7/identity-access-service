package com.arka.identityaccess.domain.shared.port;

import java.time.Instant;

public interface Clock {

    Instant now();
}
