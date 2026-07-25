package org.volodymyrzganiaiko.gym.crm.system.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor
public class Trainer extends User {
    @ManyToOne
    @JoinColumn(name = "training_type_id")
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer(TrainingType specialization, String firstName, String lastName, String username, String password, Boolean isActive, Set<Trainee> trainees) {
        super(firstName, lastName, username, password, isActive);
        this.specialization = specialization;
        this.trainees = trainees;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "id=" + getId() +
                '}';
    }
}
