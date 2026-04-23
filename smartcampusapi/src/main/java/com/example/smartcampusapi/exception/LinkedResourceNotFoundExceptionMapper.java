package com.example.smartcampusapi.exception;

import com.example.smartcampusapi.config.SmartCampusApplication;
import com.example.smartcampusapi.model.ApiError;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ApiError error = new ApiError(
                exception.getMessage(),
                422,
                SmartCampusApplication.DOCUMENTATION_URL,
                uriInfo == null ? "" : uriInfo.getPath(),
                System.currentTimeMillis());

        return Response.status(422).entity(error).build();
    }
}
