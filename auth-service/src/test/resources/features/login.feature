Feature: Authentication

    @positive
    Scenario: Successful login with valid credentials
      Given an active user "John.Doe" with password "secret"
      When I log in as "John.Doe" with password "secret"
      Then the response status is 200
      And an access token is returned

    @negative
    Scenario: Login rejected with wrong password
      Given an active user "John.Doe" with password "secret"
      When I log in as "John.Doe" with password "wrong"
      Then the response status is 401

    @negative
    Scenario: Inactive user cannot log in
      Given an inactive user "Jane.Doe" with password "secret"
      When I log in as "Jane.Doe" with password "secret"
      Then the response status is 401