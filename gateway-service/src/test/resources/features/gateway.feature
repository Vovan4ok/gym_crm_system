Feature: Gateway
    Scenario: Protected route without a token is rejected
      When I GET "/api/workload/x" without a token
      Then the gateway responds 401

    Scenario: Valid token is routed and identity forwarded
      Given a valid token for "John.Doe"
      When I GET "/api/workload/x" with the token
      Then the gateway responds 200
      And downstream received X-Auth-User "John.Doe"

    Scenario: A spoofed X-Auth-User is stripped
      Given a valid token for "John.Doe"
      When I GET "/api/workload/x" with the token and X-Auth-User "hacker"
      Then downstream received X-Auth-User "John.Doe"

    Scenario: Registration is public
      When I POST "/api/trainees" without a token
      Then the gateway responds 200