package org.volodymyrzganiaiko.workload_service.rpc;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.MDC;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * gRPC counterpart of {@code CorrelationIdFilter}: takes the correlation id
 * from the incoming metadata (or generates one), puts it in the MDC while
 * gRPC runs this call's code, and echoes it in the response headers.
 *
 * <p>The MDC is thread-local and gRPC may run a call's steps (start, message,
 * half-close, ...) on different threads, so the id is set around every step,
 * not once. {@link GlobalServerInterceptor} applies it to every gRPC service.
 */
@Component
@GlobalServerInterceptor
public class CorrelationIdServerInterceptor implements ServerInterceptor {
    public static final Metadata.Key<String> CORRELATION_ID =
            Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    static final String MDC_KEY = "correlationId";

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers,
                                                      ServerCallHandler<Q, R> next) {
        String incoming = headers.get(CORRELATION_ID);
        String correlationId = incoming == null || incoming.isBlank() ? UUID.randomUUID().toString() : incoming;

        ServerCall<Q, R> echoingCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(CORRELATION_ID, correlationId);
                super.sendHeaders(responseHeaders);
            }
        };

        ServerCall.Listener<Q> listener = callWithCorrelationId(correlationId, () -> next.startCall(echoingCall, headers));

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onMessage(Q message) {
                withCorrelationId(correlationId, () -> super.onMessage(message));
            }

            @Override
            public void onHalfClose() {
                // For a unary call this is where the service method runs.
                withCorrelationId(correlationId, super::onHalfClose);
            }

            @Override
            public void onCancel() {
                withCorrelationId(correlationId, super::onCancel);
            }

            @Override
            public void onComplete() {
                withCorrelationId(correlationId, super::onComplete);
            }

            @Override
            public void onReady() {
                withCorrelationId(correlationId, super::onReady);
            }
        };
    }

    /** Runs the action with the id in the MDC and restores whatever was there before. */
    static void withCorrelationId(String correlationId, Runnable action) {
        callWithCorrelationId(correlationId, () -> {
            action.run();
            return null;
        });
    }

    static <T> T callWithCorrelationId(String correlationId, Supplier<T> action) {
        String previous = MDC.get(MDC_KEY);
        MDC.put(MDC_KEY, correlationId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                MDC.remove(MDC_KEY);
            } else {
                MDC.put(MDC_KEY, previous);
            }
        }
    }
}
