Feature: Workload consumer
    Scenario: An ADD message updates the trainer summary
      When an ADD workload message for "Tra.Iner" with 60 minutes is sent
      Then the "Tra.Iner" summary eventually shows 60 minutes

    Scenario: Two ADD messages accumulate
      When an ADD workload message for "Tra.Iner" with 60 minutes is sent
      And an ADD workload message for "Tra.Iner" with 30 minutes is sent
      Then the "Tra.Iner" summary eventually shows 90 minutes

    Scenario: An invalid message goes to the DLQ
      When an invalid workload message is sent
      Then a message appears on the DLQ

    Scenario: Unknown trainer returns 404
      When I request the workload of "Nobody"
      Then the response status is 404