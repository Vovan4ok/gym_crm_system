package org.volodymyrzganiaiko.auth_service.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.volodymyrzganiaiko.auth_service.AbstractPostgresIT;
import org.volodymyrzganiaiko.auth_service.domain.AuthUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthUserRepositoryIT extends AbstractPostgresIT {
    @Autowired
    private AuthUserRepository authUserRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void findByUsername_success() {
        entityManager.createNativeQuery(
                        "insert into users(user_id, username, password, is_active) values (1, 'John.Doe', 'bcrypt-hash', true)")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        AuthUser result = authUserRepository.findByUsername("John.Doe").orElseThrow();
        assertEquals("John.Doe", result.getUsername());
        assertEquals("bcrypt-hash", result.getPassword());
        assertTrue(result.getIsActive());
    }

    @Test
    public void findByUsername_notFound() {
        Optional<AuthUser> result = authUserRepository.findByUsername("John.Doe");
        assertTrue(result.isEmpty());
    }

}
