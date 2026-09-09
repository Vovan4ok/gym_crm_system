Feature: Gym CRM

    @positive
    Scenario: Register a trainee
      When I register a trainee "John" "Doe"
      Then the response status is 201
      And a username and password are returned

    @positive
    Scenario: Create a training produces an outbox message
      Given a registered trainer and trainee
      When the trainee creates a 60-minute "Cardio" training
      Then the response status is 201
      And an outbox message for the trainer exists

    @negative
    Scenario: Deleting another trainee's profile is forbidden
      Given a registered trainee
      When user "Someone.Else" deletes trainee's profile
      Then the response status is 403

    @negative
    Scenario: Creating a training with a missing field is rejected
      Given a registered trainer and trainee
      When the trainee creates a training without a date
      Then the response status is 400