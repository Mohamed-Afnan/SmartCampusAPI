package com.example.smartcampusapi.exception;

import com.example.smartcampusapi.config.SmartCampusApplication;
import com.example.smartcampusapi.model.ApiError;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ApiError error = new ApiError(
                exception.getMessage(),
                Response.Status.FORBIDDEN.getStatusCode(),
                SmartCampusApplication.DOCUMENTATION_URL,
                uriInfo == null ? "" : uriInfo.getPath(),
                System.currentTimeMillis());

        return Response.status(Response.Status.FORBIDDEN).entity(error).build();
    }
}
