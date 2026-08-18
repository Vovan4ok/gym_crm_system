package org.volodymyrzganiaiko.gym.crm.system.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityEqualsHashCodeTest {
    @Test
    void hashCode_isStable_whenIdIsAssignedAfterAddingToSet() {
        Trainee trainee = new Trainee(LocalDate.of(2000, 1, 1), "addr",
                "John", "Doe", "John.Doe", "pass", true, new HashSet<>());

        Set<Trainee> set = new HashSet<>();
        set.add(trainee);

        trainee.setId(42L);

        assertTrue(set.contains(trainee));
    }

    @Test
    public void equals_isFalse_forTwoTransientInstances() {
        Trainee trainee1 = new Trainee();
        Trainee trainee2 = new Trainee();

        assertNotEquals(trainee1, trainee2);
    }

    @Test
    public void equals_isTrue_forSameId() {
        Trainee trainee1 = new Trainee();
        Trainee trainee2 = new Trainee();
        trainee1.setId(42L);
        trainee2.setId(42L);

        assertEquals(trainee1, trainee2);
    }

    @Test
    public void equals_isFalse_forDifferentSubtypes_sameId() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        trainee.setId(42L);
        trainer.setId(42L);

        assertNotEquals(trainee, trainer);
    }

    @Test
    public void trainingType_equalsByName() {
        TrainingType trainingType1 = new TrainingType("Yoga");
        TrainingType trainingType2 = new TrainingType("Yoga");
        TrainingType trainingType3 = new TrainingType("Cardio");

        assertEquals(trainingType1, trainingType2);
        assertNotEquals(trainingType2, trainingType3);
    }
}
