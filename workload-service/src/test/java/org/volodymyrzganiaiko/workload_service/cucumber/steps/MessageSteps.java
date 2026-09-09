package org.volodymyrzganiaiko.workload_service.cucumber.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.volodymyrzganiaiko.workload_service.dto.ActionType;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MessageSteps {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${messaging.workload-queue}")
    private String workloadQueue;

    @Value("${messaging.workload-dlq}")
    private String workloadDlq;

    @When("an ADD workload message for {string} with {int} minutes is sent")
    public void sendAdd(String trainerUsername, int minutes) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                trainerUsername,
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-07-20"),
                minutes,
                ActionType.ADD
        );
        jmsTemplate.convertAndSend(workloadQueue, request, message -> {
            message.setStringProperty("correlationId", "c-1");
            message.setStringProperty("messageId", java.util.UUID.randomUUID().toString());
            return message;
        });
    }

    @When("an invalid workload message is sent")
    public void sendInvalid() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                " ",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-07-20"),
                60,
                ActionType.ADD
        );
        jmsTemplate.convertAndSend(workloadQueue, request, message -> {
            message.setStringProperty("correlationId", "c-1");
            message.setStringProperty("messageId", java.util.UUID.randomUUID().toString());
            return message;
        });
    }

    @Then("a message appears on the DLQ")
    public void messageCheck() {
        jmsTemplate.setReceiveTimeout(5000);
        Object msg = jmsTemplate.receiveAndConvert(workloadDlq);
        assertNotNull(msg);
    }
}
