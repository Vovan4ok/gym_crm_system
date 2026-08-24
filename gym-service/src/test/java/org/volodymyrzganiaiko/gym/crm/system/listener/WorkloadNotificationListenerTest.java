package org.volodymyrzganiaiko.gym.crm.system.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.gym.crm.system.client.WorkloadClient;
import org.volodymyrzganiaiko.gym.crm.system.dto.ActionType;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.gym.crm.system.event.TraineeDeletedWorkloadEvent;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadNotificationListenerTest {
    @Mock
    WorkloadClient workloadClient;
    @InjectMocks
    WorkloadNotificationListener listener;

    @Test
    void onTraineeDeleted_sendsEachWorkloadWithTransactionId() {
        var w1 = new TrainerWorkloadRequest("Tra.Iner", "Tra", "Iner", true,
                LocalDate.parse("2024-01-10"), 60, ActionType.DELETE);
        var event = new TraineeDeletedWorkloadEvent(List.of(w1), "txn-123");

        listener.onTraineeDeleted(event);

        verify(workloadClient).sendWorkload(w1, "txn-123");
    }

    @Test
    void onTraineeDeleted_emptyList_sendsNothing() {
        listener.onTraineeDeleted(new TraineeDeletedWorkloadEvent(List.of(), "txn-123"));
        verifyNoInteractions(workloadClient);
    }
}
