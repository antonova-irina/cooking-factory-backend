# CookingFactoryApp (Backend)

A Java Spring Boot REST API for managing a cooking school entities (Instructors - Students - Courses). Included: JWT-based authentication, MySQL persistence, endpoints for authentication and role-based entities operations (OpenAPI/Swagger UI), Gradle as build tool.

## Requirements
- JDK 17+
- MySQL 8.x running locally or accessible via network (for dev/prod runs)
- Internet access to resolve Gradle dependencies
- Shell that can execute the Gradle Wrapper scripts.

## Project Entry Point
- Main class: `gr.aueb.cf.cookingfactory`
- Default profile: `dev` (set in `src/main/resources/application.properties`)

## Configuration & Environment Variables
The `dev` profile requires the following environment variables:

- MYSQL_HOST (default: `localhost`)
- MYSQL_PORT (default: `3306`)
- MYSQL_DB (default: `cookingfactorydb`) _// Charset: utf8mb4, Collation: utf8mb4_0900_ai_ci_
- MYSQL_USER (default: `user_chef`) _// in "Privileges" use "SELECT ALL"
- MYSQL_PASSWORD (default: `c00k1ngF@`)

Other notable properties (currently stored in `application-dev.properties`):

    `app.security.secret-key`
    `app.security.jwt-expiration`

## Setup and running the Application
1. Ensure MySQL is running and a database that matches `MYSQL_DB` exists.
2. Download and open in your JDK the project folder.
3. Build dependencies using :
   - Windows: `gradlew.bat clean build`
   - macOS/Linux: `./gradlew clean build`
   
4. Run the application via Gradle:
     - Windows: `gradlew.bat bootRun`
     - macOS/Linux: `./gradlew bootRun`
      
     _// (during the first run the db tables will be automatically created from the model)_

5. Uncomment the below lines in `src/main/resources/application-dev.properties`:
   
`spring.sql.init.mode=always`
`spring.sql.init.data-locations=classpath:sql/users.sql,classpath:sql/contact_details.sql,classpath:sql/instructors.sql,classpath:sql/courses.sql,classpath:sql/students.sql,classpath:sql/students_courses.sql`.

Then terminate and re-run the app, in order to populate sample data.
Comment out again in order to avoid reloading data on every run.

The app starts on `http://localhost:8080` by default.

### Profiles
- Default active profile is `dev`.

## API Documentation (OpenAPI/Swagger)
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Authentication
- Endpoint: `POST /api/auth/authenticate`

### Credentials for logging in as ADMIN:
- username: `alexadmin`
- password: `Alex123!@`

### Credentials for logging in as INSTRUCTOR:
- username: `emarkou`
- password: `Eleni123!@`

## Testing
- Unit and integration tests are under `src/test/java/...`
- H2 in-memory database is used in repository tests.
- Run: `gradlew[.bat] test`
- Coverage: `gradlew[.bat] jacocoTestReport` and open `build/reports/jacoco/test/html/index.html`

## Logging
- Configured via `src/main/resources/logback-spring.xml`.

## Known Ports
- HTTP server: 8080
- MySQL default: 3306 (configurable via env)

## License
No license file found in the repository.


