package com.example.smartcampusapi.exception;

import com.example.smartcampusapi.config.SmartCampusApplication;
import com.example.smartcampusapi.model.ApiError;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ApiError error = new ApiError(
                exception.getMessage(),
                Response.Status.CONFLICT.getStatusCode(),
                SmartCampusApplication.DOCUMENTATION_URL,
                uriInfo == null ? "" : uriInfo.getPath(),
                System.currentTimeMillis());

        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }
}
