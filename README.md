# Smart Campus — Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures
**Student:** Pawani Tharundi
**Student ID:** 20241212 / w2119802
**University:** University of Westminster
**Year:** 2025/26

---

## Overview

This project is a RESTful web service built using **JAX-RS (Jersey)** and an embedded **Grizzly HTTP server**. It provides a backend API for the University's "Smart Campus" initiative, allowing facilities managers and automated building systems to manage Rooms and Sensors across the campus.

The API supports full CRUD operations for Rooms and Sensors, a sub-resource pattern for Sensor Readings, advanced error handling with custom Exception Mappers, and request/response logging via JAX-RS Filters. All data is stored in-memory using `ConcurrentHashMap` and `ArrayList` — no database is used.

### Base URL
```
http://localhost:8080/api/v1
```

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── smartcampus/
                    ├── Main.java                          ← starts the server
                    ├── SmartCampusApplication.java        ← JAX-RS app config
                    ├── model/
                    │   ├── Room.java
                    │   ├── Sensor.java
                    │   └── SensorReading.java
                    ├── store/
                    │   └── DataStore.java                 ← in-memory singleton
                    ├── resource/
                    │   ├── DiscoveryResource.java         ← GET /api/v1
                    │   ├── RoomResource.java              ← /api/v1/rooms
                    │   ├── SensorResource.java            ← /api/v1/sensors
                    │   └── SensorReadingResource.java     ← /api/v1/sensors/{id}/readings
                    ├── exception/
                    │   ├── RoomNotEmptyException.java
                    │   ├── RoomNotEmptyMapper.java
                    │   ├── LinkedResourceNotFoundException.java
                    │   ├── LinkedResourceMapper.java
                    │   ├── SensorUnavailableException.java
                    │   ├── SensorUnavailableMapper.java
                    │   └── GlobalExceptionMapper.java
                    └── filter/
                        └── LoggingFilter.java
```

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 11 | Programming language |
| JAX-RS (Jakarta RESTful Web Services) | REST framework |
| Jersey 2.39.1 | JAX-RS implementation |
| Grizzly HTTP Server | Embedded server |
| Jackson | JSON serialization |
| Maven | Build tool |
| ConcurrentHashMap / ArrayList | In-memory data storage |

---

## How to Build and Run the Project

### Prerequisites
Make sure you have these installed before starting:
- **Java JDK 11** or higher
- **Apache Maven 3.6** or higher
- **NetBeans IDE 22** (or any IDE with Maven support)

### Step 1 — Clone or download the project
Download the project and open it in NetBeans:
- Open NetBeans
- Click **File → Open Project**
- Select the `smart-campus-api` project folder
- Click **Open Project**

### Step 2 — Build the project
In NetBeans:
- Click **Run** in the top menu
- Click **Clean and Build Project**
- Wait for the Output panel to show: `BUILD SUCCESS`

Or using the terminal:
```bash
mvn clean package
```

### Step 3 — Run the server
In NetBeans:
- Press the green **Play button**
- Watch the Output panel at the bottom

You should see:
```
===========================================
Smart Campus API is running!
URL: http://localhost:8080/api/v1
Press ENTER to stop the server.
===========================================
```

### Step 4 — Test the API
Open your browser and go to:
```
http://localhost:8080/api/v1
```
You should see a JSON response with API metadata. The server is now ready to accept requests.

### Step 5 — Stop the server
- In NetBeans Output panel, click inside it and press **Enter**
- The server will stop

---

## API Design Overview

The API follows REST architectural principles:
- Resources are identified by meaningful URLs (`/rooms`, `/sensors`)
- HTTP methods indicate the action (`GET`, `POST`, `PUT`, `DELETE`)
- HTTP status codes communicate the result (`200`, `201`, `204`, `400`, `403`, `404`, `409`, `422`, `500`)
- All responses are in **JSON format**
- Sub-resources are used for nested data (`/sensors/{id}/readings`)

### Pre-loaded Sample Data
The API starts with this data already in memory:

| Type | ID | Details |
|---|---|---|
| Room | LIB-301 | Library Quiet Study, capacity 50 |
| Room | LAB-101 | Computer Lab, capacity 30 |
| Sensor | TEMP-001 | Temperature, ACTIVE, in LIB-301 |
| Sensor | HUM-001 | Humidity, ACTIVE, in LIB-301 |
| Sensor | TEMP-002 | Temperature, ACTIVE, in LAB-101 |

---

## API Endpoints Reference

### Part 1 — Discovery

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1` | Returns API metadata, version, contact, and resource links |

---

### Part 2 — Room Management

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/rooms` | Get all rooms | 200 |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| PUT | `/api/v1/rooms/{roomId}` | Update an existing room | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (only if no sensors assigned) | 204 |

---

### Part 3 — Sensor Operations

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors` | Get all sensors (optional `?type=` filter) | 200 |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor | 200 |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId exists) | 201 |
| PUT | `/api/v1/sensors/{sensorId}` | Update a sensor | 200 |
| DELETE | `/api/v1/sensors/{sensorId}` | Delete a sensor | 204 |

---

### Part 4 — Sensor Readings (Sub-Resource)

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor | 200 |
| GET | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Get a specific reading | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (updates sensor's currentValue) | 201 |
| DELETE | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Delete a reading | 204 |

---

## Sample curl Commands

### 1 — Get API discovery information
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2 — Get all rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 3 — Create a new room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"HALL-A1\",\"name\":\"Main Hall\",\"capacity\":100}"
```

### 4 — Get all Temperature sensors (filtered)
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5 — Create a new sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400,\"roomId\":\"LAB-101\"}"
```

### 6 — Post a new reading to a sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":25.5}"
```

### 7 — Get all readings for a sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 8 — Delete a sensor
```bash
curl -X DELETE http://localhost:8080/api/v1/sensors/TEMP-001
```

### 9 — Delete a room (will fail with 409 if sensors still assigned)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

---

## Error Handling

The API never exposes raw Java stack traces. All errors return structured JSON.

| HTTP Status | Exception Class | Scenario |
|---|---|---|
| 409 Conflict | `RoomNotEmptyException` | Deleting a room that still has sensors |
| 422 Unprocessable Entity | `LinkedResourceNotFoundException` | Creating a sensor with a roomId that does not exist |
| 403 Forbidden | `SensorUnavailableException` | Posting a reading to a MAINTENANCE or OFFLINE sensor |
| 500 Internal Server Error | `GlobalExceptionMapper` | Any unexpected runtime error |

### Example error responses

**409 Conflict:**
```json
{
  "error": "409 Conflict",
  "message": "Room LIB-301 still has 2 sensor(s) assigned. Remove all sensors before deleting this room."
}
```

**422 Unprocessable Entity:**
```json
{
  "error": "422 Unprocessable Entity",
  "message": "Room not found: FAKE-999. The roomId in the request body references a room that does not exist."
}
```

**403 Forbidden:**
```json
{
  "error": "403 Forbidden",
  "message": "Sensor TEMP-002 is currently MAINTENANCE and cannot accept new readings."
}
```

---

## Report — Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of each Resource class for every incoming HTTP request** (request-scoped lifecycle). This means every request gets its own fresh object. Because of this, resource classes cannot store shared state in instance variables — if they did, each request would start with an empty object and data would be lost.

To solve this, this project uses a **Singleton DataStore** (`DataStore.getInstance()`). The DataStore is created once when the application starts and shared across all requests. It uses `ConcurrentHashMap` instead of a regular `HashMap` to prevent race conditions — if two requests arrive at the same time and both try to write data, `ConcurrentHashMap` handles this safely without corrupting the data.

---

### Part 1.2 — HATEOAS (Hypermedia as the Engine of Application State)

HATEOAS is a REST constraint where API responses include links that guide the client to related resources. Instead of a client needing to memorise all URLs from static documentation, the API itself tells the client where to go next.

For example, the discovery endpoint at `GET /api/v1` returns links to `/api/v1/rooms` and `/api/v1/sensors`. A client that has never used the API before can discover all available resources just by calling the root endpoint. This makes the API **self-documenting** and reduces the risk of clients using outdated or wrong URLs. It also allows the server to change URL structures without breaking clients, as long as the links in responses are updated.

---

### Part 2.1 — Returning IDs vs Full Objects in Lists

When returning a list of rooms, the API returns **full room objects** rather than just IDs. Returning only IDs would force the client to make a separate GET request for every single room to get its details — this is called the N+1 problem and wastes network bandwidth with many round trips. Returning full objects in one response is more efficient for the client, especially when the client needs to display all room details at once. However, for very large datasets, returning full objects increases the response payload size, so pagination or field filtering could be considered as a future improvement.

---

### Part 2.2 — Is DELETE Idempotent?

In this implementation, DELETE is **partially idempotent**. The first DELETE request on a room that exists and has no sensors returns `204 No Content` — the room is removed. If the exact same DELETE request is sent again for the same room ID, the server returns `404 Not Found` because the room no longer exists.

Strictly speaking, a truly idempotent DELETE would return the same status code every time. However, the important thing is that the **server state is identical after both calls** — the room is gone either way. Most REST API designs accept this behaviour as idempotent in terms of server state, even if the response code differs between the first and subsequent calls.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST method only accepts requests with `Content-Type: application/json`. If a client sends data with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS will automatically reject the request and return **415 Unsupported Media Type** before the method even executes. The developer does not need to write any code to handle this — JAX-RS enforces it automatically at the framework level.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

Using `@QueryParam("type")` for filtering (e.g. `GET /api/v1/sensors?type=Temperature`) is the correct REST design. Query parameters are optional by nature — without the parameter, the endpoint returns all sensors; with it, results are filtered. This keeps the base resource URL clean and stable (`/api/v1/sensors`).

If the type were part of the path (e.g. `/api/v1/sensors/type/Temperature`), it would imply that "type/Temperature" is a distinct resource, which is semantically incorrect. Path parameters should only be used for identifying a specific resource (like `/sensors/TEMP-001`). Query parameters are intended for filtering, searching, sorting, and pagination of collections.

---

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern delegates handling of a nested path to a separate class. In `SensorResource`, the method annotated with `@Path("/{sensorId}/readings")` does not handle the request itself — it returns an instance of `SensorReadingResource`, which handles all the reading operations.

This is beneficial for large APIs because it keeps each class focused on one responsibility (Single Responsibility Principle). If all nested paths were defined in one massive class, the file would become very long and hard to maintain. Separate classes are easier to test independently, easier to read, and can be developed by different team members without conflicts.

---

### Part 5.2 — Why 422 Instead of 404 for Missing roomId Reference

When a client POSTs a new sensor with a `roomId` that does not exist, the correct response is **422 Unprocessable Entity** rather than 404 Not Found. The reason is that 404 means "the URL you requested was not found" — but in this case, the URL `/api/v1/sensors` is perfectly valid and was found. The problem is not the URL but the **content of the JSON payload** — it contains a reference to a room that does not exist. HTTP 422 specifically means "the request was well-formed but the server cannot process the instructions it contains", which is exactly this situation.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a serious security risk for several reasons. A stack trace reveals the **internal package structure and class names** of the application, which helps an attacker understand how the code is organised. It also exposes **library names and version numbers** (e.g. Jersey 2.39.1, Grizzly), which allows attackers to look up known vulnerabilities for those specific versions. Stack traces can also reveal **file paths on the server**, **database query logic**, and **business logic flow**, all of which give an attacker a detailed map of how to exploit the system. The `GlobalExceptionMapper` in this project catches all unexpected errors and returns only a generic "An unexpected error occurred" message, hiding all internal details from the consumer.

---

### Part 5.5 — Why Use Filters for Cross-Cutting Concerns

Using JAX-RS filters (`ContainerRequestFilter` and `ContainerResponseFilter`) for logging is better than manually adding `Logger.info()` inside every resource method for several reasons. First, it follows the **DRY principle** (Don't Repeat Yourself) — the logging logic is written once in `LoggingFilter` and automatically applies to every single request and response without touching any resource class. Second, if the logging format needs to change, only one file needs to be updated. Third, resource methods stay clean and focused on business logic only. Fourth, filters run even for requests that fail early (e.g. 404 or 405 errors) because `@PreMatching` is used, ensuring complete observability across the entire API.

---

## Logging

All incoming requests and outgoing responses are logged to the console using `java.util.logging.Logger`. The log format is:

```
>>> REQUEST:  GET http://localhost:8080/api/v1/rooms
<<< RESPONSE: GET http://localhost:8080/api/v1/rooms → HTTP 200
```

---

## Author

**Pawani Tharundi**
University of Westminster
Module: 5COSC022W Client-Server Architectures
Academic Year: 2025/26
