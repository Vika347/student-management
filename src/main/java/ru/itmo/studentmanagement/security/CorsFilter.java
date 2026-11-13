
package ru.itmo.studentmanagement.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

// Разрешает запросы frontend к Java API
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ALLOWED_ORIGIN = "http://localhost:5500";

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        // Отвечаем на предварительные CORS-запросы
        if ("OPTIONS".equals(requestContext.getMethod())) {
            requestContext.abortWith(
                    jakarta.ws.rs.core.Response.noContent().build()
            );
        }
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext)
            throws IOException {

        String origin = requestContext.getHeaderString("Origin");

        if (!ALLOWED_ORIGIN.equals(origin)) {
            return;
        }

        responseContext.getHeaders().putSingle(
                "Access-Control-Allow-Origin", ALLOWED_ORIGIN
        );
        responseContext.getHeaders().putSingle(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, PATCH, DELETE, QUERY, OPTIONS"
        );
        responseContext.getHeaders().putSingle(
                "Access-Control-Allow-Headers",
                "Authorization, Content-Type, Accept"
        );
        responseContext.getHeaders().putSingle(
                "Access-Control-Max-Age", "600"
        );
        responseContext.getHeaders().add("Vary", "Origin");
    }
}

