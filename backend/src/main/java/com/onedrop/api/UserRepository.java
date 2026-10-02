package com.onedrop.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public ApiModels.UserResponse create(ApiModels.CreateUserRequest request) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO app_user(id,name,mobile_number,blood_group) VALUES (?,?,?,?)",
                id, request.name().trim(), request.mobileNumber().trim(), request.bloodGroup());
        return find(id);
    }

    public ApiModels.UserResponse find(UUID id) {
        return jdbc.queryForObject("""
                SELECT id,name,mobile_number,blood_group,available,latitude,longitude,location_updated_at
                FROM app_user WHERE id = ?""", (rs, row) -> new ApiModels.UserResponse(
                rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("mobile_number"),
                rs.getString("blood_group"), rs.getBoolean("available"),
                (Double) rs.getObject("latitude"), (Double) rs.getObject("longitude"),
                rs.getTimestamp("location_updated_at") == null ? null :
                        rs.getTimestamp("location_updated_at").toInstant()), id);
    }

    public void availability(UUID id, boolean available) {
        jdbc.update("UPDATE app_user SET available=? WHERE id=?", available, id);
    }

    public void location(UUID id, double latitude, double longitude) {
        jdbc.update("UPDATE app_user SET latitude=?, longitude=?, location_updated_at=? WHERE id=?",
                latitude, longitude, Timestamp.from(Instant.now()), id);
    }

    public java.util.List<ApiModels.MatchResponse> matches(ApiModels.RequestResponse request, int radiusKm, int maxAgeMinutes) {
        String sql = """
                SELECT id,name,blood_group,
                  ST_DistanceSphere(ST_MakePoint(longitude,latitude),
                                    ST_MakePoint(?,?))/1000 AS distance_km
                FROM app_user
                WHERE available = true AND blood_group = ?
                  AND location_updated_at > NOW() - (? * INTERVAL '1 minute')
                  AND ST_DWithin(ST_SetSRID(ST_MakePoint(longitude,latitude),4326)::geography,
                                 ST_SetSRID(ST_MakePoint(?,?),4326)::geography, ?)
                ORDER BY distance_km
                """;
        return jdbc.query(sql, new Object[]{request.longitude(), request.latitude(), request.bloodGroup(),
                        maxAgeMinutes, request.longitude(), request.latitude(), radiusKm * 1000},
                (rs, row) -> new ApiModels.MatchResponse(
                rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("blood_group"),
                rs.getDouble("distance_km")));
    }
}
