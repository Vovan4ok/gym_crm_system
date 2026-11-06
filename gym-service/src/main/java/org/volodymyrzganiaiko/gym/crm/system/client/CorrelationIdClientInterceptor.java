package org.volodymyrzganiaiko.gym.crm.system.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Copies the current request's correlation id (set in the MDC by
 * {@code CorrelationIdFilter}) into the metadata of every outgoing gRPC call,
 * so workload-service logs the same id. gRPC metadata is the equivalent of
 * HTTP headers.
 */
@Component
public class CorrelationIdClientInterceptor implements ClientInterceptor {
    /** Metadata keys are lowercase (HTTP/2 headers). */
    public static final Metadata.Key<String> CORRELATION_ID =
            Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final String MDC_KEY = "correlationId";

    @Override
    public <Q, R> ClientCall<Q, R> interceptCall(MethodDescriptor<Q, R> method, CallOptions options, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, options)) {
            @Override
            public void start(Listener<R> responseListener, Metadata headers) {
                // Read here, on the calling (request) thread, where the MDC is set.
                String correlationId = MDC.get(MDC_KEY);
                if (correlationId != null) {
                    headers.put(CORRELATION_ID, correlationId);
                }
                super.start(responseListener, headers);
            }
        };
    }
}
