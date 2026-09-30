# Champions League Draw Backend

A Spring Boot REST API that simulates a UEFA Champions League league-phase draw for 36 teams.

The backend handles team management, draw generation, home/away assignment, and draw validation while storing team data in PostgreSQL.

## Overview

The application simulates a Champions League-style draw based on predefined constraints.

The 36 teams are divided into **4 pots**, with **9 teams in each pot**. Each team is assigned **8 opponents**, with exactly **2 opponents from each pot**.

The backend is responsible for the complete draw algorithm and validation. A frontend can consume the REST API to display the generated draw for a selected club.

## Features

* 36 teams divided into 4 pots
* PostgreSQL database integration
* Automatic team loading from CSV
* Random draw generation
* 8 opponents for every team
* 2 opponents from each pot
* No team can play itself
* No same-country opponents
* Maximum of 2 opponents from the same country
* Mutual opponent relationships
* Exactly 4 home and 4 away matches per team
* Consistent home/away assignment between both teams
* Draw validation
* RESTful API endpoints
* Global exception handling
* Team lookup by ID and pot

## Draw Rules

The generated draw follows these rules:

1. There are 36 teams.
2. Teams are divided into 4 pots of 9 teams.
3. Each team plays 8 opponents.
4. Each team receives exactly 2 opponents from every pot.
5. A team cannot play itself.
6. Teams from the same country cannot be paired.
7. A team can have a maximum of 2 opponents from the same country.
8. Every pairing is mutual.
9. Each team plays 4 matches at home and 4 matches away.
10. Home/away assignments are consistent for both sides of every match.
11. The generated draw is validated before being returned by the API.

## Technologies

* Java
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* PostgreSQL
* Hibernate
* Maven
* REST API

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/mayer/championsleaguedraw/
    │       ├── Controller/
    │       │   └── DrawController.java
    │       ├── Service/
    │       │   └── DrawService.java
    │       ├── exception/
    │       │   ├── GlobalExceptionHandler.java
    │       │   └── TeamNotFoundException.java
    │       ├── model/
    │       │   ├── DrawOpponent.java
    │       │   ├── DrawResult.java
    │       │   └── Team.java
    │       ├── repository/
    │       │   └── TeamRepository.java
    │       ├── ChampionsLeagueDrawApplication.java
    │       └── DataLoader.java
    │
    └── resources/
        ├── teams.csv
        └── application.properties
```

## API Endpoints

### Get all teams

```http
GET /api/draw/teams
```

Returns all 36 teams stored in the database.

### Get teams by pot

```http
GET /api/draw/teams/pot/{pot}
```

Example:

```http
GET /api/draw/teams/pot/1
```

Returns the teams belonging to the selected pot.

### Get team by ID

```http
GET /api/draw/teams/{id}
```

Example:

```http
GET /api/draw/teams/3
```

### Generate a complete draw

```http
POST /api/draw/generate
```

Generates and validates a complete draw for all 36 teams.

### Get the draw for a specific team

```http
GET /api/draw/team/{id}
```

Example:

```http
GET /api/draw/team/3
```

Returns the selected team's 8 opponents grouped by pot, including home/away information.

## Database Configuration

The application uses PostgreSQL.

Database configuration is provided through environment variables rather than storing database credentials directly in the repository.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Environment Variables

Set the following variables before running the application:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Example on Windows PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:1234/champions_league"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

## Running the Application

### Prerequisites

Make sure you have:

* Java 21 or later
* PostgreSQL
* Git
* Maven or Maven Wrapper

### Run with Maven Wrapper

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## Data Loading

Team data is stored in:

```text
src/main/resources/teams.csv
```

When the application starts, `DataLoader` checks whether teams already exist in the database.

If the database is empty, the 36 teams are loaded automatically from the CSV file.

## Validation

After generating a draw, the backend validates the complete result.

Validation checks include:

* Correct number of opponents
* Correct number of opponents from each pot
* No self-opponents
* No same-country opponents
* Maximum two opponents from the same country
* Mutual pairings
* Correct home/away balance
* Consistent home/away relationships

A successful generation produces:

```text
DRAW VALIDATION PASSED
```

## Architecture

The project follows a simple layered Spring Boot architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

### Controller

Handles HTTP requests and exposes the REST API.

### Service

Contains the main Champions League draw algorithm and validation logic.

### Repository

Uses Spring Data JPA to communicate with PostgreSQL.

### Model

Contains the entities and objects used by the application.

### Exception

Provides centralized exception handling for API errors.

## Future Frontend

The backend is designed to be consumed by a frontend application.

A frontend can:

1. Load the available teams.
2. Allow the user to select a club.
3. Request that team's draw.
4. Display the 8 opponents.
5. Group opponents by pot.
6. Display home and away matches.

The draw algorithm remains entirely on the backend.

## Author

**Mayer Magdy**

Computer Science Student
Backend Development — Java & Spring Boot

GitHub: [Add your GitHub profile here]

LinkedIn: [Add your LinkedIn profile here]
