Here is the complete system design for the Automated Parking Garage, formatted in standard Markdown. You can directly copy and paste this content into a .md file.

Automated Parking Garage System Design
======================================

### 1\. System Overview

The Automated Parking Garage system is composed of two primary layers: the **Physical Layer** and the **Software Layer**.

*   **Physical Layer:** This includes all the hardware components.

   *   **Parking Lift:** The entry point for all vehicles. It includes sensors to measure car dimensions.

   *   **Car Dimension Sensors:** A set of sensors on the lift that measure the length, width, and height of a vehicle to determine its size.

   *   **Terminal:** A user interface (UI) located at the lift's entrance. It displays information and prints tickets.

   *   **Automated Transport System:** The robotic or mechanical system responsible for moving the car from the lift to its assigned parking spot.

*   **Software Layer:** This is the "brain" of the system, responsible for processing information and controlling the physical hardware.

   *   **Car Classifier:** A module that takes sensor data and categorizes the car as Small, Medium, or Large.

   *   **Parking Manager:** The central command center that handles the entire parking process, from checking for available spots to assigning them.

   *   **Database:** A system that stores the state of all parking spaces and car information.

   *   **Lift Controller:** A module that translates software commands into signals for the physical lift and transport system.


### 2\. High-Level System Diagram (ASCII)


![Status](https://github.com/sagarcodesandbox/aws-snowflake-microservices/blob/master/Comprehencive_AWS_Documents/images/Cloud%20Architecture.png)



### 3\. User Interaction Flow

The system is designed to be fully automated with minimal user input. The typical user flow for a customer parking their car is as follows:

1.  **Car Arrival:** The customer drives their car onto the parking lift.

2.  **Automated Measurement:** The lift's sensors automatically measure the car's dimensions.

3.  **Size Classification:** The **Car Classifier** module processes the sensor data and determines the car's size (Small, Medium, or Large).

4.  **Space Check:** The **Parking Manager** queries the database to find the number of available parking spaces for their specific car size.

5.  **Information Display:** The terminal displays the number of open spots to the customer.

6.  **Customer Confirmation:** The customer uses the terminal to confirm they want to park their car.

7.  **Ticket Generation:** Upon confirmation, the system generates a unique identifier, stores the car's information and assigned ID in the database, and prints a physical ticket with this ID.

8.  **Automated Parking:** The customer leaves the car, and the **Parking Manager** instructs the **Lift Controller** to move the car to the most appropriate spot.

9.  **Database Update:** Once the car is in its spot, the database is updated to reflect that the space is now occupied.


### 4\. Data Model

The system needs to track the status of all parking spaces and the cars currently occupying them. A simple relational data model would be effective for this prototype.

*   **ParkingSpaces Table:**

| Column      | Data Type                          | Description                                                                 |
|-------------|------------------------------------|-----------------------------------------------------------------------------|
| `space_id`  | `VARCHAR(10)`                      | Unique identifier for each parking space (e.g., S-01, M-50, L-10).         |
| `size`      | `ENUM('Small', 'Medium', 'Large')` | The designated size of the space.                                           |
| `is_occupied` | `BOOLEAN`                        | A flag indicating if the space is currently taken (`true`) or empty (`false`). |
| `car_id`    | `VARCHAR(10)`                      | The unique ID of the car in the space (nullable).                          |

*   **Tickets Table:**
    | Column           | Data Type                          | Description                                                              |
    |------------------|------------------------------------|--------------------------------------------------------------------------|
    | `ticket_id`      | `VARCHAR(10)`                      | The unique identifier printed on the ticket. This ID is also the `car_id`. |
    | `car_size`       | `ENUM('Small', 'Medium', 'Large')` | The size of the car that was parked.                                     |
    | `entry_timestamp`| `TIMESTAMP`                        | The time the car was parked.                                             |
    | `space_id`       | `VARCHAR(10)`                      | The parking space the car was assigned to.                               |

The car\_id in ParkingSpaces and the ticket\_id in Tickets act as a link between the physical location and the customer's record.

### 5\. Parking Logic: Finding the "Most Appropriate Spot"

The core logic for assigning a parking spot should be based on maximizing space efficiency. A simple **greedy algorithm** can be implemented by the **Parking Manager** to achieve this.

1.  **Exact Match:** The system first searches for an available parking space that exactly matches the car's size. For example, if a Small car arrives, the system looks for an empty **Small** space.

2.  **Hierarchical Search:** If no exact match is found, the system then searches for the next largest size. This ensures that larger spaces are only used when absolutely necessary, preserving them for larger vehicles.

   *   A **Small** car will first check for Small spaces. If none are available, it will check for Medium spaces. If those are also full, it will check for Large spaces.

   *   A **Medium** car will first check for Medium spaces. If none are available, it will check for Large spaces. A Medium car cannot fit in a Small space.

   *   A **Large** car can only be parked in a Large space.


By following this logic, the system prevents a Small car from immediately occupying a Large space if a Medium or Small space is available, thereby optimizing the total number of cars that can be parked at any given time.

### 6\. Concurrent Access and Race Conditions

In a multi-user environment, it's possible for two or more cars to attempt to park at the same time. This could lead to a **race condition** where both systems read the same available parking space and try to assign it, resulting in an error or data inconsistency. To prevent this, the system must implement a locking mechanism.

#### **Pessimistic Locking (Recommended)**

This approach is highly reliable and is recommended for a system where a single, critical resource (the parking spot) is being assigned.

*   **How it works:** When a parking request begins, the **Parking Manager** initiates a database transaction. Within this transaction, it performs a SELECT ... FOR UPDATE query on the available ParkingSpaces rows that match the car's size. This action puts an **exclusive lock** on those rows.

*   **The Flow:**

   1.  Request A arrives, finds an empty Small spot (S-01), and locks the S-01 row.

   2.  Request B arrives simultaneously, also looking for a Small spot. It will try to access the available Small spots, including S-01. However, it will **wait** until Request A's lock is released.

   3.  Request A assigns the car to S-01 and commits the transaction, releasing the lock.

   4.  Request B's query can now proceed. It will see that S-01 is now occupied and will move on to find the next available spot.

*   **Advantages:** Guarantees data integrity and prevents conflicts absolutely.

*   **Disadvantages:** Can lead to performance bottlenecks if there is high contention for the same resources, as other requests have to wait.


#### **Optimistic Locking**

This is an alternative approach that assumes conflicts are rare and is more scalable for low-contention scenarios.

*   **How it works:** Instead of locking, this method adds a version number (version\_id) or a timestamp to the ParkingSpaces table. When the **Parking Manager** retrieves an empty spot, it also gets its version\_id. When it's time to update the spot, the UPDATE query includes a WHERE clause that checks if the version\_id is still the same.

*   **The Flow:**

   1.  Request A and Request B both read the same empty Small spot (S-02) with version\_id = 1.

   2.  Request A updates S-02, setting is\_occupied = true and incrementing version\_id to 2. The UPDATE query's WHERE clause checks that version\_id = 1. This succeeds.

   3.  Request B tries to update S-02, also checking that version\_id = 1. This UPDATE query will **fail** because the version has already been changed to 2.

   4.  Request B's transaction detects the failure and **retries** the entire process from the beginning, finding a new available spot.

*   **Advantages:** Better performance in a low-contention environment because there is no waiting for locks.

*   **Disadvantages:** Can be less intuitive to implement and requires the application to handle retries gracefully.


For a prototype, **Pessimistic Locking** is the more straightforward and safer choice to ensure a spot is assigned only once.

### 7\. Spring Boot Microservices Architecture

To make the system more scalable, maintainable, and resilient, the functionality can be broken down into a set of specialized microservices using Spring Boot. This modular approach allows for independent development, deployment, and scaling of each component.

#### Detailed Microservices Architecture Diagram

The introduction of an **API Gateway** provides a single, secure entry point for all incoming requests, which is a best practice for microservices. This new diagram illustrates the detailed communication flow, including the API Gateway and asynchronous messaging.
<pre>
+---------------------+
|   Terminal (UI)     |
+----------+----------+
           | HTTP(s)
           v
+---------------------+
|    API Gateway      |
|  (e.g., Spring     |
|   Cloud Gateway)    |
+----------+----------+
           |
           +--------------------------------------------+
           |                                            |
           v HTTP(s)                                    v HTTP(s)
+---------------------+                      +---------------------+
|  Ticket/Entry       |                      |   Car Sizing        |
|  Service            |<-------------------->|   Service           |
|  (Spring Boot REST) |  (Internal Call)     |  (Spring Boot REST) |
+----------+----------+                      +---------------------+
           |
           v HTTP(s)
+---------------------+
|  Parking Lot        |
|  Service            |
|  (Spring Boot REST) |
+----------+----------+
           |
           +--------------------------------+
           |  (Asynchronous Message Queue)  |
           v                                |
+---------------------+                     |
|  Automated          |                     |
|  Transport Service  |<--------------------+
|  (Spring Boot)      |
+---------------------+
           |
           v
+---------------------+
| Physical Components |
+---------------------+
</pre>
#### Microservice Roles and Responsibilities

*   **API Gateway:**

   *   **Responsibility:** Serves as the single entry point for all client requests from the Terminal UI. It handles routing, security (authentication and authorization), and load balancing.

   *   **Communication:** Routes incoming requests to the appropriate microservice based on the URL path.

*   **TicketEntryService:**

   *   **Responsibility:** This is the primary orchestrator for the parking process. It receives the initial request from the API Gateway, handles the user-facing logic, and generates the unique ticket.

   *   **Communication:** Calls the **CarSizingService** to determine the vehicle's size and then makes a blocking REST call to the **ParkingLotService** to reserve a spot. Finally, it sends the ticket details to the Terminal.

*   **CarSizingService:**

   *   **Responsibility:** A specialized service for classifying the car's size (Small, Medium, or Large) based on sensor data.

   *   **Communication:** Exposes a simple REST endpoint that the TicketEntryService calls to get the car size.

*   **ParkingLotService:**

   *   **Responsibility:** This is the most critical service, managing the state of all parking spots. It contains the business logic for finding a spot and ensuring data integrity.

   *   **Race Condition Handling:** This service is where the **Pessimistic Locking** is implemented. When a parking request arrives, it starts a database transaction, locks the available parking space row, assigns the car, and commits the transaction.

   *   **Communication:** Exposes a REST API for reserving a spot. After a spot is successfully assigned, it sends an asynchronous message (e.g., to a RabbitMQ or Kafka queue) to the AutomatedTransportService to initiate the car movement.

*   **AutomatedTransportService:**

   *   **Responsibility:** This service is decoupled from the main parking logic. It subscribes to a message queue and listens for commands to move a car. Upon receiving a message with a parking spot ID, it translates this into physical commands for the automated transport system.

   *   **Communication:** Communicates with the physical hardware (e.g., the lift, robotic movers) to execute the parking process.


#### How a Request is Handled

1.  A car arrives, and the Terminal (UI) sends the sensor data and a parking request to the **API Gateway**.

2.  The **API Gateway** routes the request to the **TicketEntryService**.

3.  The **TicketEntryService** calls the **CarSizingService** to get the car size.

4.  With the car size, the **TicketEntryService** then calls the **ParkingLotService** to reserve a spot.

5.  The **ParkingLotService** executes its core business logic with **Pessimistic Locking**. It acquires a lock on a database row, assigns a spot, and commits the transaction. This guarantees that no other concurrent request can claim the same spot.

6.  After a successful assignment, the **ParkingLotService** asynchronously sends a message to a queue, which the **AutomatedTransportService** is listening to.

7.  The **AutomatedTransportService** receives the message and starts the physical process of moving the car.

8.  The **ParkingLotService** returns the assigned spot details and a unique ticket ID to the **TicketEntryService**, which then sends this information back to the Terminal via the **API Gateway** for the customer.


### 8\. Low-Level Design (LLD) for ParkingLotService

The ParkingLotService is central to managing parking space allocation and ensuring data consistency. This section details its internal structure, including key classes, relationships, and the implementation of pessimistic locking.

#### ParkingLotService Class Diagram (ASCII)

<pre>
+--------------------------+
|      ParkingLotService   |
+--------------------------+
| - parkingSpotRepository  | <--- Dependency Injection
| - ticketRepository       | <--- Dependency Injection
| - messageProducer        | <--- Dependency Injection (for Async)
+--------------------------+
| + getAvailableSpots(size): int    |
| + parkCar(carSize, carId): ParkingResponse |
| + unparkCar(ticketId): boolean    |
+--------------------------+
             | Uses
             v
+--------------------------+
|    ParkingSpotRepository |
+--------------------------+
| + findByIsOccupiedFalseAndSizeOrderBySizeAsc(size, pageable): Page<ParkingSpot> |
| + save(parkingSpot): ParkingSpot |
| + findByCarId(carId): Optional<ParkingSpot> |
| + findById(spaceId): Optional<ParkingSpot> |
+--------------------------+
             | Interacts with
             v
+--------------------------+
|        ParkingSpot       |
+--------------------------+
| - spaceId: String        |
| - size: Enum             |
| - isOccupied: Boolean    |
| - carId: String          |
+--------------------------+
             ^
             | Creates
             |
+--------------------------+
|          Ticket          |
+--------------------------+
| - ticketId: String       |
| - carSize: Enum          |
| - entryTimestamp: Timestamp |
| - spaceId: String        |
+--------------------------+
             ^
             | Uses
             |
+--------------------------+
|     TicketRepository     |
+--------------------------+
| + save(ticket): Ticket   |
| + findById(ticketId): Optional<Ticket> |
+--------------------------+
</pre>
#### Detailed Explanation of Classes

1.  **ParkingSpot (Entity/Model)**

   *   **Purpose:** Represents a single parking space in the database. This is a JPA entity that maps directly to the ParkingSpaces table.

   *   **Attributes:**

      *   spaceId (String): Primary key, unique identifier for the parking spot (e.g., "S-001", "M-050", "L-010").

      *   size (Enum CarSize): Defines the size of the parking spot (SMALL, MEDIUM, LARGE).

      *   isOccupied (boolean): A flag indicating whether the spot is currently occupied (true) or available (false).

      *   carId (String): The unique identifier of the car currently occupying this spot. This will be nullable when the spot is empty.

   *   **Annotations (Spring Data JPA):** @Entity, @Id, @Enumerated(EnumType.STRING), @Column.

2.  **Ticket (Entity/Model)**

   *   **Purpose:** Represents a parking ticket issued to a customer. This is a JPA entity that maps directly to the Tickets table.

   *   **Attributes:**

      *   ticketId (String): Primary key, unique identifier for the ticket. This will be the same as the carId.

      *   carSize (Enum CarSize): The size of the car that parked.

      *   entryTimestamp (Timestamp): The exact time the car entered the garage.

      *   spaceId (String): The spaceId of the ParkingSpot where the car is parked.

   *   **Annotations (Spring Data JPA):** @Entity, @Id, @Enumerated(EnumType.STRING), @Column.

3.  **ParkingSpotRepository (Data Access Layer)**

   *   **Purpose:** An interface that extends Spring Data JPA's JpaRepository. It provides methods for CRUD operations on ParkingSpot entities and custom queries for finding available spots with pessimistic locking.

   *   **Key Methods:**

      *   findByIsOccupiedFalseAndSizeOrderBySizeAsc(CarSize size, Pageable pageable): Finds available spots of a specific size, ordered (e.g., by spaceId for consistency), and supports pagination to retrieve just one.

      *   @Lock(LockModeType.PESSIMISTIC\_WRITE): This annotation is crucial. It ensures that when a ParkingSpot is retrieved by a method (e.g., a custom findFirstBy... query or findById), an exclusive write lock is placed on that database row. This prevents other concurrent transactions from reading or modifying the same row until the current transaction commits or rolls back.

      *   save(ParkingSpot parkingSpot): Saves or updates a ParkingSpot entity.

      *   findByCarId(String carId): Finds a ParkingSpot by the carId (useful for unparking).

      *   findById(String spaceId): Finds a ParkingSpot by its spaceId.

4.  **TicketRepository (Data Access Layer)**

   *   **Purpose:** An interface extending JpaRepository for Ticket entities.

   *   **Key Methods:**

      *   save(Ticket ticket): Saves a new Ticket entity.

      *   findById(String ticketId): Retrieves a ticket by its ID.

5.  **ParkingLotService (Service Layer)**

   *   **Purpose:** Contains the core business logic for parking and unparking cars. It orchestrates interactions with the ParkingSpotRepository, TicketRepository, and sends messages for physical transport.

   *   **Dependencies:** Injects ParkingSpotRepository, TicketRepository, and a MessageProducer (for sending messages to AutomatedTransportService).

   *   **Key Methods:**

      *   getAvailableSpots(CarSize size): Returns the count of available spots for a given size. This method does not need locking as it's a read-only operation.

      *   @Transactional

      *   parkCar(CarSize carSize, String carId):

         *   This method is annotated with @Transactional to ensure atomicity.

         *   It implements the **greedy algorithm** by attempting to find an exact match first, then progressively larger sizes.

         *   For each search, it calls a ParkingSpotRepository method that uses @Lock(LockModeType.PESSIMISTIC\_WRITE) to acquire an exclusive lock on the _first available spot found_.

         *   If a spot is found and locked:

            *   The ParkingSpot entity is updated (isOccupied = true, carId = carId).

            *   The updated ParkingSpot is saved via parkingSpotRepository.save().

            *   A new Ticket entity is created and saved via ticketRepository.save().

            *   An asynchronous message is sent to the message queue (e.g., messageProducer.sendParkCommand(spotId, carId)).

            *   The method returns a ParkingResponse object (e.g., containing ticketId, spaceId).

         *   If no spot is found after checking all compatible sizes, an exception is thrown (e.g., ParkingFullException).

      *   unparkCar(String ticketId):

         *   Also annotated with @Transactional.

         *   Retrieves the Ticket using ticketRepository.findById().

         *   Retrieves the associated ParkingSpot using parkingSpotRepository.findByCarId().

         *   Acquires a lock on the ParkingSpot (e.g., by calling findById with LockModeType.PESSIMISTIC\_WRITE).

         *   Updates the ParkingSpot (isOccupied = false, carId = null).

         *   Deletes the Ticket record.

         *   Sends an asynchronous message to the message queue (e.g., messageProducer.sendUnparkCommand(spotId))


### **2\. API Gateway Endpoints (External/Public Facing)**

These are the primary endpoints exposed by the **API Gateway** to the external world (e.g., the Terminal UI). The API Gateway will then route these requests to the appropriate internal microservices.

#### **2.1. Parking Operations**

-   **Request to Park a Car (Ticket Creation)**

    -   **Endpoint:** `POST /v1/parking/park`

    -   **Description:** Initiates the parking process. The `TicketEntryService` orchestrates this, calling `CarSizingService` and `ParkingLotService`.

    -   **Request Body:**

        JSON

        ```
        {
            "vehicleDetails": {
                "licensePlate": "ABC123",
                "vehicleType": "CAR", // CAR, MOTORCYCLE, VAN, TRUCK
                "dimensions": {
                    "lengthCm": 450,
                    "widthCm": 180,
                    "heightCm": 150
                }
            }
        }

        ```

    -   **Success Response (201 Created):**

        JSON

        ```
        {
            "ticketId": "TICKET-XYZ789",
            "spaceId": "S-005",
            "entryTime": "2025-08-06T10:30:00Z",
            "message": "Car parked successfully. Please take your ticket."
        }

        ```

    -   **Error Response (e.g., 400 Bad Request, 409 Conflict):**

        JSON

        ```
        {
            "code": "PARKING_FULL",
            "message": "No available spots for the specified vehicle type."
        }

        ```

-   **Request to Unpark a Car**

    -   **Endpoint:** `POST /v1/parking/unpark`

    -   **Description:** Initiates the unparking process. The `TicketEntryService` orchestrates this, calling `ParkingLotService` and `PaymentService`.

    -   **Request Body:**

        JSON

        ```
        {
            "ticketId": "TICKET-XYZ789"
        }

        ```

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "ticketId": "TICKET-XYZ789",
            "spaceId": "S-005",
            "entryTime": "2025-08-06T10:30:00Z",
            "exitTime": "2025-08-06T12:30:00Z",
            "parkingDurationMinutes": 120,
            "amountDue": 15.00,
            "currency": "USD",
            "message": "Please proceed to payment."
        }

        ```

    -   **Error Response (e.g., 404 Not Found, 400 Bad Request):**

        JSON

        ```
        {
            "code": "TICKET_NOT_FOUND",
            "message": "Invalid or expired ticket ID."
        }

        ```

#### **2.2. Information Retrieval**

-   **Get Available Parking Spots**

    -   **Endpoint:** `GET /v1/parking/available-spots`

    -   **Description:** Retrieves the count of available parking spots, optionally filtered by size.

    -   **Query Parameters:**

        -   `vehicleType` (Optional): `CAR`, `MOTORCYCLE`, `VAN`, `TRUCK`

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "totalAvailable": 150,
            "details": [
                { "vehicleType": "CAR", "availableCount": 100 },
                { "vehicleType": "MOTORCYCLE", "availableCount": 20 },
                { "vehicleType": "VAN", "availableCount": 15 },
                { "vehicleType": "TRUCK", "availableCount": 15 }
            ]
        }

        ```

    -   **Example with `vehicleType` query parameter:** `GET /v1/parking/available-spots?vehicleType=CAR`

        JSON

        ```
        {
            "totalAvailable": 100,
            "details": [
                { "vehicleType": "CAR", "availableCount": 100 }
            ]
        }

        ```

-   **Get Ticket Details**

    -   **Endpoint:** `GET /v1/tickets/{ticketId}`

    -   **Description:** Retrieves details for a specific parking ticket.

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "ticketId": "TICKET-XYZ789",
            "licensePlate": "ABC123",
            "vehicleType": "CAR",
            "spaceId": "S-005",
            "entryTime": "2025-08-06T10:30:00Z",
            "exitTime": null, // or "2025-08-06T12:30:00Z" if unparked
            "parkingDurationMinutes": null, // or 120
            "amountDue": 15.00, // or 0.00 if paid
            "currency": "USD",
            "isPaid": false // or true
        }

        ```

    -   **Error Response (e.g., 404 Not Found):**

        JSON

        ```
        {
            "code": "TICKET_NOT_FOUND",
            "message": "Ticket with ID TICKET-XYZ789 not found."
        }

        ```

#### **2.3. Payment Operations**

-   **Process Payment**

    -   **Endpoint:** `POST /v1/payments`

    -   **Description:** Records a payment for a parking ticket.

    -   **Request Body:**

        JSON

        ```
        {
            "ticketId": "TICKET-XYZ789",
            "amountPaid": 15.00,
            "currency": "USD",
            "paymentMethod": "CREDIT_CARD", // or DEBIT_CARD, CASH, MOBILE_PAY
            "transactionDetails": {
                "cardNumberLast4": "1234",
                "cardType": "VISA"
            }
        }

        ```

    -   **Success Response (201 Created):**

        JSON

        ```
        {
            "paymentId": "PAY-12345",
            "ticketId": "TICKET-XYZ789",
            "amountPaid": 15.00,
            "currency": "USD",
            "paymentTime": "2025-08-06T12:35:00Z",
            "message": "Payment processed successfully."
        }

        ```

    -   **Error Response (e.g., 400 Bad Request, 404 Not Found, 409 Conflict):**

        JSON

        ```
        {
            "code": "PAYMENT_FAILED",
            "message": "Payment could not be processed due to insufficient funds."
        }

        ```

* * * * *

### **3\. Microservice-Specific Endpoints (Internal)**

These APIs are typically consumed by other microservices or the API Gateway, not directly by external clients.

#### **3.1. `TicketEntryService` (Orchestrator)**

-   *(No direct external API, acts as an orchestrator for external requests)*

#### **3.2. `CarSizingService`**

-   **Classify Vehicle Size**

    -   **Endpoint:** `POST /internal/v1/car-sizing/classify`

    -   **Description:** Classifies a vehicle into `SMALL`, `MEDIUM`, or `LARGE` based on dimensions.

    -   **Request Body:**

        JSON

        ```
        {
            "vehicleType": "CAR",
            "dimensions": {
                "lengthCm": 450,
                "widthCm": 180,
                "heightCm": 150
            }
        }

        ```

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "carSize": "MEDIUM" // SMALL, MEDIUM, LARGE
        }

        ```

    -   **Error Response (e.g., 400 Bad Request):** Invalid dimensions.

#### **3.3. `ParkingLotService`**

-   **Find and Lock Parking Spot**

    -   **Endpoint:** `POST /internal/v1/parking-lots/allocate`

    -   **Description:** Finds and *locks* an available parking spot for a given car size. This is where pessimistic locking occurs.

    -   **Request Body:**

        JSON

        ```
        {
            "carSize": "MEDIUM", // SMALL, MEDIUM, LARGE
            "carId": "XYZ-1234" // Unique ID for the car (same as ticketId)
        }

        ```

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "spaceId": "M-023"
        }

        ```

    -   **Error Response (e.g., 409 Conflict, 404 Not Found):** No available spots.

-   **Release Parking Spot**

    -   **Endpoint:** `POST /internal/v1/parking-lots/release`

    -   **Description:** Marks a parking spot as available.

    -   **Request Body:**

        JSON

        ```
        {
            "spaceId": "M-023",
            "carId": "XYZ-1234"
        }

        ```

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "message": "Parking spot M-023 released."
        }

        ```

    -   **Error Response (e.g., 404 Not Found):** Spot not found or not occupied by this car.

-   **Get Parking Spot Status (Internal)**

    -   **Endpoint:** `GET /internal/v1/parking-lots/spots/{spaceId}`

    -   **Description:** Retrieves detailed status of a specific parking spot.

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "spaceId": "M-023",
            "size": "MEDIUM",
            "isOccupied": true,
            "carId": "XYZ-1234"
        }

        ```

#### **3.4. `PaymentService`**

-   **Calculate Parking Fee**

    -   **Endpoint:** `GET /internal/v1/payments/calculate-fee`

    -   **Description:** Calculates the parking fee based on entry time and current time/exit time.

    -   **Query Parameters:**

        -   `ticketId`: The ID of the ticket.

    -   **Success Response (200 OK):**

        JSON

        ```
        {
            "amountDue": 15.00,
            "currency": "USD",
            "parkingDurationMinutes": 120
        }

        ```

    -   **Error Response (e.g., 404 Not Found):** Ticket not found.

-   **Record Payment Transaction**

    -   **Endpoint:** `POST /internal/v1/payments/record`

    -   **Description:** Records a successful payment transaction.

    -   **Request Body:**

        JSON

        ```
        {
            "ticketId": "TICKET-XYZ789",
            "amount": 15.00,
            "currency": "USD",
            "paymentMethod": "CREDIT_CARD",
            "transactionReference": "PAYGATEWAY-TXN-ABC"
        }

        ```

    -   **Success Response (201 Created):**

        JSON

        ```
        {
            "paymentId": "PAY-12345",
            "message": "Payment recorded."
        }

        ```

    -   **Error Response:** Payment recording failed.

* * * * *

### **4\. Consistent Error Handling**

All API responses for errors should follow a consistent structure to make error handling predictable for clients.

-   **Error Response Structure:**

    JSON

    ```
    {
        "timestamp": "2025-08-06T10:30:00Z",
        "status": 400,
        "error": "Bad Request",
        "code": "INVALID_INPUT", // A specific, internal error code
        "message": "Vehicle dimensions are missing.",
        "path": "/v1/parking/park"
    }

    ```

-   **Common Error Codes:**

    -   `INVALID_INPUT`: Malformed request body, missing required fields, invalid data types.

    -   `NOT_FOUND`: Resource not found (e.g., `ticketId` not found).

    -   `CONFLICT`: Resource conflict (e.g., parking spot already occupied, payment already processed).

    -   `UNAUTHORIZED`: Missing or invalid authentication credentials.

    -   `FORBIDDEN`: Authenticated but not authorized to perform the action.

    -   `SERVICE_UNAVAILABLE`: Downstream service issue or temporary outage.

    -   `INTERNAL_SERVER_ERROR`: Generic server-side error.

* * * * *

### **5\. API Versioning**

We will use **URI Versioning** (embedding the version number in the URL path) as it's simple, explicit, and widely understood.

-   **Example:** `/v1/parking/park`

-   **Strategy:** When significant breaking changes are introduced (e.g., a change in request/response schema that's not backward compatible), a new version (`/v2/`) will be released. Older versions will be maintained for a deprecation period.


