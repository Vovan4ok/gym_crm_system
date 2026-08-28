package org.volodymyrzganiaiko.workload_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class WorkloadControllerTest {
    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadController workloadController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workloadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    public void getWorkload_success() throws Exception {
        when(workloadService.getWorkload("x")).thenReturn(new TrainerSummaryResponse(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                List.of()
        ));

        mockMvc.perform(get("/api/workload/x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Tra.Iner"))
                .andExpect(jsonPath("$.firstName").value("Tra"))
                .andExpect(jsonPath("$.lastName").value("Iner"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    public void getWorkload_notFound() throws Exception {
        when(workloadService.getWorkload("Ghost")).thenThrow(new NoSuchElementException("Not found"));

        mockMvc.perform(get("/api/workload/Ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getWorkload_unexpectedError_returns500() throws Exception {
        when(workloadService.getWorkload("x")).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/api/workload/x"))
                .andExpect(status().isInternalServerError());
    }
}
