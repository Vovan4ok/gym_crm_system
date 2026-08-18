package org.volodymyrzganiaiko.gym.crm.system.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionIdFilterTest {
    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Mock
    private FilterChain chain;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    public void setup() {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainees");
        response = new MockHttpServletResponse();
    }

    @AfterEach
    public void teardown() {
        MDC.clear();
    }

    @Test
    public void doFilter_passesRequestDownTheChain() throws Exception {
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void doFilter_setsTransactionIdDuringChain() throws Exception {
        String[] captured = new String[1];
        doAnswer(invocation -> {
            captured[0] = MDC.get("transactionId");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        assertNotNull(captured[0]);
        assertEquals(8, captured[0].length());
    }

    @Test
    public void doFilter_clearsMdcAfterCompletion() throws Exception {
        filter.doFilter(request, response, chain);

        assertNull(MDC.get("transactionId"));
    }

    @Test
    public void doFilter_clearsMdcWhenChainThrows() throws Exception {
        doThrow(new ServletException("boom")).when(chain).doFilter(request, response);

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
        assertNull(MDC.get("transactionId"));
    }

    @Test
    public void doFilter_appendsQueryString()  throws Exception {
        request.setQueryString("firstName=John");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
