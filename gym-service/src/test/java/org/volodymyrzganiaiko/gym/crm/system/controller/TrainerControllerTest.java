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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.handler.GlobalExceptionHandler;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainerControllerTest {
    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TrainerController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    public void getProfile_success() throws Exception {
        TrainerProfileResponse response = new TrainerProfileResponse(
                "Tra.Iner", "John", "Doe",
                new TrainingTypeResponse(2L, "Cardio"), true, List.of());

        when(gymFacade.getTrainerProfile(any())).thenReturn(response);

        mockMvc.perform(get("/api/trainers/{username}", "Tra.Iner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Tra.Iner"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.specialization.name").value("Cardio"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(gymFacade).getTrainerProfile(eq("Tra.Iner"));
    }

    @Test
    public void getProfile_notFound() throws Exception {
        when(gymFacade.getTrainerProfile(any()))
                .thenThrow(new IllegalArgumentException("Trainer with the username Tra.Iner was not found"));

        mockMvc.perform(get("/api/trainers/{username}", "Tra.Iner"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void createTrainer_success() throws Exception {
        when(gymFacade.createTrainer(any())).thenReturn(new TrainerRegistrationDTO("John.Doe", "rawPass"));

        TrainerRegistrationRequest request = new TrainerRegistrationRequest("John", "Doe", 2L);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Doe"))
                .andExpect(jsonPath("$.password").value("rawPass"));

        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(gymFacade).createTrainer(trainerCaptor.capture());

        Trainer captured = trainerCaptor.getValue();
        assertEquals("John", captured.getFirstName());
        assertEquals(2L, captured.getSpecialization().getId());
    }

    @Test
    public void changeStatus_conflict() throws Exception {
        doThrow(new IllegalStateException("Trainer is already active"))
                .when(gymFacade).changeTrainerStatus(any(), any());

        UpdateStatusRequest request = new UpdateStatusRequest(true);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/trainers/{username}/status", "Tra.Iner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Trainer is already active"));
    }

    @Test
    public void updateProfile_success() throws Exception {
        TrainerProfileResponse response = new TrainerProfileResponse("Tra.Iner", "Jane", "Roe",
                new TrainingTypeResponse(2L, "Cardio"), false, List.of());

        when(gymFacade.updateTrainerProfile(any(), any(Trainer.class))).thenReturn(response);

        UpdateTrainerRequest request = new UpdateTrainerRequest("Jane", "Roe", false);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/trainers/{username}", "Tra.Iner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Roe"))
                .andExpect(jsonPath("$.isActive").value(false));

        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(gymFacade).updateTrainerProfile(eq("Tra.Iner"), trainerCaptor.capture());

        assertEquals("Jane", trainerCaptor.getValue().getFirstName());
        assertNull(trainerCaptor.getValue().getSpecialization());
    }

    @Test
    public void getTrainings_success() throws Exception {
        TrainerTrainingResponse training = new TrainerTrainingResponse("Morning session",
                LocalDate.of(2026, 7, 20), new TrainingTypeResponse(2L, "Cardio"), 60, "Tr.Ainee");
        when(gymFacade.getTrainerTrainings(anyString(),
                any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainers/{username}/trainings", "Tra.Iner")
                .param("periodFrom", "2026-07-01")
                .param("periodTo", "2026-07-31")
                .param("traineeName", "Tr.Ainee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingName").value("Morning session"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-07-20"))
                .andExpect(jsonPath("$[0].trainingType.name").value("Cardio"))
                .andExpect(jsonPath("$[0].traineeName").value("Tr.Ainee"));

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(gymFacade).getTrainerTrainings(eq("Tra.Iner"),
                fromCaptor.capture(), any(LocalDate.class), eq("Tr.Ainee"));

        assertEquals(LocalDate.of(2026, 7, 1), fromCaptor.getValue());
    }
}
