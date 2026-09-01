package org.volodymyrzganiaiko.workload_service.messaging;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ProcessMessageStore {
    private static final int MAX = 10000;
    private final Set<String> seen = Collections.newSetFromMap(
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > MAX;
                }
    }));

    public boolean isProcessed(String messageId) { return seen.contains(messageId); }
    public void markProcessed(String messageId) { seen.add(messageId); }
}
