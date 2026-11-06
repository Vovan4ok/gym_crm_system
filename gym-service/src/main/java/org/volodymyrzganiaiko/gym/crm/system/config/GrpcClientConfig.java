package org.volodymyrzganiaiko.gym.crm.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;

@Configuration
public class GrpcClientConfig {

    /**
     * Blocking stub for workload-service. The channel "workload" is configured
     * under spring.grpc.client.channels.workload; it connects lazily on the
     * first call, so gym-service starts even if workload-service is down.
     */
    @Bean
    public WorkloadServiceGrpc.WorkloadServiceBlockingStub workloadStub(GrpcChannelFactory channels) {
        return WorkloadServiceGrpc.newBlockingStub(channels.createChannel("workload"));
    }
}
