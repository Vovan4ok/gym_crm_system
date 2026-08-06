package org.volodymyrzganiaiko.gym.crm.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.gym.crm.system.dto.AddTrainingRequest;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.handler.GlobalExceptionHandler;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainingControllerTest {
    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TrainingController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void createTraining_success() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest("Tr.Ainee", "Tra.Iner", "Morning session", LocalDate.of(2026, 7, 20), 60);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<AddTrainingRequest> requestCaptor = ArgumentCaptor.forClass(AddTrainingRequest.class);
        verify(gymFacade).createTraining(requestCaptor.capture());

        AddTrainingRequest requestCaptorValue = requestCaptor.getValue();
        assertEquals("Tr.Ainee", requestCaptorValue.traineeUsername());
        assertEquals("Tra.Iner", requestCaptorValue.trainerUsername());
        assertEquals("Morning session", requestCaptorValue.trainingName());
        assertEquals(LocalDate.of(2026, 7, 20), requestCaptorValue.trainingDate());
        assertEquals(60, requestCaptorValue.trainingDuration());
    }

    @Test
    public void createTraining_invalidBody() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest("Tr.Ainee", "Tra.Iner", "Morning session", LocalDate.of(2026, 7, 20), -5);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("trainingDuration")));
        verifyNoInteractions(gymFacade);
    }

    @Test
    public void createTraining_throwsIllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Trainer with username Tra.Iner was not found"))
                .when(gymFacade).createTraining(any(AddTrainingRequest.class));
        AddTrainingRequest request = new AddTrainingRequest("Tr.Ainee", "Tra.Iner", "Morning session", LocalDate.of(2026, 7, 20), 60);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Trainer with username Tra.Iner was not found"));
    }
}
