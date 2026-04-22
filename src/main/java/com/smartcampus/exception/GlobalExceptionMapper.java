package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {

        // If it's already a proper HTTP response exception, pass it through as-is
        if (e instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) e;
            Response original = wae.getResponse();
            Map<String, String> error = new HashMap<>();
            error.put("error", original.getStatus() + " " + getStatusText(original.getStatus()));
            error.put("message", e.getMessage() != null ? e.getMessage() : "No details available.");
            return Response.status(original.getStatus())
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Genuine unexpected error → 500
        logger.severe("Unexpected error: " + e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "500 Internal Server Error");
        error.put("message", "An unexpected error occurred. Please contact the administrator.");
        return Response.status(500)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String getStatusText(int status) {
        switch (status) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 409: return "Conflict";
            case 422: return "Unprocessable Entity";
            default:  return "Error";
        }
    }
}