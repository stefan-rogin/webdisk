package com.example.webdisk.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * BearerAuthenticationFilter is a custom filter that processes HTTP requests
 * to authorize users based on a preauthentication Bearer token provided in the Authorization header.
 * 
 * <p>Note: In a real implementation, the token should be verified against an external 
 * authentication service to ensure its validity.</p>
 *  
 * @see OncePerRequestFilter
 * @see PreAuthenticatedAuthenticationToken
 * @see SecurityContextHolder
 */
public class BearerAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Filters incoming HTTP requests to check for a Bearer token in the Authorization header.
     * If a valid Bearer token is found, it sets the authentication in the security context.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during the filtering process
     * @throws IOException if an I/O error occurs during the filtering process
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            // In a real implementation, the token would be verified against
            // the external authentication service
            PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                header.substring(7), 
                "Admin",
                Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

}