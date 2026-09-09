package org.volodymyrzganiaiko.gym.crm.system.cucumber.steps;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Hooks {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE users, outbox_messages RESTART IDENTITY CASCADE");
    }
}
