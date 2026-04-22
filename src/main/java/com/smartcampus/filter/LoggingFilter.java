package com.smartcampus.filter;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
// FIX: @PreMatching ensures the filter runs BEFORE JAX-RS matches the request to a resource method.
// This guarantees ALL incoming requests are logged, even ones that result in 404/405 errors.
@PreMatching
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.info(">>> REQUEST:  "
                + requestContext.getMethod()
                + " "
                + requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        logger.info("<<< RESPONSE: "
                + requestContext.getMethod()
                + " "
                + requestContext.getUriInfo().getRequestUri()
                + " → HTTP "
                + responseContext.getStatus());
    }
}