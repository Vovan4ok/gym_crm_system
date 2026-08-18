package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @DataJpaTest only picks up entities and Spring Data repositories, so the hand-written
 * DAO components have to be contributed explicitly.
 */
@TestConfiguration
@ComponentScan("org.volodymyrzganiaiko.gym.crm.system.dao.impl")
public class DaoTestConfig {
}
