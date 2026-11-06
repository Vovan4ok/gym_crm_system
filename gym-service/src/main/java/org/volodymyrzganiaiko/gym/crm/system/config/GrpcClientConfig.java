package org.volodymyrzganiaiko.gym.crm.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.volodymyrzganiaiko.gym.crm.system.client.CorrelationIdClientInterceptor;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;

@Configuration
public class GrpcClientConfig {

    /**
     * Blocking stub for workload-service. The channel "workload" is configured
     * under spring.grpc.client.channels.workload; it connects lazily on the
     * first call, so gym-service starts even if workload-service is down.
     * Every call carries the correlation id (see the interceptor).
     */
    @Bean
    public WorkloadServiceGrpc.WorkloadServiceBlockingStub workloadStub(GrpcChannelFactory channels,
                                                                        CorrelationIdClientInterceptor correlationId) {
        return WorkloadServiceGrpc.newBlockingStub(channels.createChannel("workload"))
                .withInterceptors(correlationId);
    }
}
