package com.example.smartcampusapi.exception;

import com.example.smartcampusapi.config.SmartCampusApplication;
import com.example.smartcampusapi.model.ApiError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(ThrowableMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Unexpected API failure", exception);

        ApiError error = new ApiError(
                "An unexpected internal server error occurred.",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                SmartCampusApplication.DOCUMENTATION_URL,
                uriInfo == null ? "" : uriInfo.getPath(),
                System.currentTimeMillis());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
