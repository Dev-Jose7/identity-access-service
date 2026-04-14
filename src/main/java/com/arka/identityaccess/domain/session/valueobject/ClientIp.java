package com.arka.identityaccess.domain.session.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.regex.Pattern;

public record ClientIp(String value) {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");

    private static final Pattern IPV6_HEX_PATTERN = Pattern.compile("^[0-9A-Fa-f:]+$");

    public ClientIp {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("ipAddress is required");
        }
        value = value.trim();
        if (!isValidIpv4(value) && !isValidIpv6(value)) {
            throw new DomainInvariantViolationException("ipAddress must be a valid IPv4 or IPv6 literal");
        }
    }

    public static ClientIp of(String value) {
        return new ClientIp(value);
    }

    private static boolean isValidIpv4(String value) {
        return IPV4_PATTERN.matcher(value).matches();
    }

    private static boolean isValidIpv6(String value) {
        if (value.length() > 39 || value.chars().filter(ch -> ch == ':').count() < 2) {
            return false;
        }
        if (!IPV6_HEX_PATTERN.matcher(value).matches()) {
            return false;
        }
        if (value.indexOf("::") != value.lastIndexOf("::")) {
            return false;
        }

        String[] groups = value.split(":", -1);
        boolean compressed = value.contains("::");
        int nonEmptyGroups = 0;
        for (String group : groups) {
            if (group.isEmpty()) {
                continue;
            }
            if (group.length() > 4) {
                return false;
            }
            nonEmptyGroups++;
        }

        if (compressed) {
            return nonEmptyGroups < 8;
        }
        return nonEmptyGroups == 8;
    }
}
