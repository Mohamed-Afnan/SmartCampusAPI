package com.example.smartcampusapi.resources;

import com.example.smartcampusapi.model.Sensor;
import com.example.smartcampusapi.store.SmartCampusStore;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SmartCampusStore store = SmartCampusStore.getInstance();

    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        return store.getAllSensors(type);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        Sensor createdSensor = store.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdSensor.getId()).build();
        return Response.created(location).entity(createdSensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        return store.getSensorOrThrow(sensorId);
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource sensorReadingResource(@PathParam("sensorId") String sensorId) {
        store.getSensorOrThrow(sensorId);
        return new SensorReadingResource(sensorId, store);
    }
}
