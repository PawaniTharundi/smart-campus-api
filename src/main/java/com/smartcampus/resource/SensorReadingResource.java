package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        if (!store.getSensors().containsKey(sensorId)) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        List<SensorReading> history = store.getReadings()
                .getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    // GET /api/v1/sensors/{sensorId}/readings/{readingId}
    @GET
    @Path("/{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        if (!store.getSensors().containsKey(sensorId)) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        List<SensorReading> history = store.getReadings()
                .getOrDefault(sensorId, new ArrayList<>());
        return history.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(404)
                        .entity(error("Reading not found: " + readingId))
                        .build());
    }

    // POST /api/v1/sensors/{sensorId}/readings
    // FIX: Now also blocks OFFLINE sensors (spec lists ACTIVE, MAINTENANCE, OFFLINE)
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        // Block readings for MAINTENANCE or OFFLINE sensors
        if ("MAINTENANCE".equals(sensor.getStatus()) || "OFFLINE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is currently " + sensor.getStatus()
                    + " and cannot accept new readings.");
        }
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.getReadings()
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // Side effect: update the parent sensor's currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());

        return Response.status(201).entity(reading).build();
    }

    // DELETE /api/v1/sensors/{sensorId}/readings/{readingId}
    @DELETE
    @Path("/{readingId}")
    public Response deleteReading(@PathParam("readingId") String readingId) {
        if (!store.getSensors().containsKey(sensorId)) {
            return Response.status(404)
                    .entity(error("Sensor not found: " + sensorId))
                    .build();
        }
        List<SensorReading> history = store.getReadings()
                .getOrDefault(sensorId, new ArrayList<>());
        boolean removed = history.removeIf(r -> r.getId().equals(readingId));
        if (!removed) {
            return Response.status(404)
                    .entity(error("Reading not found: " + readingId))
                    .build();
        }
        return Response.noContent().build();
    }

    private Map<String, String> error(String msg) {
        Map<String, String> e = new HashMap<>();
        e.put("error", msg);
        return e;
    }
}