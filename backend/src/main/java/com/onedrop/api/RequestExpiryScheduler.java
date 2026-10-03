package com.onedrop.api;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RequestExpiryScheduler {
    private final RequestRepository requests;

    public RequestExpiryScheduler(RequestRepository requests) {
        this.requests = requests;
    }

    @Scheduled(fixedDelayString = "${onedrop.lifecycle.expiry-check-ms:60000}")
    public void expireDueRequests() {
        requests.expireDueRequests();
    }
}
