package org.volodymyrzganiaiko.gateway_service.security.filter;

import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

public class IdentityHeaderFilterTest {
    private final IdentityHeaderFilter filter = new IdentityHeaderFilter();

    @Test
    public void stripsClientSuppliedHeader_whenUnauthenticated() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trainees/John.Doe")
                        .header("X-Auth-User", "forged").build());

        AtomicReference<ServerHttpRequest> forwarded = new AtomicReference<>();
        WebFilterChain chain = ex -> { forwarded.set(ex.getRequest()); return Mono.empty(); };

        filter.filter(exchange, chain).block();

        assertNull(forwarded.get().getHeaders().getFirst("X-Auth-User"));
    }

    @Test
    public void setsHeaderFromJwtSubject_whenAuthenticated() {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject("John.Doe").build();
        ServerWebExchange base = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trainees/John.Doe")
                        .header("X-Auth-User", "forged").build());
        ServerWebExchange exchange = spy(base);
        doReturn(Mono.just(new JwtAuthenticationToken(jwt))).when(exchange).getPrincipal();

        AtomicReference<ServerHttpRequest> forwarded = new AtomicReference<>();
        WebFilterChain chain = ex -> { forwarded.set(ex.getRequest()); return Mono.empty(); };

        filter.filter(exchange, chain).block();

        assertEquals("John.Doe", forwarded.get().getHeaders().getFirst("X-Auth-User"));
    }
}
