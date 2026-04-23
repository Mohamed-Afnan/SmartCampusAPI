package com.example.smartcampusapi.resources;

import com.example.smartcampusapi.model.SensorReading;
import com.example.smartcampusapi.store.SmartCampusStore;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final SmartCampusStore store;

    public SensorReadingResource(String sensorId, SmartCampusStore store) {
        this.sensorId = sensorId;
        this.store = store;
    }

    @GET
    public List<SensorReading> getReadings() {
        return store.getReadingsForSensor(sensorId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        SensorReading createdReading = store.addReading(sensorId, reading);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdReading.getId()).build();
        return Response.created(location).entity(createdReading).build();
    }
}
