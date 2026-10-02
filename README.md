# OneDrop MVP

OneDrop is a local-first MVP for connecting blood requests with nearby available
users. It is not a medical authority; eligibility and transfusion decisions must
be made by qualified medical professionals or blood banks.

## Run the API locally

1. Start dependencies: `docker compose up -d`
2. Start the API: `cd backend` then `mvn spring-boot:run`
3. API base URL: `http://localhost:8080`

The current MVP intentionally does not claim to provide authentication,
notifications, or medical verification. Those must be added before public use.
The API currently exposes user creation, availability/location updates, request
creation/listing, and radius-based direct matching.

## Flutter client

The `mobile` directory is a minimal Flutter client. Run `flutter pub get` and
`flutter run`; set `apiBaseUrl` in `mobile/lib/main.dart` for a physical device
(for Android emulator use `http://10.0.2.2:8080`).

## Next production steps

- Add OTP authentication and authenticated ownership checks.
- Add request abuse controls and Redis-backed rate limiting.
- Integrate FCM/APNs through a server-side notification worker.
- Add consent, account deletion, reporting/blocking, verification, privacy
  policy, and terms of use.
- Never expose exact donor coordinates or phone numbers to other users.
