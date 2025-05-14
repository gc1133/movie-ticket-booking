# Movie Ticket Booking Microservices

A microservices-based application for booking movie tickets, built with Spring Boot, Kafka, PostgreSQL, Eureka, and Resilience4j.

## Architecture
- **Discovery Service**: Eureka Server for service discovery.
- **User Service**: Manages user data.
- **Movie Service**: Manages movie data and search.
- **Theatre Service**: Manages theatres, screens, shows, and seats.
- **Booking Service**: Handles ticket booking and payments.
- **Notification Service**: Sends booking notifications (simulated via logs).

## Features
- **Asynchronous Communication**: Kafka for event-driven communication.
- **Service Discovery**: Eureka for dynamic service registration.
- **Resilience**: Circuit breakers and fallbacks with Resilience4j.
- **Metrics**: Prometheus endpoints for monitoring.
- **Transactional Handling**: Kafka transactions for consistency.

## Prerequisites
- Java 21
- Maven
- Docker & Docker Compose
- IntelliJ IDEA (optional)
- Postman (for testing)

## Setup Instructions
1. **Clone the Repository** (or create manually):
   ```bash
   git clone <repository-url>
   cd movie-ticket-booking
   ```
2. **Create Databases**:
    - Run PostgreSQL containers via Docker Compose:
      ```bash
      docker-compose up -d postgres-user postgres-movie postgres-theatre postgres-booking
      ```
    - Apply `data.sql` for each database using `psql` or pgAdmin.
3. **Run Services**:
    - Build and run all services:
      ```bash
      mvn clean package
      docker-compose up --build
      ```
    - Or run individual services in IntelliJ:
        - Open each module’s `Application` class (e.g., `UserServiceApplication`).
        - Click the green "Run" button.
4. **Access Eureka Dashboard**:
    - Open `http://localhost:8761` to verify registered services.

## Testing

This section provides steps to test the end-to-end booking flow using Postman, covering user creation, movie and theatre setup, booking, confirmation, and notification verification. Ensure Docker containers are running (`docker-compose up --build`) and all services are registered in Eureka (`http://localhost:8761`).

### Prerequisites
- **Postman**: Installed (download from `https://www.postman.com/downloads/`).
- **Docker**: Containers running (`docker ps` shows `user-service-1`, `movie-service-1`, etc.).
- **Eureka**: Verify services at `http://localhost:8761`.

### Testing Steps

1. **Create a User (`user-service`)**:
   - **Method**: POST
   - **URL**: `http://localhost:8081/api/users`
   - **Headers**: `Content-Type: application/json`
   - **Body**:
     ```json
     {
       "name": "John Doe",
       "email": "john@example.com",
       "phoneNumber": "1234567890"
     }
     ```
   - **Expected Response**: `200 OK` or `201 Created`
     ```json
     {
       "id": 1,
       "name": "John Doe",
       "email": "john@example.com",
       "phoneNumber": "1234567890"
     }
     ```
   - **Action**: Note the `id` (e.g., `1`). Repeat to create another user (e.g., `Jane Doe`, `jane@example.com`).

2. **Add a Movie (`movie-service`)**:
   - **Method**: POST
   - **URL**: `http://localhost:8082/api/movies`
   - **Headers**: `Content-Type: application/json`
   - **Body**:
     ```json
     {
       "title": "Inception",
       "genre": "Sci-Fi",
       "duration": 148
     }
     ```
   - **Expected Response**: `200 OK` or `201 Created`
     ```json
     {
       "id": 1,
       "title": "Inception",
       "genre": "Sci-Fi",
       "duration": 148
     }
     ```
   - **Action**: Note the `id` (e.g., `1`).

3. **Add a Theatre, Show, and Seats (`theatre-service`)**:
   - **Create Theatre**:
      - **Method**: POST
      - **URL**: `http://localhost:8083/api/theatres`
      - **Headers**: `Content-Type: application/json`
      - **Body**:
        ```json
        {
          "name": "AMC Empire",
          "cityId": 1
        }
        ```
      - **Expected Response**: `200 OK` or `201 Created`
        ```json
        {
          "id": 1,
          "name": "AMC Empire",
          "cityId": 1
        }
        ```
      - **Action**: Note the `id` (e.g., `1`).
   - **Create Show**:
      - **Method**: POST
      - **URL**: `http://localhost:8083/api/shows`
      - **Headers**: `Content-Type: application/json`
      - **Body**:
        ```json
        {
          "movieId": 1,
          "theatreId": 1,
          "showTime": "2025-05-15T18:00:00",
          "price": 10.00
        }
        ```
      - **Expected Response**: `200 OK` or `201 Created`
        ```json
        {
          "id": 1,
          "movieId": 1,
          "theatreId": 1,
          "showTime": "2025-05-15T18:00:00",
          "price": 10.00
        }
        ```
      - **Action**: Note the `id` (e.g., `1`).
   - **Create Seats**:
      - **Method**: POST
      - **URL**: `http://localhost:8083/api/shows/1/seats`
      - **Headers**: `Content-Type: application/json`
      - **Body**:
        ```json
        [
          { "seatNumber": "A1", "status": "AVAILABLE" },
          { "seatNumber": "A2", "status": "AVAILABLE" }
        ]
        ```
      - **Expected Response**: `200 OK` or `201 Created`
        ```json
        [
          { "id": 1, "seatNumber": "A1", "status": "AVAILABLE", "showId": 1 },
          { "id": 2, "seatNumber": "A2", "status": "AVAILABLE", "showId": 1 }
        ]
        ```
      - **Action**: Note the `seatIds` (e.g., `1`, `2`).

4. **Create and Confirm a Booking (`booking-service`)**:
   - **Create Booking**:
      - **Method**: POST
      - **URL**: `http://localhost:8084/api/bookings`
      - **Headers**: `Content-Type: application/json`
      - **Body**:
        ```json
        {
          "userId": 1,
          "showId": 1,
          "totalAmount": 20.00,
          "seatIds": [1, 2]
        }
        ```
      - **Expected Response**: `200 OK` or `201 Created`
        ```json
        {
          "id": 1,
          "userId": 1,
          "showId": 1,
          "totalAmount": 20.00,
          "seatIds": [1, 2],
          "status": "PENDING"
        }
        ```
   - **Confirm Booking**:
      - **Method**: POST
      - **URL**: `http://localhost:8084/api/bookings/1/confirm`
      - **Headers**: `Content-Type: application/json`
      - **Body**: `{}`
      - **Expected Response**: `200 OK`
        ```json
        {
          "id": 1,
          "userId": 1,
          "showId": 1,
          "totalAmount": 20.00,
          "seatIds": [1, 2],
          "status": "CONFIRMED"
        }
        ```

5. **Verify Notification (`notification-service`)**:
   - **Action**: Check logs:
     ```bash
     docker-compose logs notification-service
     ```
   - **Expected Output**:
     ```
     Sending notification to user: john@example.com for booking ID: 1, amount: 20.00
     ```
   - **Note**: This confirms Kafka-driven notification for the confirmed booking.

### Troubleshooting
- **Service Not Responding**: Verify containers (`docker ps`) and restart if needed (`docker-compose restart <service-name>`).
- **Kafka Issues**: Check `kafka-1` logs (`docker-compose logs kafka`) and ensure topics exist:
  ```bash
  docker exec -it kafka-1 kafka-topics.sh --bootstrap-server kafka:9092 --list
  ```
- **Database Errors**: Verify PostgreSQL logs (e.g., `docker-compose logs postgres-user`) and schema (`spring.jpa.hibernate.ddl-auto: update`).
- **API Mismatches**: If endpoints differ, check controller classes (e.g., `UserController.java`) and update URLs.
- **Logs**: Monitor service logs for errors:
  ```bash
  docker-compose logs --follow
  ```

### Additional Tests
- **Retrieve Users**: `GET http://localhost:8081/api/users`
- **List Movies**: `GET http://localhost:8082/api/movies`
- **List Shows**: `GET http://localhost:8083/api/shows`
- **Test Failure Cases**: Try booking unavailable seats (`POST /api/bookings` with booked `seatIds`) and expect a `400 Bad Request`.