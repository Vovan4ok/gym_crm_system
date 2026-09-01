package org.volodymyrzganiaiko.gym.crm.system.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxMessage {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "destination")
    private String destination;

    @Column(name = "payload")
    private String payload;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "status")
    private String status;

    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
