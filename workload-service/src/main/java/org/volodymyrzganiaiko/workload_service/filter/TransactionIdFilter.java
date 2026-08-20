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
public class TransactionIdFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(TransactionIdFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            String txId = req.getHeader("X-Transaction-Id");
            if (txId == null) {
                txId = UUID.randomUUID().toString().substring(0, 8);
            }
            MDC.put("transactionId", txId);
            log.info("Incoming request: {} {}", req.getMethod(), req.getRequestURI());
            chain.doFilter(request, response);
            log.info("Response: {}", resp.getStatus());
        } finally {
            MDC.clear();
        }
    }
}
