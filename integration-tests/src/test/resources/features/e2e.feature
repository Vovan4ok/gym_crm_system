Feature: End-to-end journey

    @positive
    Scenario: Register, log in, train, and see the workload
      When a trainer and a trainee are registered
      And the trainee logs in
      And the trainee creates a 60-minute training
      Then the training is accepted
      And the trainer workload eventually shows 60 minutes

    @negative
    Scenario: Login with a wrong password is rejected
      Given a registered trainee
      When the trainee logs in with a wrong password
      Then the response status is 401