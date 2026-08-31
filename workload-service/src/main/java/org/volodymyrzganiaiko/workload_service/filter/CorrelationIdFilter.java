package org.volodymyrzganiaiko.workload_service.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            String correlationId = req.getHeader("X-Correlation-Id") == null ? UUID.randomUUID().toString() : req.getHeader("X-Correlation-Id");
            MDC.put("correlationId", correlationId);
            log.info("Incoming request: {} {}", req.getMethod(), req.getRequestURI());
            resp.setHeader("X-Correlation-Id", correlationId);
            chain.doFilter(request, response);
            log.info("Response: {}", resp.getStatus());
        } finally {
            MDC.clear();
        }
    }
}
