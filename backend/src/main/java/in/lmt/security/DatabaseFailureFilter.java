package in.lmt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Covers failures in persistent-session loading/saving, which occur outside MVC advice. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseFailureFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (ServletException | RuntimeException failure) {
            boolean unavailable = false;
            for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.springframework.dao.DataAccessException
                        || cause instanceof org.springframework.transaction.TransactionException
                        || cause instanceof java.sql.SQLException) {
                    unavailable = true;
                    break;
                }
            }
            if (!unavailable || response.isCommitted()) throw failure;
            response.resetBuffer();
            response.setHeader("Retry-After", "60");
            SecurityProblems.write(response, 503, "SERVICE_UNAVAILABLE",
                    "The service is temporarily unavailable. Retry the same submission after recovery.");
        }
    }
}
