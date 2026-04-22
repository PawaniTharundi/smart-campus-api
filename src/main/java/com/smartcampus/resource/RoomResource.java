package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // FIX: Inject UriInfo to build the Location header for POST responses
    @Context
    private UriInfo uriInfo;

    // GET /api/v1/rooms
    @GET
    public Response getAllRooms() {
        Collection<Room> allRooms = store.getRooms().values();
        return Response.ok(allRooms).build();
    }

    // GET /api/v1/rooms/{roomId}
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(error("Room not found: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    // POST /api/v1/rooms
    // FIX: Now returns a proper Location header pointing to the newly created room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400)
                    .entity(error("Room ID is required"))
                    .build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(409)
                    .entity(error("Room with this ID already exists"))
                    .build();
        }
        store.getRooms().put(room.getId(), room);

        // Build the Location URI: e.g. http://localhost:8080/api/v1/rooms/LIB-301
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(location).entity(room).build();
    }

    // PUT /api/v1/rooms/{roomId}
    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updated) {
        Room existing = store.getRooms().get(roomId);
        if (existing == null) {
            return Response.status(404)
                    .entity(error("Room not found: " + roomId))
                    .build();
        }
        if (updated.getName() != null)  existing.setName(updated.getName());
        if (updated.getCapacity() > 0)  existing.setCapacity(updated.getCapacity());
        return Response.ok(existing).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            // Idempotent: already gone, still return 404 to inform client
            return Response.status(404)
                    .entity(error("Room not found: " + roomId))
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room " + roomId + " still has " + room.getSensorIds().size()
                    + " sensor(s) assigned. Remove all sensors before deleting this room.");
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }

    private Map<String, String> error(String msg) {
        Map<String, String> e = new HashMap<>();
        e.put("error", msg);
        return e;
    }
}