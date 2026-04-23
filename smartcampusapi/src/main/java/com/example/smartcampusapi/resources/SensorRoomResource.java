package com.example.smartcampusapi.resources;

import com.example.smartcampusapi.model.Room;
import com.example.smartcampusapi.store.SmartCampusStore;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final SmartCampusStore store = SmartCampusStore.getInstance();

    @GET
    public List<Room> getAllRooms() {
        return store.getAllRooms();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Room createdRoom = store.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(createdRoom.getId()).build();
        return Response.created(location).entity(createdRoom).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        return store.getRoomOrThrow(roomId);
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
