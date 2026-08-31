package org.volodymyrzganiaiko.workload_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;

import java.util.Map;

@Configuration
public class JmsConfig {
    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of("trainerWorkload", TrainerWorkloadRequest.class));
        return converter;
    }

    @Bean
    public ActiveMQConnectionFactoryCustomizer redeliveryPolicyCustomizer() {
        return factory -> {
            RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            redeliveryPolicy.setMaximumRedeliveries(3);
            redeliveryPolicy.setInitialRedeliveryDelay(1000);
            redeliveryPolicy.setUseExponentialBackOff(true);
            redeliveryPolicy.setBackOffMultiplier(2);
            factory.setRedeliveryPolicy(redeliveryPolicy);
        };
    }
}
