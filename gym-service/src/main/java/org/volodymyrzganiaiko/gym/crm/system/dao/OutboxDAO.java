package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.volodymyrzganiaiko.gym.crm.system.domain.OutboxMessage;

import java.util.List;
import java.util.UUID;

public interface OutboxDAO {
    OutboxMessage save(OutboxMessage message);
    List<OutboxMessage> findPending(int limit);
    void markSent(UUID id);
}
