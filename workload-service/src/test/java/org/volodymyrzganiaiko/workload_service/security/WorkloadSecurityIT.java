package org.volodymyrzganiaiko.workload_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.volodymyrzganiaiko.workload_service.AbstractMongoIT;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WorkloadSecurityIT extends AbstractMongoIT {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkloadService workloadService;

    @Test
    void validAuthHeader_ok() throws Exception {
        when(workloadService.getWorkload("x"))
                .thenReturn(new TrainerSummaryResponse("x", "Tra", "Iner", true, List.of()));

        mockMvc.perform(get("/api/workload/x")
                        .header("X-Auth-User", "x"))
                .andExpect(status().isOk());
        verify(workloadService).getWorkload("x");
    }

    @Test
    void noAuthHeader_401() throws Exception {
        mockMvc.perform(get("/api/workload/x"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }

    @Test
    public void blankAuthHeader_401() throws Exception {
        mockMvc.perform(get("/api/workload/x")
                        .header("X-Auth-User", ""))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }
}