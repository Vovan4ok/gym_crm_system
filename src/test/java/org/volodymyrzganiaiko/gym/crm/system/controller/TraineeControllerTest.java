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
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.gym.crm.system.service.JwtService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TraineeControllerTest {
    @Mock
    private GymFacade gymFacade;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TraineeController controller;

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
    public void createTrainee_success() throws Exception {
        when(gymFacade.createTrainee(any())).thenReturn(new TraineeRegistrationDTO("John.Doe", "rawPass"));
        when(jwtService.generateToken("John.Doe")).thenReturn("test.jwt.token");

        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(1990, 5, 15), "Main st. 1");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Doe"))
                .andExpect(jsonPath("$.password").value("rawPass"))
                .andExpect(jsonPath("$.token").value("test.jwt.token"));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(gymFacade).createTrainee(captor.capture());
        assertEquals("John", captor.getValue().getFirstName());
        assertEquals(LocalDate.of(1990, 5, 15), captor.getValue().getDateOfBirth());
    }

    @Test
    public void getProfile_success() throws Exception {
        TraineeProfileResponse response = new TraineeProfileResponse(
                "Tr.Ainee", "John", "Doe", LocalDate.of(1990, 5, 15), "Main st. 1", true,
                List.of(new TrainerSummaryResponse("Tra.Iner", "Jane", "Roe",
                        new TrainingTypeResponse(2L, "Cardio"))));
        when(gymFacade.getTraineeProfile(any())).thenReturn(response);

        mockMvc.perform(get("/api/trainees/{username}", "Tr.Ainee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Tr.Ainee"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-05-15"))
                .andExpect(jsonPath("$.trainers", hasSize(1)))
                .andExpect(jsonPath("$.trainers[0].username").value("Tra.Iner"));

        verify(gymFacade).getTraineeProfile("Tr.Ainee");
    }

    @Test
    public void deleteTrainee_success() throws Exception {
        mockMvc.perform(delete("/api/trainees/{username}", "Tr.Ainee"))
                .andExpect(status().isOk());

        verify(gymFacade).deleteTraineeProfile("Tr.Ainee");
    }

    @Test
    public void updateProfile_success() throws Exception {
        when(gymFacade.updateTraineeProfile(any(), any(Trainee.class)))
                .thenReturn(new TraineeProfileResponse("Tr.Ainee", "Jane", "Roe",
                        LocalDate.of(1990, 5, 15), "New st. 2", false, List.of()));

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "Jane", "Roe", false, LocalDate.of(1990, 5, 15), "New st. 2");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/trainees/{username}", "Tr.Ainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.address").value("New st. 2"))
                .andExpect(jsonPath("$.isActive").value(false));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(gymFacade).updateTraineeProfile(eq("Tr.Ainee"), captor.capture());
        assertEquals("Jane", captor.getValue().getFirstName());
        assertEquals(false, captor.getValue().getIsActive());
    }

    @Test
    public void getUnassignedTrainer_success() throws Exception {
        when(gymFacade.getUnassignedTrainers(anyString()))
                .thenReturn(List.of(new TrainerSummaryResponse("Tra.Iner", "Jane", "Roe",
                        new TrainingTypeResponse(2L, "Cardio"))));

        mockMvc.perform(get("/api/trainees/{username}/unassigned-trainers", "Tr.Ainee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("Tra.Iner"))
                .andExpect(jsonPath("$[0].specialization.name").value("Cardio"));
    }

    @Test
    public void updateTrainers_success() throws Exception {
        when(gymFacade.updateTrainers(any(), any()))
                .thenReturn(List.of(new TrainerSummaryResponse("Tra.Iner", "Jane", "Roe",
                        new TrainingTypeResponse(2L, "Cardio"))));
        UpdateTrainerListRequest request = new UpdateTrainerListRequest(List.of("Tra.Iner", "Other.Trainer"));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/trainees/{username}/trainers", "Tr.Ainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("Tra.Iner"));

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(gymFacade).updateTrainers(eq("Tr.Ainee"), captor.capture());
        assertEquals(List.of("Tra.Iner", "Other.Trainer"), captor.getValue());
    }

    @Test
    public void changeStatus_conflict() throws Exception {
        doThrow(new IllegalStateException("Trainee is already active"))
                .when(gymFacade).changeTraineeStatus(any(), any());

        UpdateStatusRequest request = new UpdateStatusRequest(true);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/trainees/{username}/status", "Tr.Ainee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Trainee is already active"));
    }

    @Test
    public void getTrainings_success() throws Exception {
        when(gymFacade.getTraineeTrainings(anyString(),
                any(LocalDate.class), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(List.of(new TraineeTrainingResponse("Morning session",
                        LocalDate.of(2026, 7, 20), new TrainingTypeResponse(2L, "Cardio"), 60, "Tra.Iner")));

        mockMvc.perform(get("/api/trainees/{username}/trainings", "Tr.Ainee")
                .param("periodFrom", "2026-07-01")
                .param("periodTo", "2026-07-31")
                .param("trainerName", "Tra.Iner")
                .param("trainingType", "Cardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingName").value("Morning session"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-07-20"))
                .andExpect(jsonPath("$[0].trainerName").value("Tra.Iner"));

        verify(gymFacade).getTraineeTrainings(eq("Tr.Ainee"),
                eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 7, 31)),
                eq("Tra.Iner"), eq("Cardio"));
    }
}
