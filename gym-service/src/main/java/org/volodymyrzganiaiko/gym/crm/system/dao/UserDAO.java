package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);

    List<User> findAll();
}
