package gr.aueb.cf.cookingfactory;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * A Java Spring Boot REST API for managing a cooking school entities (Instructors - Students - Courses etc.).
 * Included: JWT-based authentication, MySQL persistence, endpoints for authentication and
 * role-based entities operations (OpenAPI/Swagger UI), Gradle as build tool.
 */

@SpringBootApplication
@EnableJpaAuditing
public class CookingfactoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(CookingfactoryApplication.class, args);
	}

}
