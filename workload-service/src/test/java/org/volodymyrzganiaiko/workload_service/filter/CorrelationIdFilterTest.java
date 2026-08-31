package org.volodymyrzganiaiko.workload_service.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    public void clear() {
        MDC.clear();
    }

    @Test
    public void readsHeader_putsInMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "abc12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] captured = new String[1];
        FilterChain chain = (req, res) -> captured[0] = MDC.get("correlationId");

        filter.doFilter(request, response, chain);

        assertEquals("abc12345", captured[0]);
    }

    @Test
    public void noHeader_generatesId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] captured = new String[1];
        FilterChain chain = (req, res) -> captured[0] = MDC.get("correlationId");

        filter.doFilter(request, response, chain);

        assertNotNull(captured[0]);
    }

    @Test
    public void clearsMdcAfter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "abc12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(MDC.get("correlationId"));
    }

    @Test
    public void doFilter_getsHeaderCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-Id", "my-test-123");
        String[] captured = new String[1];

        FilterChain chain = (req, res) -> captured[0] = MDC.get("correlationId");

        filter.doFilter(request, response, chain);

        assertEquals("my-test-123", captured[0]);
    }

    @Test
    public void doFilter_givesTheSameCorrelationIdHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-Id", "my-test-123");
        String[] captured = new String[1];

        FilterChain chain = (req, res) -> captured[0] = MDC.get("correlationId");

        filter.doFilter(request, response, chain);

        assertEquals(captured[0], response.getHeader("X-Correlation-ID"));
    }
}
