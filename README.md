# Full-Stack User Authentication App

A secure full-stack user authentication system built with **Spring Boot**, **Spring Data JPA**, and **PostgreSQL**, featuring a responsive frontend interface.

## Tech Stack
* **Backend**: Java, Spring Boot, Spring Data JPA / Hibernate
* **Database**: PostgreSQL, pgAdmin
* **Frontend**: HTML5, CSS3, JavaScript (Fetch API)

## Features
* Secure User Registration and Login workflow.
* Password handling and database persistence using Spring Data JPA.
* Custom database table mapping to resolve reserved-keyword constraints.

## Project Structure Highlights
- `AuthController.java`: Manages REST endpoints for login and registration requests.
- `Users.java`: Entity model mapped securely to the PostgreSQL database.
- `UserRepository.java`: Data access layer interface extending JpaRepository.
