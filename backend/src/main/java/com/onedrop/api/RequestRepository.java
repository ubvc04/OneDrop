package com.onedrop.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class RequestRepository {
    private final JdbcTemplate jdbc;
    public RequestRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public ApiModels.RequestResponse create(ApiModels.CreateRequest r) {
        int createdToday = jdbc.queryForObject("""
                SELECT COUNT(*) FROM blood_request
                WHERE requester_id = ? AND created_at >= CURRENT_DATE""", Integer.class, r.requesterId());
        if (createdToday >= 5) {
            throw new RequestLimitExceededException("request limit reached; try again tomorrow");
        }
        int activeCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM blood_request
                WHERE requester_id = ? AND status NOT IN ('FULFILLED','EXPIRED')
                  AND expires_at > NOW()""", Integer.class, r.requesterId());
        if (activeCount >= 3) {
            throw new RequestLimitExceededException("maximum active request limit reached");
        }
        UUID id = UUID.randomUUID();
        Instant expires = Instant.now().plusSeconds(24 * 60 * 60);
        jdbc.update("""
                INSERT INTO blood_request
                (id,requester_id,blood_group,units_required,hospital,urgency,additional_information,
                 latitude,longitude,expires_at) VALUES (?,?,?,?,?,?,?,?,?,?)""",
                id, r.requesterId(), r.bloodGroup(), r.unitsRequired(), r.hospital().trim(), r.urgency(),
                r.additionalInformation(), r.latitude(), r.longitude(), Timestamp.from(expires));
        return find(id);
    }

    public int expireDueRequests() {
        return jdbc.update("""
                UPDATE blood_request SET status = 'EXPIRED'
                WHERE status NOT IN ('FULFILLED','EXPIRED') AND expires_at <= NOW()""");
    }

    public ApiModels.RequestResponse find(UUID id) {
        return jdbc.queryForObject("""
                SELECT id,requester_id,blood_group,units_required,hospital,urgency,
                additional_information,latitude,longitude,status,expires_at FROM blood_request WHERE id=?""",
                (rs, row) -> new ApiModels.RequestResponse(rs.getObject("id", UUID.class),
                        rs.getObject("requester_id", UUID.class), rs.getString("blood_group"),
                        rs.getInt("units_required"), rs.getString("hospital"), rs.getString("urgency"),
                        rs.getString("additional_information"), rs.getDouble("latitude"),
                        rs.getDouble("longitude"), rs.getString("status"),
                        rs.getTimestamp("expires_at").toInstant()), id);
    }

    public java.util.List<ApiModels.RequestResponse> active() {
        return jdbc.query("""
                SELECT id,requester_id,blood_group,units_required,hospital,urgency,
                additional_information,latitude,longitude,status,expires_at
                FROM blood_request WHERE status NOT IN ('FULFILLED','EXPIRED') AND expires_at > NOW()
                ORDER BY created_at DESC""", (rs, row) -> new ApiModels.RequestResponse(
                rs.getObject("id", UUID.class), rs.getObject("requester_id", UUID.class),
                rs.getString("blood_group"), rs.getInt("units_required"), rs.getString("hospital"),
                rs.getString("urgency"), rs.getString("additional_information"), rs.getDouble("latitude"),
                rs.getDouble("longitude"), rs.getString("status"), rs.getTimestamp("expires_at").toInstant()));
    }
}
