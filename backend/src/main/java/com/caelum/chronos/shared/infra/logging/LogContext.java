package com.caelum.chronos.shared.infra.logging;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

public final class LogContext {

    public static final String CORRELATION_ID = "correlationId";
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_PATH = "requestPath";
    public static final String EVENT_ID = "eventId";
    public static final String EVENT_TYPE = "eventType";

    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    private LogContext() {
    }

    public static String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }

    public static void startRequest(String correlationId, String requestPath) {
        put(CORRELATION_ID, correlationId);
        put(REQUEST_PATH, requestPath);
    }

    public static void setEvent(String eventId, String eventType) {
        put(EVENT_ID, eventId);
        put(EVENT_TYPE, eventType);
    }

    public static void setUserId(String userId) {
        put(USER_ID, userId);
    }

    public static void setTrace(String traceId, String spanId) {
        put(TRACE_ID, traceId);
        put(SPAN_ID, spanId);
    }

    public static void clearEvent() {
        MDC.remove(EVENT_ID);
        MDC.remove(EVENT_TYPE);
    }

    public static void clearRequest() {
        MDC.remove(CORRELATION_ID);
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(USER_ID);
        MDC.remove(REQUEST_PATH);
        MDC.remove(EVENT_ID);
        MDC.remove(EVENT_TYPE);
    }

    public static void clear() {
        MDC.clear();
    }

    private static void put(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }
}