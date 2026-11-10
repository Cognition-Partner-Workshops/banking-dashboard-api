package com.banking.dashboard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Injects a dark-theme stylesheet into the Swagger UI page.
 *
 * <p>Swagger UI is served from the springdoc/webjar bundle, so its
 * {@code index.html} cannot be edited directly. This filter rewrites the served
 * HTML on the fly, adding the dark-theme stylesheet plus a small script that
 * renders a dark/light toggle button in the topbar, before {@code </head>}.
 */
@Component
public class SwaggerUiDarkThemeFilter extends OncePerRequestFilter {

    private static final String DARK_THEME_LINK =
            "<link id=\"swagger-dark-theme\" rel=\"stylesheet\" type=\"text/css\" href=\"/css/swagger-dark-theme.css\" />\n"
            + "    <script src=\"/js/swagger-theme-toggle.js\"></script>\n  </head>";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri == null || !uri.endsWith("/swagger-ui/index.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        byte[] body = wrapper.getContentAsByteArray();
        String contentType = wrapper.getContentType();
        boolean isHtml = body.length > 0 && contentType != null && contentType.contains("text/html");

        if (!isHtml) {
            wrapper.copyBodyToResponse();
            return;
        }

        String html = new String(body, StandardCharsets.UTF_8);
        if (html.contains("</head>") && !html.contains("swagger-dark-theme.css")) {
            html = html.replace("</head>", DARK_THEME_LINK);
        }

        byte[] modified = html.getBytes(StandardCharsets.UTF_8);
        response.setContentType(contentType);
        response.setContentLength(modified.length);
        response.getOutputStream().write(modified);
    }
}
