package com.example.smartcampusapi.exception;

import com.example.smartcampusapi.config.SmartCampusApplication;
import com.example.smartcampusapi.model.ApiError;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int statusCode = exception.getResponse() == null
                ? Response.Status.BAD_REQUEST.getStatusCode()
                : exception.getResponse().getStatus();

        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "The request could not be processed.";
        }

        ApiError error = new ApiError(
                message,
                statusCode,
                SmartCampusApplication.DOCUMENTATION_URL,
                uriInfo == null ? "" : uriInfo.getPath(),
                System.currentTimeMillis());

        return Response.status(statusCode).entity(error).build();
    }
}
