CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    mobile_number VARCHAR(30) NOT NULL UNIQUE,
    blood_group VARCHAR(3) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS blood_request (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES app_user(id),
    blood_group VARCHAR(3) NOT NULL,
    units_required INTEGER NOT NULL CHECK (units_required > 0 AND units_required <= 20),
    hospital VARCHAR(240) NOT NULL,
    urgency VARCHAR(20) NOT NULL,
    additional_information VARCHAR(2000),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS app_user_location_idx ON app_user (latitude, longitude);
CREATE INDEX IF NOT EXISTS blood_request_status_idx ON blood_request (status, expires_at);
