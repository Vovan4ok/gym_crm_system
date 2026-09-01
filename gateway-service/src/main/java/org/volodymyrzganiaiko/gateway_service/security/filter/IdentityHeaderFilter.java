package org.volodymyrzganiaiko.gateway_service.security.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class IdentityHeaderFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest stripped = exchange.getRequest().mutate()
                .headers(h -> h.remove("X-Auth-User")).build();     // зрізали клієнтський

        return exchange.getPrincipal()
                .filter(p -> p instanceof JwtAuthenticationToken)
                .map(p -> ((JwtAuthenticationToken) p).getToken().getSubject())
                .map(sub -> exchange.mutate().request(
                        stripped.mutate().header("X-Auth-User", sub).build()).build())
                .defaultIfEmpty(exchange.mutate().request(stripped).build())
                .flatMap(chain::filter);
    }
}
