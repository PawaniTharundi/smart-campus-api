# Smart Campus API

**Module:** 5COSC022W Client-Server Architectures  
**Student:** Pawani Tharundi  
**Student ID:** 20241212 / W2119802  
**University:** University of Westminster  

---

## What is this project?

This is a REST API I built for the Smart Campus coursework. It manages Rooms and Sensors across a university campus. Facilities managers can use it to add rooms, register sensors in those rooms, and log sensor readings over time.

The API is built using JAX-RS (Jersey) running on an embedded Grizzly HTTP server. All data is stored in memory using ConcurrentHashMap and ArrayList — no database is used.

Base URL: `http://localhost:8080/api/v1`

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java
    ├── SmartCampusApplication.java
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── store/
    │   └── DataStore.java
    ├── resource/
    │   ├── DiscoveryResource.java
    │   ├── RoomResource.java
    │   ├── SensorResource.java
    │   └── SensorReadingResource.java
    ├── exception/
    │   ├── RoomNotEmptyException.java + RoomNotEmptyMapper.java
    │   ├── LinkedResourceNotFoundException.java + LinkedResourceMapper.java
    │   ├── SensorUnavailableException.java + SensorUnavailableMapper.java
    │   └── GlobalExceptionMapper.java
    └── filter/
        └── LoggingFilter.java
```

---

## How to Build and Run

**You need:** Java 17, Maven 3.6+

**Step 1 — Build**
```bash
mvn clean package
```

**Step 2 — Run**
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

You should see this in the terminal:
```
===========================================
Smart Campus API is running!
URL: http://localhost:8080/api/v1
Press ENTER to stop the server.
===========================================
```

**Step 3 — Stop**  
Just press Enter in the terminal.

---

## API Endpoints

### Discovery
- `GET /api/v1` — returns API info and links to main resources

### Rooms
- `GET /api/v1/rooms` — get all rooms
- `GET /api/v1/rooms/{roomId}` — get one room
- `POST /api/v1/rooms` — create a room (returns 201)
- `PUT /api/v1/rooms/{roomId}` — update a room
- `DELETE /api/v1/rooms/{roomId}` — delete a room (only works if no sensors assigned)

### Sensors
- `GET /api/v1/sensors` — get all sensors
- `GET /api/v1/sensors?type=Temperature` — filter by type
- `GET /api/v1/sensors/{sensorId}` — get one sensor
- `POST /api/v1/sensors` — register a new sensor (roomId must exist)
- `PUT /api/v1/sensors/{sensorId}` — update a sensor
- `DELETE /api/v1/sensors/{sensorId}` — delete a sensor

### Sensor Readings
- `GET /api/v1/sensors/{sensorId}/readings` — get all readings for a sensor
- `GET /api/v1/sensors/{sensorId}/readings/{readingId}` — get one reading
- `POST /api/v1/sensors/{sensorId}/readings` — add a new reading (also updates the sensor's currentValue)
- `DELETE /api/v1/sensors/{sensorId}/readings/{readingId}` — delete a reading

---

## Sample curl Commands

**1. Get API info**
```bash
curl -X GET http://localhost:8080/api/v1
```

**2. Get all rooms**
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

**3. Create a new room**
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"HALL-A1\",\"name\":\"Main Hall\",\"capacity\":100}"
```

**4. Get only Temperature sensors**
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**5. Create a new sensor**
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400,\"roomId\":\"LAB-101\"}"
```

**6. Post a new reading**
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":25.5}"
```

**7. Get all readings for a sensor**
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

**8. Try to delete a room that still has sensors (should get 409)**
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**9. Delete a sensor first, then the room**
```bash
curl -X DELETE http://localhost:8080/api/v1/sensors/TEMP-001
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

---

## Error Handling

The API never returns a raw Java stack trace. All errors come back as JSON. The custom exception mappers handle these specific cases:

- **409 Conflict** — trying to delete a room that still has sensors assigned to it
- **422 Unprocessable Entity** — registering a sensor with a roomId that doesn't exist
- **403 Forbidden** — posting a reading to a sensor that is in MAINTENANCE or OFFLINE status
- **500 Internal Server Error** — any unexpected error that isn't caught elsewhere

Example 409 response:
```json
{
  "error": "409 Conflict",
  "message": "Room LIB-301 still has 2 sensor(s) assigned. Remove all sensors before deleting this room."
}
```

Example 422 response:
```json
{
  "error": "422 Unprocessable Entity",
  "message": "Room not found: FAKE-999. The roomId in the request body references a room that does not exist."
}
```

---

## Logging

Every request and response is logged to the console automatically through the LoggingFilter. The output looks like this:

```
>>> REQUEST:  GET http://localhost:8080/api/v1/rooms
<<< RESPONSE: GET http://localhost:8080/api/v1/rooms → HTTP 200
```

---

## Pre-loaded Data

When the server starts, it already has some sample data loaded so you can test the endpoints straight away:

- Room: LIB-301 (Library Quiet Study, capacity 50)
- Room: LAB-101 (Computer Lab, capacity 30)
- Sensor: TEMP-001 (Temperature, ACTIVE, in LIB-301)
- Sensor: HUM-001 (Humidity, ACTIVE, in LIB-301)
- Sensor: TEMP-002 (Temperature, ACTIVE, in LAB-101)

---

## Report — Question Answers

### Part 1.1 — JAX-RS Resource Class Lifecycle

y default JAX-RS creates a new instance of each resource class for every single HTTP request that comes in. This is called the per-request lifecycle. Because of this, you can't store shared data inside a resource class as an instance field — it would just be lost after each request finishes.

To get around this I used a Singleton DataStore class. It gets created once when the application starts and the same instance is shared across every request through `DataStore.getInstance()`. I also used ConcurrentHashMap instead of a regular HashMap because multiple requests can come in at the same time, and ConcurrentHashMap handles concurrent reads and writes safely without needing manual synchronization.

---

### Part 1.2 — HATEOAS

HATEOAS (Hypermedia as the Engine of Application State): HATEOAS allows the application-state to be discovered by clients through hypermedia link relations rather than relying on resources being predefined via a separate document. In this manner, there is no need for any change in the linking structure to break existing applications. 

The example given in this project is the discovery endpoint `GET /api/v1` provides links to `/api/v1/rooms` and `/api/v1/sensors` so that clients can discover everything needed to work from the root. If there are any URL-structure changes, clients using links will continue to function.

---

### Part 2.1 — Returning IDs vs Full Objects

I decided to return the entire room object rather than just returning the room ID in the list response, because if only the room IDs were returned to the client, the client would have to send a new GET request for each room to obtain its name, occupancy capacity, and list of sensors. This results in an excessive number of requests for even a moderate number of rooms, which is referred to as the N + 1 problem. By returning the complete room object, the client retrieves everything it needs at once. This is more appropriate for the scope of this project, which is a large university campus where it would be impractical to send individual requests for so many different rooms.

---

### Part 2.2 — Is DELETE Idempotent?

Yes, DELETE operations are idempotent. You make a DELETE request to remove a room from your system for the first time and the server sends you back 204 No Content to indicate that the room has been removed. If you then make the same DELETE request again, the server will respond with 404 Not Found because the requested room is no longer in the system due to having just been deleted. While the response code differs, the two DELETE requests leave the server in the same state; the requested room does not exist. This is considered standard REST idempotent behaviour.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` tells JAX-RS to only accept requests that use a `Content-Type` of `application/json`. If you send a request with `text/plain` or another media type, JAX-RS will automatically reject the request at the time you send it and return a `415 Unsupported Media Type` before your method ever runs! You don't have to write any code to check whether the request is valid; JAX-RS will do that for you.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

Using a query parameter (i.e., GET /api/v1/sensors?type=Temperature) has advantages over putting the filter in the URL's path (i.e., /api/v1/sensors/type/Temperature) because of the following reasons: query parameters can be optional; hence, the same endpoint can serve requests for both all sensors as well as filtered sensors. In the case of putting the filter in the path as shown above, it can be read as if "type/Temperature" was its own resource; this does not make semantic sense. Path parameters are used to locate a single resource (e.g., /sensors/TEMP-001) while query parameters are used for filtering.

---

### Part 4.1 — Sub-Resource Locator Pattern

The subresource locator pattern means that instead of managing all of the reading endpoints directly within the SensorResource class, we simply create a new instance of SensorReadingResource for each request to the method annotated with @Path("/{sensorId}/readings"), and allow JAX-RS to route the request to that resource class for processing. As a result, there is a separate class to manage CRUD operations on sensors and another to manage readings associated with that sensor. If there were a single class managing both sensors and readings, it would have become quite long and difficult to maintain. Additionally, separating the two resources also allows them to be tested independently from one another more easily.

---

### Part 5.2 — Why 422 Instead of 404 for a Missing roomId

If a client attempts to create a sensor using an invalid roomId, the server should respond with 422 Unprocessable Entity, instead of a 404 Not Found. A 404 error indicates that the requested URL was not able to be found on the server, while the URL '/api/v1/sensors' is absolutely valid. The issue with this request is not the URL, but rather the request body, which contains an invalid roomId. A 422 Unprocessable Entity response would more accurately describe this error condition because the request body has been well-structured and interpreted by the server, but there is one or more invalid pieces of data within the body of the request.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing internal information, such as raw stack traces, when interfacing with API consumers is an issue of security. The names of all packages & classes within your application are visible to the consumer in the raw stack trace. If an attacker saw this they would learn how your application is built (i.e. its structure). This is as well as providing knowledge about any third-party libraries you are using and the version of them. This could provide an attacker with the ability to search for known vulnerabilities associated with any of the library versions. Additionally, an attacker may also obtain file paths from the server that are displayed within raw stack traces. Within this application project, the GlobalExceptionMapper catches any unhandled errors that occur and throws a generic message back to the consumer to prevent the above from being disclosed.

---

### Part 5.5 — Why Use Filters for Logging

Instead of logging using Logger calls in every resource method, it is preferable to use a JAX-RS filter for logging. By using this method, you adhere to the DRY principle as you only write the logic for logging once in LoggingFilter and the filter will apply to all requests and responses across the entire API. In addition, if you implement Logger calls separately in each resource method it is possible to miss some methods very easily, and if you change the log format then you have to update all of those resource methods that contain the logger call. Additionally, filters also log requests that fail before hitting the resource method (e.g. 404) due to the effect of the `@PreMatching` annotation, which means that all traffic to the API is fully viewable.

---
