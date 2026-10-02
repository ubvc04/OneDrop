package com.onedrop.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository users;
    private final RequestRepository requests;
    public ApiController(UserRepository users, RequestRepository requests) {
        this.users = users; this.requests = requests;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.UserResponse createUser(@Valid @RequestBody ApiModels.CreateUserRequest request) {
        return users.create(request);
    }

    @PatchMapping("/users/{id}/availability")
    public ApiModels.UserResponse availability(@PathVariable UUID id,
                                                @Valid @RequestBody ApiModels.UpdateAvailabilityRequest request) {
        users.availability(id, request.available()); return users.find(id);
    }

    @PatchMapping("/users/{id}/location")
    public ApiModels.UserResponse location(@PathVariable UUID id,
                                            @Valid @RequestBody ApiModels.UpdateLocationRequest request) {
        users.location(id, request.latitude(), request.longitude()); return users.find(id);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.RequestResponse createRequest(@Valid @RequestBody ApiModels.CreateRequest request) {
        return requests.create(request);
    }

    @GetMapping("/requests")
    public List<ApiModels.RequestResponse> activeRequests() { return requests.active(); }

    @GetMapping("/requests/{id}/matches")
    public List<ApiModels.MatchResponse> matches(@PathVariable UUID id,
                                                  @RequestParam(defaultValue = "25") int radiusKm) {
        if (!List.of(25, 50, 100, 200).contains(radiusKm)) {
            throw new IllegalArgumentException("radiusKm must be one of 25, 50, 100, 200");
        }
        return users.matches(requests.find(id), radiusKm, 60);
    }
}
