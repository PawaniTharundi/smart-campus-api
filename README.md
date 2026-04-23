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

By default JAX-RS creates a new instance of each resource class for every single HTTP request that comes in. This is called the per-request lifecycle. Because of this, you can't store shared data inside a resource class as an instance field — it would just be lost after each request finishes.

To get around this I used a Singleton DataStore class. It gets created once when the application starts and the same instance is shared across every request through `DataStore.getInstance()`. I also used ConcurrentHashMap instead of a regular HashMap because multiple requests can come in at the same time, and ConcurrentHashMap handles concurrent reads and writes safely without needing manual synchronization.

---

### Part 1.2 — HATEOAS

HATEOAS stands for Hypermedia as the Engine of Application State. The basic idea is that instead of a client needing to know all the API URLs upfront from a document, the API itself includes links in its responses that tell the client where to go next.

In this project the discovery endpoint at `GET /api/v1` returns links to `/api/v1/rooms` and `/api/v1/sensors`. A client can start at the root and find everything from there without needing external documentation. This also means if the URL structure ever changes, clients that follow the links will still work rather than breaking because they had URLs hard-coded.

---

### Part 2.1 — Returning IDs vs Full Objects

I chose to return full room objects in the list response rather than just IDs. If only IDs were returned, the client would have to send a separate GET request for each room to get its name, capacity and sensor list. For even a modest number of rooms this adds up to a lot of unnecessary requests — this is known as the N+1 problem. Returning full objects means the client gets everything it needs in one call. For the scale of this project (a university campus) this is the more practical approach.

---

### Part 2.2 — Is DELETE Idempotent?

Yes, DELETE is idempotent in terms of server state. The first time you send `DELETE /api/v1/rooms/LAB-101` the room gets removed and you get back 204 No Content. If you send the exact same request again the room is already gone so you get 404 Not Found. The response code is different but the important thing is the server ends up in the same state both times — the room does not exist. This is the accepted behaviour for idempotent operations in REST.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS to only allow requests that have `Content-Type: application/json` in the header. If a client sends `text/plain` or `application/xml` instead, JAX-RS will automatically reject it and return 415 Unsupported Media Type before the method even runs. I don't need to write any code to check this myself — the framework handles it automatically.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

Using a query parameter like `GET /api/v1/sensors?type=Temperature` is better than putting the filter in the path like `/api/v1/sensors/type/Temperature` for a couple of reasons. Query parameters are optional by design, so the same endpoint works for both getting all sensors and getting filtered results. A path like `/sensors/type/Temperature` implies that "type/Temperature" is its own resource which doesn't make semantic sense. Path parameters are meant for identifying a specific resource (like `/sensors/TEMP-001`), not for filtering a collection.

---

### Part 4.1 — Sub-Resource Locator Pattern

The sub-resource locator pattern means that instead of handling all the reading endpoints directly in SensorResource, the method annotated with `@Path("/{sensorId}/readings")` just returns a new SensorReadingResource object and JAX-RS passes the request to that class to handle. This keeps each class focused on its own job — SensorResource deals with sensor CRUD and SensorReadingResource deals with readings. If everything was in one class it would get very long and hard to manage. It also makes it easier to test each part separately.

---

### Part 5.2 — Why 422 Instead of 404 for a Missing roomId

When a client tries to register a sensor with a roomId that doesn't exist, the right response is 422 Unprocessable Entity rather than 404. A 404 means the URL that was requested couldn't be found, but in this case the URL `/api/v1/sensors` is completely valid. The problem is with the content of the JSON body — it references a room that doesn't exist. HTTP 422 means the request was understood and well-formed but the server can't process it because of invalid data inside it. That describes this situation much more accurately.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Returning raw stack traces to API consumers is a security problem because they give away too much internal information. They show the full package and class names of your application which helps an attacker understand how your code is structured. They also reveal which third-party libraries you're using and their exact versions, which makes it easy to look up known vulnerabilities for those versions. Sometimes file paths on the server also appear in stack traces. The GlobalExceptionMapper in this project catches all unexpected errors and only returns a generic error message so none of this gets exposed.

---

### Part 5.5 — Why Use Filters for Logging

Using a JAX-RS filter for logging is better than manually adding Logger calls inside every resource method. The main reason is the DRY principle — the logic is written once in LoggingFilter and automatically covers every request and response in the whole API. If I had added Logger calls in each method individually it would be very easy to miss some methods, and any time the log format needed changing I would have to update many files. Filters also run even for requests that fail before reaching a resource method (like 404 errors) because of the `@PreMatching` annotation, so you get complete visibility of everything that hits the API.

---
