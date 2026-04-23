package com.example.smartcampusapi.store;

import com.example.smartcampusapi.exception.LinkedResourceNotFoundException;
import com.example.smartcampusapi.exception.RoomNotEmptyException;
import com.example.smartcampusapi.exception.SensorUnavailableException;
import com.example.smartcampusapi.model.Room;
import com.example.smartcampusapi.model.Sensor;
import com.example.smartcampusapi.model.SensorReading;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public final class SmartCampusStore {

    private static final SmartCampusStore INSTANCE = new SmartCampusStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<String, Room>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<String, Sensor>();
    private final Map<String, List<SensorReading>> sensorReadings =
            new ConcurrentHashMap<String, List<SensorReading>>();

    private final Object roomLock = new Object();
    private final Object sensorLock = new Object();

    private SmartCampusStore() {
    }

    public static SmartCampusStore getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getId))
                .map(this::copyRoom)
                .collect(Collectors.toList());
    }

    public Room getRoomOrThrow(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room '" + roomId + "' was not found.");
        }
        return copyRoom(room);
    }

    public Room createRoom(Room room) {
        validateRoom(room);
        synchronized (roomLock) {
            if (rooms.containsKey(room.getId())) {
                throw new ClientErrorException(
                        "Room '" + room.getId() + "' already exists.",
                        Response.Status.CONFLICT);
            }
            Room stored = copyRoom(room);
            if (stored.getSensorIds() == null) {
                stored.setSensorIds(new ArrayList<String>());
            }
            rooms.put(stored.getId(), stored);
            return copyRoom(stored);
        }
    }

    public void deleteRoom(String roomId) {
        synchronized (roomLock) {
            Room room = rooms.get(roomId);
            if (room == null) {
                throw new NotFoundException("Room '" + roomId + "' was not found.");
            }
            if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
                throw new RoomNotEmptyException(
                        "Room '" + roomId + "' cannot be deleted because sensors are still assigned to it.");
            }
            rooms.remove(roomId);
        }
    }

    public List<Sensor> getAllSensors(String type) {
        return sensors.values().stream()
                .filter(sensor -> type == null
                        || type.trim().isEmpty()
                        || sensor.getType().equalsIgnoreCase(type.trim()))
                .sorted(Comparator.comparing(Sensor::getId))
                .map(this::copySensor)
                .collect(Collectors.toList());
    }

    public Sensor getSensorOrThrow(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' was not found.");
        }
        return copySensor(sensor);
    }

    public Sensor createSensor(Sensor sensor) {
        validateSensor(sensor);
        synchronized (sensorLock) {
            if (sensors.containsKey(sensor.getId())) {
                throw new ClientErrorException(
                        "Sensor '" + sensor.getId() + "' already exists.",
                        Response.Status.CONFLICT);
            }

            Room room = rooms.get(sensor.getRoomId());
            if (room == null) {
                throw new LinkedResourceNotFoundException(
                        "Cannot register sensor '" + sensor.getId()
                                + "' because room '" + sensor.getRoomId() + "' does not exist.");
            }

            Sensor stored = copySensor(sensor);
            sensors.put(stored.getId(), stored);
            sensorReadings.putIfAbsent(stored.getId(), Collections.synchronizedList(new ArrayList<SensorReading>()));

            synchronized (roomLock) {
                Room linkedRoom = rooms.get(stored.getRoomId());
                if (linkedRoom.getSensorIds() == null) {
                    linkedRoom.setSensorIds(new ArrayList<String>());
                }
                if (!linkedRoom.getSensorIds().contains(stored.getId())) {
                    linkedRoom.getSensorIds().add(stored.getId());
                }
            }
            return copySensor(stored);
        }
    }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        getSensorOrThrow(sensorId);
        List<SensorReading> readings = sensorReadings.get(sensorId);
        if (readings == null) {
            return new ArrayList<SensorReading>();
        }
        synchronized (readings) {
            return readings.stream()
                    .sorted(Comparator.comparingLong(SensorReading::getTimestamp))
                    .map(this::copyReading)
                    .collect(Collectors.toList());
        }
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        if (reading == null) {
            throw new BadRequestException("Reading payload is required.");
        }

        synchronized (sensorLock) {
            Sensor sensor = sensors.get(sensorId);
            if (sensor == null) {
                throw new NotFoundException("Sensor '" + sensorId + "' was not found.");
            }

            if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
                throw new SensorUnavailableException(
                        "Sensor '" + sensorId + "' is currently in MAINTENANCE and cannot accept new readings.");
            }

            SensorReading stored = copyReading(reading);
            if (stored.getId() == null || stored.getId().trim().isEmpty()) {
                stored.setId(UUID.randomUUID().toString());
            }
            if (stored.getTimestamp() <= 0L) {
                stored.setTimestamp(System.currentTimeMillis());
            }

            List<SensorReading> readings = sensorReadings.computeIfAbsent(
                    sensorId,
                    key -> Collections.synchronizedList(new ArrayList<SensorReading>()));

            readings.add(stored);
            sensor.setCurrentValue(stored.getValue());
            return copyReading(stored);
        }
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new BadRequestException("Room payload is required.");
        }
        if (isBlank(room.getId())) {
            throw new BadRequestException("Room id is required.");
        }
        if (isBlank(room.getName())) {
            throw new BadRequestException("Room name is required.");
        }
        if (room.getCapacity() <= 0) {
            throw new BadRequestException("Room capacity must be greater than zero.");
        }
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null) {
            throw new BadRequestException("Sensor payload is required.");
        }
        if (isBlank(sensor.getId())) {
            throw new BadRequestException("Sensor id is required.");
        }
        if (isBlank(sensor.getType())) {
            throw new BadRequestException("Sensor type is required.");
        }
        if (isBlank(sensor.getStatus())) {
            throw new BadRequestException("Sensor status is required.");
        }
        if (isBlank(sensor.getRoomId())) {
            throw new BadRequestException("Sensor roomId is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Room copyRoom(Room room) {
        return new Room(room.getId(), room.getName(), room.getCapacity(), room.getSensorIds());
    }

    private Sensor copySensor(Sensor sensor) {
        return new Sensor(sensor.getId(), sensor.getType(), sensor.getStatus(),
                sensor.getCurrentValue(), sensor.getRoomId());
    }

    private SensorReading copyReading(SensorReading reading) {
        return new SensorReading(reading.getId(), reading.getTimestamp(), reading.getValue());
    }
}
