package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // FIX: Inject UriInfo to build the Location header for POST responses
    @Context
    private UriInfo uriInfo;

    // GET /api/v1/sensors
    // GET /api/v1/sensors?type=Temperature
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.getSensors().values();
        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = all.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }
        return Response.ok(all).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // POST /api/v1/sensors
    // FIX: Now returns a proper Location header pointing to the newly created sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(400)
                    .entity(error("Sensor ID is required"))
                    .build();
        }
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(409)
                    .entity(error("Sensor with this ID already exists"))
                    .build();
        }
        // Validate that the referenced room actually exists
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Room not found: " + sensor.getRoomId()
                    + ". The roomId in the request body references a room that does not exist.");
        }
        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }
        store.getSensors().put(sensor.getId(), sensor);
        store.getReadings().put(sensor.getId(), new ArrayList<>());
        room.getSensorIds().add(sensor.getId());

        // Build the Location URI: e.g. http://localhost:8080/api/v1/sensors/TEMP-003
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    // PUT /api/v1/sensors/{sensorId}
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updated) {
        Sensor existing = store.getSensors().get(sensorId);
        if (existing == null) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        if (updated.getType() != null)   existing.setType(updated.getType());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        existing.setCurrentValue(updated.getCurrentValue());
        return Response.ok(existing).build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        // Remove sensor ID from its parent room's list
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }
        store.getSensors().remove(sensorId);
        store.getReadings().remove(sensorId);
        return Response.noContent().build();
    }

    // Sub-resource locator → delegates to SensorReadingResource
    // Handles: GET/POST /api/v1/sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private Map<String, String> error(String msg) {
        Map<String, String> e = new HashMap<>();
        e.put("error", msg);
        return e;
    }
}