package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import gr.aueb.cf.cookingfactory.core.enums.Role;
import gr.aueb.cf.cookingfactory.model.ContactDetails;
import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private UserRepository userRepository;

    private Course course;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setIsActive(true);
        course.setName("Fresh Pasta");
        course.setDescription("100% practical pasta cooking workshop, inspired by the best Italian recipes and techniques.");

        User user = userRepository.save(User.builder()
                .isActive(true)
                .username("chef.pasta")
                .password("$2a$12$hash")
                .role(Role.INSTRUCTOR)
                .vat("987654321")
                .build());

        instructor = instructorRepository.save(Instructor.builder()
                .isActive(true)
                .firstname("Dimitris")
                .lastname("Papadopoulos")
                .identityNumber("AX555666")
                .gender(Gender.MALE)
                .user(user)
                .contactDetails(ContactDetails.builder()
                        .city("Athens")
                        .street("Ermou")
                        .streetNumber("10")
                        .postalCode("10563")
                        .email("d.papadopoulos@cooking.gr")
                        .phoneNumber("6901234567")
                        .build())
                .build());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists course without instructor")
        void save_withoutInstructor() {
            Course saved = courseRepository.save(course);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Fresh Pasta");
            assertThat(saved.getDescription()).contains("practical pasta");
            assertThat(saved.getInstructor()).isNull();
        }

        @Test
        @DisplayName("persists course with instructor")
        void save_withInstructor() {
            course.setInstructor(instructor);
            Course saved = courseRepository.save(course);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getInstructor()).isNotNull();
            assertThat(saved.getInstructor().getFirstname()).isEqualTo("Dimitris");
            assertThat(saved.getInstructor().getId()).isEqualTo(instructor.getId());
        }

    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("returns course when name exists")
        void findByName_found() {
            courseRepository.save(course);

            Optional<Course> result = courseRepository.findByName("Fresh Pasta");

            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).contains("Italian recipes");
        }

        @Test
        @DisplayName("returns empty when name does not exist")
        void findByName_notFound() {
            Optional<Course> result = courseRepository.findByName("Greek Cuisine");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByInstructor_Id")
    class FindByInstructor_Id {

        @Test
        @DisplayName("returns courses when instructor has courses")
        void findByInstructor_Id_found() {
            course.setInstructor(instructor);
            courseRepository.save(course);

            Course c2 = new Course();
            c2.setIsActive(true);
            c2.setName("Mediterranean Cuisine");
            c2.setDescription("Traditional Mediterranean cooking.");
            c2.setInstructor(instructor);
            courseRepository.save(c2);

            List<Course> result = courseRepository.findByInstructor_Id(instructor.getId());

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Course::getName).containsExactlyInAnyOrder("Fresh Pasta", "Mediterranean Cuisine");
        }

        @Test
        @DisplayName("returns empty list when instructor has no courses")
        void findByInstructor_Id_empty() {
            List<Course> result = courseRepository.findByInstructor_Id(instructor.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for unknown instructor id")
        void findByInstructor_Id_unknownId() {
            List<Course> result = courseRepository.findByInstructor_Id(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll / findById")
    class FindAllAndById {

        @Test
        @DisplayName("findAll returns all courses")
        void findAll_returnsAll() {
            courseRepository.save(course);

            Course c2 = new Course();
            c2.setIsActive(true);
            c2.setName("Greek Cuisine");
            c2.setDescription("Basic techniques of Greek traditional cooking.");
            courseRepository.save(c2);

            List<Course> all = courseRepository.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("findById returns course when id exists")
        void findById_found() {
            Course saved = courseRepository.save(course);

            Optional<Course> result = courseRepository.findById(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Fresh Pasta");
        }

        @Test
        @DisplayName("findById returns empty when id does not exist")
        void findById_notFound() {
            Optional<Course> result = courseRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }
}
