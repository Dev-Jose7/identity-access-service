package io.identityaccess.infrastructure.adapter.in.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = serialize(Map.of(
                "code", "token_expirado_o_revocado",
                "message", exception.getMessage() == null ? "Authentication required" : exception.getMessage()));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    private byte[] serialize(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (JsonProcessingException ex) {
            return "{\"code\":\"token_expirado_o_revocado\",\"message\":\"Authentication required\"}"
                    .getBytes(StandardCharsets.UTF_8);
        }
    }
}
