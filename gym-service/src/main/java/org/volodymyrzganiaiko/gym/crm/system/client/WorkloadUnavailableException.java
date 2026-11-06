package org.volodymyrzganiaiko.gym.crm.system.client;

/** workload-service could not answer: down, too slow, or failed internally. */
public class WorkloadUnavailableException extends RuntimeException {
    public WorkloadUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
