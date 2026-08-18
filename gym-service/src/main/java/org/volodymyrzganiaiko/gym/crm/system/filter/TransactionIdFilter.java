package org.volodymyrzganiaiko.gym.crm.system.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionIdFilter implements Filter {
    private static final Logger log =  LoggerFactory.getLogger(TransactionIdFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            MDC.put("transactionId", UUID.randomUUID().toString().substring(0, 8));
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String uri = httpRequest.getRequestURI();
            String query = httpRequest.getQueryString();
            String path = query == null ? uri : uri + "?" + query;
            log.info("Incoming request: {} {}", httpRequest.getMethod(), path);
            long start = System.currentTimeMillis();
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - start;
            log.info("Response: {} in {}ms", httpResponse.getStatus(), duration);
        } finally {
            MDC.clear();
        }
    }
}
