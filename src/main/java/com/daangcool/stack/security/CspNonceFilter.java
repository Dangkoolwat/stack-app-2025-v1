package com.daangcool.stack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

public class CspNonceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // Generate nonce
        String nonce = UUID.randomUUID().toString();
        request.setAttribute("cspNonce", nonce);

        // Define CSP
        String csp = String.format(
            "default-src 'self'; " +
            "style-src 'self' 'nonce-%s' 'unsafe-inline'; " +
            "script-src 'self' 'nonce-%s' 'unsafe-eval'; " +
            "img-src 'self' data:; " +
            "font-src 'self' data:", 
            nonce, nonce
        );
        response.setHeader("Content-Security-Policy", csp);

        // Wrap response to modify content
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.contains("text/html")) {
            String content = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
            String modifiedContent = content.replace("<%= nonce %>", nonce);
            byte[] modifiedBytes = modifiedContent.getBytes(responseWrapper.getCharacterEncoding());
            response.setContentLength(modifiedBytes.length);
            response.getOutputStream().write(modifiedBytes);
        } else {
            responseWrapper.copyBodyToResponse();
        }
    }
}
