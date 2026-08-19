package org.volodymyrzganiaiko.workload_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.workload_service.dto.ActionType;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
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
    public void submitTraining_success() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(workloadService).process(any());
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
    public void submitTraining_invalidBody() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                -50,
                ActionType.ADD
        );
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/workload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
