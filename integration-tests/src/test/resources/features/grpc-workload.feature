Feature: Trainer workload served by gym-service over gRPC
  gym-service answers GET /api/trainers/{username}/workload by calling
  workload-service over gRPC. Every request goes through the gateway.

    @positive @grpc
    Scenario: A new trainer sees an empty workload, then their training minutes
      Given a trainer and a trainee are registered
      And the trainer logs in
      When the trainer requests their workload
      Then the response status is 200
      And the workload has no entries
      When the trainee logs in
      And the trainee creates a 60-minute training
      Then the trainer's own workload eventually shows 60 minutes

    @negative @grpc
    Scenario: A trainee cannot read a trainer's workload
      Given a trainer and a trainee are registered
      And the trainee logs in
      When the trainee requests the trainer's workload
      Then the response status is 403

    @negative @grpc
    Scenario: Only trainers have a workload
      Given a registered trainee
      And the trainee logs in
      When the trainee requests a workload under their own username
      Then the response status is 404

    @negative @grpc @workload-down
    Scenario: The workload is unavailable while workload-service is down, and recovers
      Given a trainer and a trainee are registered
      And the trainer logs in
      And workload-service is stopped
      When the trainer requests their workload
      Then the response status is 503
      And once workload-service is started again the workload is returned
