package io.identityaccess.infrastructure.adapter.out.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class JwtRsaKeyProvider {

    private final String activeKeyId;
    private final RSAPrivateKey activePrivateKey;
    private final RSAPublicKey activePublicKey;
    private final Map<String, RSAPublicKey> verificationPublicKeysByKid;

    public JwtRsaKeyProvider(
            @Value("${app.security.jwt.algorithm:RS256}") String algorithm,
            @Value("${app.security.jwt.key-id:dev-rsa-key-1}") String keyId,
            @Value("${app.security.jwt.private-key-path:classpath:keys/dev-private.pem}") String privateKeyPath,
            @Value("${app.security.jwt.public-key-path:classpath:keys/dev-public.pem}") String publicKeyPath,
            @Value("${app.security.jwt.additional-public-keys:}") String additionalPublicKeys,
            ResourceLoader resourceLoader) {
        if (!"RS256".equalsIgnoreCase(algorithm)) {
            throw new IllegalStateException("Only RS256 is supported for JWT signing");
        }
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalStateException("JWT key id is required");
        }
        this.activeKeyId = keyId.trim();
        this.activePrivateKey = loadPrivateKey(privateKeyPath, resourceLoader);
        this.activePublicKey = loadPublicKey(publicKeyPath, resourceLoader);
        this.verificationPublicKeysByKid =
                loadVerificationKeySet(this.activeKeyId, this.activePublicKey, additionalPublicKeys, resourceLoader);
    }

    public String keyId() {
        return activeKeyId;
    }

    public RSAPrivateKey privateKey() {
        return activePrivateKey;
    }

    public RSAPublicKey publicKey() {
        return activePublicKey;
    }

    public RSAKey publicJwk() {
        return toJwk(activeKeyId, activePublicKey);
    }

    public List<JWK> publicJwks() {
        return verificationPublicKeysByKid.entrySet().stream()
                .map(entry -> (JWK) toJwk(entry.getKey(), entry.getValue()))
                .toList();
    }

    public Optional<RSAPublicKey> findVerificationPublicKey(String kid) {
        if (kid == null || kid.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(verificationPublicKeysByKid.get(kid.trim()));
    }

    private RSAPrivateKey loadPrivateKey(String location, ResourceLoader resourceLoader) {
        try {
            byte[] keyBytes = readPem(location, resourceLoader);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load RSA private key from " + location, exception);
        }
    }

    private RSAPublicKey loadPublicKey(String location, ResourceLoader resourceLoader) {
        try {
            byte[] keyBytes = readPem(location, resourceLoader);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load RSA public key from " + location, exception);
        }
    }

    private byte[] readPem(String location, ResourceLoader resourceLoader) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("JWT key resource does not exist: " + location);
            }
            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String base64 = pem
                    .replaceAll("-----BEGIN [A-Z ]+-----", "")
                    .replaceAll("-----END [A-Z ]+-----", "")
                    .replaceAll("\\s", "");
            return Base64.getDecoder().decode(base64);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read JWT key resource: " + location, exception);
        }
    }

    private Map<String, RSAPublicKey> loadVerificationKeySet(
            String primaryKid,
            RSAPublicKey primaryPublicKey,
            String additionalPublicKeys,
            ResourceLoader resourceLoader) {
        LinkedHashMap<String, RSAPublicKey> keysByKid = new LinkedHashMap<>();
        keysByKid.put(primaryKid, primaryPublicKey);

        if (additionalPublicKeys == null || additionalPublicKeys.isBlank()) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(keysByKid));
        }

        String[] entries = additionalPublicKeys.split(",");
        for (String rawEntry : entries) {
            String entry = rawEntry == null ? "" : rawEntry.trim();
            if (entry.isBlank()) {
                continue;
            }

            int separator = entry.indexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                throw new IllegalStateException("Invalid additional public key format. Expected kid=path");
            }

            String kid = entry.substring(0, separator).trim();
            String path = entry.substring(separator + 1).trim();
            if (kid.isBlank() || path.isBlank()) {
                throw new IllegalStateException("Invalid additional public key entry. kid and path are required");
            }
            if (keysByKid.containsKey(kid)) {
                throw new IllegalStateException("Duplicated JWT key id in verification set: " + kid);
            }
            keysByKid.put(kid, loadPublicKey(path, resourceLoader));
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(keysByKid));
    }

    private RSAKey toJwk(String kid, RSAPublicKey publicKey) {
        return new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(kid)
                .build();
    }
}
