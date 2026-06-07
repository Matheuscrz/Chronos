package com.caelum.chronos.shared.infra.logging;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro responsável por gerar e propagar um Correlation ID para cada
 * requisição HTTP. O Correlation ID é um identificador único que permite
 * rastrear uma requisição através de diferentes serviços e componentes do
 * sistema, facilitando a depuração e o monitoramento. Este filtro é executado
 * uma vez por requisição e garante que o Correlation ID seja adicionado ao
 * contexto de log e incluído nas respostas HTTP para que os clientes possam
 * correlacionar as requisições com os logs gerados no servidor.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = LogContext.getOrCreateCorrelationId(request);

        LogContext.startRequest(correlationId, request.getRequestURI());
        response.setHeader(LogContext.HEADER_CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            LogContext.clear();
        }
    }
}