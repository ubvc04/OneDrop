package com.onedrop.api;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.UUID;

public final class ApiModels {
    private ApiModels() {}

    public record CreateUserRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Pattern(regexp = "^[+0-9 ()-]{7,30}$") String mobileNumber,
            @NotBlank @Pattern(regexp = "^(A|B|AB|O)[+-]$") String bloodGroup) {}

    public record UpdateAvailabilityRequest(@NotNull Boolean available) {}

    public record UpdateLocationRequest(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {}

    public record CreateRequest(
            @NotNull UUID requesterId,
            @NotBlank @Pattern(regexp = "^(A|B|AB|O)[+-]$") String bloodGroup,
            @NotNull @Min(1) @Max(20) Integer unitsRequired,
            @NotBlank @Size(max = 240) String hospital,
            @NotBlank @Pattern(regexp = "^(STANDARD|URGENT|EMERGENCY)$") String urgency,
            @Size(max = 2000) String additionalInformation,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {}

    public record UserResponse(UUID id, String name, @JsonIgnore String mobileNumber, String bloodGroup,
                               boolean available, @JsonIgnore Double latitude, @JsonIgnore Double longitude,
                               @JsonIgnore Instant locationUpdatedAt) {}

    public record RequestResponse(UUID id, @JsonIgnore UUID requesterId, String bloodGroup, int unitsRequired,
                                  String hospital, String urgency, String additionalInformation,
                                  Double latitude, Double longitude, String status, Instant expiresAt) {}

    public record MatchResponse(UUID id, String name, String bloodGroup, double distanceKm) {}
}
