package br.com.gazintech.orderapp.logger;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * A filter that logs request details such as request ID, user agent, and remote address.
 * It uses SLF4J's MDC (Mapped Diagnostic Context) to store these details for logging purposes.
 */
@Order(1)
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            MDC.put("request-id", UUID.randomUUID().toString());
            MDC.put("user-agent", httpRequest.getHeader("User-Agent"));
            MDC.put("remote-addr", httpRequest.getRemoteAddr());
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
