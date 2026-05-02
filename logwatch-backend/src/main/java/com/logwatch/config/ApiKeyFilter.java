package com.logwatch.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class ApiKeyFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    @Value("${logwatch.api.key:logwatch-dev-key}")
    private String apiKey;

    @Value("${logwatch.api.key-enabled:true}")
    private boolean keyEnabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (!keyEnabled || isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String providedKey = httpRequest.getHeader(API_KEY_HEADER);

        if (apiKey.equals(providedKey)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Acceso denegado: API Key inválida desde {}", httpRequest.getRemoteAddr());
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"API Key inválida o ausente\"}");
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/actuator/health");
    }
}
