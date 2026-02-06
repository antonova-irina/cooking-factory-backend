package gr.aueb.cf.cookingfactory.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        log.warn("Access denied for user to request={} with message={}", request.getRequestURI(), accessDeniedException.getMessage());
        // Set the response status and content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json; charset=UTF-8");

        // Write a custom JSON response with the collected information
        String jsonResponse = "{\"code\": \"UserNotAuthorized\", \"description\": \"User is not allowed to access this route.\"}";
        response.getWriter().write(jsonResponse);
    }
}

