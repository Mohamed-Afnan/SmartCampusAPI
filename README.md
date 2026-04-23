# Smart Campus API

## Overview
The Smart Campus API is a RESTful web service developed using **JAX-RS**, **Apache Tomcat**, and **Maven**. It manages campus rooms, sensors, and historical sensor readings for a Smart Campus environment.

The API supports:

- Room management
- Sensor registration and room linking
- Sensor reading history tracking
- Sensor filtering by type
- Nested sub-resources for readings
- Structured JSON error handling
- Request and response logging

The system uses **in-memory Java data structures only**. No database and no Spring Boot are used, in line with the coursework specification.

## Project Structure
```text
smartcampusapi/
|
|-- nb-configuration.xml
|-- pom.xml
|
`-- src/main/
    |
    |-- java/com/example/smartcampusapi/
    |   |
    |   |-- config/
    |   |   `-- SmartCampusApplication.java
    |   |
    |   |-- model/
    |   |   |-- Room.java
    |   |   |-- Sensor.java
    |   |   |-- SensorReading.java
    |   |   `-- ApiError.java
    |   |
    |   |-- resources/
    |   |   |-- DiscoveryResource.java
    |   |   |-- SensorRoomResource.java
    |   |   |-- SensorResource.java
    |   |   `-- SensorReadingResource.java
    |   |
    |   |-- store/
    |   |   `-- SmartCampusStore.java
    |   |
    |   |-- exception/
    |   |   |-- RoomNotEmptyException.java
    |   |   |-- RoomNotEmptyExceptionMapper.java
    |   |   |-- LinkedResourceNotFoundException.java
    |   |   |-- LinkedResourceNotFoundExceptionMapper.java
    |   |   |-- SensorUnavailableException.java
    |   |   |-- SensorUnavailableExceptionMapper.java
    |   |   |-- WebApplicationExceptionMapper.java
    |   |   `-- ThrowableMapper.java
    |   |
    |   `-- filter/
    |       `-- ApiLoggingFilter.java
    |
    |-- resources/META-INF/
    |
    `-- webapp/
        |-- index.html
        |-- META-INF/context.xml
        `-- WEB-INF/web.xml
```

## Folder Description
- `config/` contains `SmartCampusApplication.java`, which configures the JAX-RS application using `/api/v1`.
- `model/` contains the POJO data models: `Room`, `Sensor`, `SensorReading`, and `ApiError`.
- `resources/` contains the REST endpoints.
- `store/` contains the in-memory data store using thread-safe collections.
- `exception/` contains custom exceptions and exception mappers.
- `filter/` contains the request and response logging filter.
- `webapp/` contains the Tomcat web application configuration.

## Setup and Run Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/smartcampusapi.git
cd smartcampusapi
```

### 2. Build the Project
Make sure Maven is installed, then run:

```bash
mvn clean install
```

### 3. Deploy to Server
Open the project in NetBeans.

Deploy to Apache Tomcat 9 or TomEE.

Ensure the application is running at:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

## Important Runtime Note
The application starts with empty collections. Rooms, sensors, and readings are created during runtime and stored in memory. After a real Tomcat restart, manually created data is reset.

This design follows the coursework rule that no database technology should be used.

## API Endpoints

### Discovery
```text
GET /api/v1
```

### Rooms
```text
GET    /api/v1/rooms
POST   /api/v1/rooms
GET    /api/v1/rooms/{roomId}
DELETE /api/v1/rooms/{roomId}
```

### Sensors
```text
GET  /api/v1/sensors
GET  /api/v1/sensors?type=CO2
POST /api/v1/sensors
GET  /api/v1/sensors/{sensorId}
```

### Sensor Readings
```text
GET  /api/v1/sensors/{sensorId}/readings
POST /api/v1/sensors/{sensorId}/readings
```

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -H "Accept: application/json" ^
  -d "{\"id\":\"ENG-300\",\"name\":\"Engineering Smart Lab\",\"capacity\":30}"
```

### 3. Get All Rooms
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 4. Create an Active Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -H "Accept: application/json" ^
  -d "{\"id\":\"CO2-ENG-300-01\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":418.2,\"roomId\":\"ENG-300\"}"
```

### 5. Filter Sensors by Type
```bash
curl "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 6. Add a Sensor Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-ENG-300-01/readings ^
  -H "Content-Type: application/json" ^
  -H "Accept: application/json" ^
  -d "{\"value\":421.7}"
```

## Error Handling
The API uses custom exception mappers to return structured JSON error responses instead of raw Java stack traces.

Handled error scenarios include:

- `400 Bad Request` for invalid payloads
- `404 Not Found` for missing rooms or sensors
- `409 Conflict` for deleting a room that still has sensors
- `422 Unprocessable Entity` for linking a sensor to a room that does not exist
- `403 Forbidden` for posting readings to a sensor in `MAINTENANCE`
- `500 Internal Server Error` for unexpected server-side failures

## Logging
A custom JAX-RS filter logs every request and response.

Logged information includes:

- Incoming HTTP method
- Incoming request URI
- Outgoing response status code

Example:

```text
Incoming request: POST http://localhost:8080/SmartCampusAPI/api/v1/sensors
Outgoing response: POST http://localhost:8080/SmartCampusAPI/api/v1/sensors -> 201
```

## Report Answers

## Part 1: Service Architecture and Setup

### 1. Project and Application Configuration - JAX-RS Resource Lifecycle
In JAX-RS, resource classes follow a per-request lifecycle by default. This means a new instance of the resource class is usually created for each incoming HTTP request. Therefore, resource classes are not treated as singletons unless they are explicitly configured that way.

This lifecycle improves safety because each request works with its own resource object. However, the Smart Campus API needs data to remain available across multiple requests. For that reason, the application does not store the main data inside resource class instance variables. Instead, it stores shared data in `SmartCampusStore`.

The store uses in-memory data structures such as maps for rooms, sensors, and readings. Since several clients may send requests at the same time, shared data can be accessed concurrently. To reduce the risk of race conditions and inconsistent updates, the implementation uses `ConcurrentHashMap` and synchronized blocks for operations that update related data together.

For example, when a new sensor is registered, the sensor must be added to the sensor collection and its ID must also be linked to the correct room. Synchronization helps keep these related updates consistent.


### 2. Discovery Endpoint - HATEOAS
Hypermedia, also known as HATEOAS, is considered an advanced feature of RESTful design because it allows an API response to guide the client through available resources using links. Instead of forcing the client to know every endpoint in advance, the API can return useful navigation links in the response.

In this project, the discovery endpoint at `/api/v1` returns API metadata such as the API name, version, contact details, and links to the main resource collections such as rooms and sensors.

This is helpful for client developers because they do not need to depend only on static documentation. They can inspect the discovery response and understand which resources are available. It also reduces mistakes when endpoint paths change in the future, because clients can follow the links provided by the API.


## Part 2: Room Management

### 1. Room Resource Implementation - Returning IDs vs Full Objects
Returning only room IDs reduces the size of the response. This can save bandwidth, especially if the system contains many rooms. However, it also means the client may need to make extra requests to retrieve full details for each room.

Returning full room objects requires a larger response, but it makes client-side development simpler because the client receives all useful room information in one request.

In this Smart Campus API, full room objects are returned because the data size is small and it makes testing, Postman demonstration, and client interaction clearer.


### 2. Room Deletion and Safety Logic - DELETE Idempotency
The `DELETE` operation in this implementation is idempotent in terms of the final state of the system. If a room exists and has no sensors assigned to it, the first `DELETE` request removes the room.

If the same `DELETE` request is sent again, the room no longer exists, so the API returns `404 Not Found`. Although the response status changes after the first request, the final state of the system remains the same because the room is still absent.

Therefore, the operation is still considered idempotent because repeated identical requests do not create additional side effects after the first successful deletion.


## Part 3: Sensor Management

### 1. Sensor Resource and Integrity - `@Consumes` and Media Type Mismatch
The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the method expects a JSON request body. This is used on methods such as `POST /sensors` and `POST /rooms` to make sure the API receives data in the correct format.

If a client sends data using another media type, such as `text/plain` or `application/xml`, JAX-RS may reject the request because the method is designed to consume JSON. In this situation, the server can respond with `415 Unsupported Media Type` or a related request parsing error before the resource method is executed.

This protects the API contract because the server only processes request bodies that match the expected JSON format.


### 2. Filtered Retrieval and Search - `@QueryParam` vs `@PathParam`
Using `@QueryParam` for filtering is more flexible for collection resources. For example, `/api/v1/sensors?type=CO2` clearly means the client wants to retrieve sensors from the sensor collection, but only those matching the `CO2` type.

If the filter is placed inside the path, such as `/api/v1/sensors/type/CO2`, the URL becomes less flexible because it treats the filter like part of the resource hierarchy. This becomes harder to manage when more filters are added later.

Query parameters are generally better for searching and filtering because they are optional, easy to combine, and do not require new endpoint paths for every filtering condition.


## Part 4: Sensor Readings

### 1. Deep Nesting with Sub-Resource Locator - Benefits
The sub-resource locator pattern allows nested resources such as `/sensors/{sensorId}/readings` to be delegated to a separate resource class. In this project, `SensorResource` handles the main sensor endpoints, while `SensorReadingResource` handles the reading history for a specific sensor.

This design improves separation of responsibility. `SensorResource` focuses on sensor registration and retrieval, while `SensorReadingResource` focuses on adding and retrieving readings.

If all nested paths were implemented inside one large resource class, the code would become harder to read, maintain, and extend. By using a separate sub-resource class, the API structure remains cleaner and closer to the real resource hierarchy.


### 2. Historical Data Management - Updating `currentValue`
The Smart Campus API stores historical readings for each sensor. When a client posts a new reading to `/sensors/{sensorId}/readings`, the reading is added to that sensor's reading history.

A successful reading post also updates the parent sensor's `currentValue`. This is important because the sensor resource should always show the latest measurement without requiring the client to manually inspect the full reading history.

This keeps the sensor data consistent across the API. The reading history stores past values, while the sensor's `currentValue` shows the most recent value.


## Part 5: Advanced Error Handling, Exception Mapping and Logging

### 1. Resource Conflict - RoomNotEmptyException
The API prevents a room from being deleted if it still has sensors assigned to it. This rule avoids orphaned sensor records that refer to a room that no longer exists.

When a client sends `DELETE /rooms/{roomId}` for a room that still contains sensors, the application throws `RoomNotEmptyException`. The exception mapper converts this into a structured JSON response with HTTP `409 Conflict`.

This status code is suitable because the request conflicts with the current state of the resource.


### 2. Dependency Validation - HTTP 422 and 404
HTTP `422 Unprocessable Entity` is more appropriate than `404 Not Found` when the request URL is valid but a reference inside the JSON payload is invalid.

For example, `POST /sensors` is a valid endpoint, and the JSON structure may be correct. However, if the `roomId` in the request body refers to a room that does not exist, the server cannot process the request logically.

A `404` usually means the requested URL or resource itself could not be found. In this case, the endpoint exists, but the linked room inside the payload does not. Therefore, `422` communicates the problem more accurately.


### 3. State Constraint - SensorUnavailableException
If a sensor has the status `MAINTENANCE`, it represents a device that is unavailable and cannot accept new readings. When a client tries to post a reading to a maintenance sensor, the API throws `SensorUnavailableException`.

This exception is mapped to HTTP `403 Forbidden` because the client is calling a valid endpoint, but the current state of the sensor does not allow the requested operation.

The response is returned as structured JSON, so the client receives a clear reason instead of a generic server error.


### 4. Global Safety Net - Hiding Stack Traces
Exposing internal Java stack traces through an API is dangerous because it can reveal technical details about the application. A stack trace may show package names, class names, file paths, method names, framework details, and server-side logic.

Attackers can use this information to understand how the system is built and search for weaknesses. It may also reveal details about libraries, frameworks, or deployment structure, such as Jersey or Tomcat.

For this reason, the API includes a global exception mapper. Unexpected errors are converted into a generic HTTP `500 Internal Server Error` JSON response, while the detailed exception is logged on the server side instead of being exposed to the client.


### 5. API Request and Response Logging Filters
JAX-RS filters are useful for logging because logging is a cross-cutting concern. It should apply to every request and response, not just one endpoint.

Using a filter allows the application to log the incoming HTTP method and URI, as well as the outgoing response status code, from one central place. This avoids repeating `Logger.info()` statements inside every resource method.

This approach improves maintainability because any logging change only needs to be made in one class. It also reduces the chance of forgetting to add logging when new endpoints are created.

## Author
Name: Afnan Unais
UOW & IIT ID: w2120190 / 20231520
Module: 5COSC022C.2 Client-Server Architectures
