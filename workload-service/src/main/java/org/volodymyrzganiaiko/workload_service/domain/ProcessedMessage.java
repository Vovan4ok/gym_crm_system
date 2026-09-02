package org.volodymyrzganiaiko.workload_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "processed_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMessage {
    @Id
    private String messageId;

    @Indexed(expireAfter = "86400s")
    private Instant processedAt;
}
