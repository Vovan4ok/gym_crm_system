package org.volodymyrzganiaiko.gym.crm.system.mapper;

import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.Training;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;

@Component
public class DtoMapper {

    public TrainingTypeResponse mapTrainingTypeToTrainingTypeResponse(TrainingType trainingType) {
        return new TrainingTypeResponse(trainingType.getId(), trainingType.getTrainingTypeName());
    }

    public TrainerSummaryResponse mapTrainerToTrainerSummaryResponse(Trainer trainer) {
        return new TrainerSummaryResponse(trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(), mapTrainingTypeToTrainingTypeResponse(trainer.getSpecialization()));
    }

    public TraineeProfileResponse mapTraineeToTraineeProfileResponse(Trainee trainee) {
        return new TraineeProfileResponse(trainee.getUsername(), trainee.getFirstName(), trainee.getLastName(), trainee.getDateOfBirth(), trainee.getAddress(), trainee.getIsActive(), trainee.getTrainers().stream().map(this::mapTrainerToTrainerSummaryResponse).toList());
    }

    public TraineeSummaryResponse mapTraineeToTraineeSummaryResponse(Trainee trainee) {
        return new TraineeSummaryResponse(trainee.getUsername(), trainee.getFirstName(), trainee.getLastName());
    }

    public TraineeTrainingResponse mapTrainingToTraineeTrainingResponse(Training training) {
        return new TraineeTrainingResponse(training.getTrainingName(), training.getTrainingDate(), mapTrainingTypeToTrainingTypeResponse(training.getTrainingType()), training.getTrainingDurationInMinutes(), training.getTrainer().getUsername());
    }

    public TrainerTrainingResponse mapTrainingToTrainerTrainingResponse(Training training) {
        return new TrainerTrainingResponse(training.getTrainingName(), training.getTrainingDate(), mapTrainingTypeToTrainingTypeResponse(training.getTrainingType()), training.getTrainingDurationInMinutes(), training.getTrainee().getUsername());
    }

    public TrainerProfileResponse mapTrainerToTrainerProfileResponse(Trainer trainer) {
        return new TrainerProfileResponse(trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(), mapTrainingTypeToTrainingTypeResponse(trainer.getSpecialization()), trainer.getIsActive(), trainer.getTrainees().stream().map(this::mapTraineeToTraineeSummaryResponse).toList());
    }
}
