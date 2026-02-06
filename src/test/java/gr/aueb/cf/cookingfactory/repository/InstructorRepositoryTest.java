package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import gr.aueb.cf.cookingfactory.core.enums.Role;
import gr.aueb.cf.cookingfactory.model.ContactDetails;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class InstructorRepositoryTest {

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private ContactDetails contactDetails;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .isActive(true)
                .username("dimitris.chef")
                .password("$2a$12$hashed")
                .role(Role.INSTRUCTOR)
                .vat("123456789")
                .build();
        user = userRepository.save(user);

        contactDetails = ContactDetails.builder()
                .city("Thessaloniki")
                .street("Tsimiski")
                .streetNumber("45")
                .postalCode("54622")
                .email("v.papadopoulos@gmail.com")
                .phoneNumber("6931234567")
                .build();

        instructor = Instructor.builder()
                .isActive(true)
                .firstname("Vasilis")
                .lastname("Papadopoulos")
                .identityNumber("AX111222")
                .gender(Gender.MALE)
                .user(user)
                .contactDetails(contactDetails)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists instructor with user and contact details")
        void save_persistsInstructor() {
            Instructor saved = instructorRepository.save(instructor);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUuid()).isNotNull();
            assertThat(saved.getFirstname()).isEqualTo("Vasilis");
            assertThat(saved.getLastname()).isEqualTo("Papadopoulos");
            assertThat(saved.getIdentityNumber()).isEqualTo("AX111222");
            assertThat(saved.getUser()).isNotNull();
            assertThat(saved.getUser().getUsername()).isEqualTo("dimitris.chef");
            assertThat(saved.getContactDetails()).isNotNull();
            assertThat(saved.getContactDetails().getCity()).isEqualTo("Thessaloniki");
        }

        @Test
        @DisplayName("duplicate identity number throws")
        void save_duplicateIdentityNumber_throws() {
            instructorRepository.save(instructor);

            User user2 = userRepository.save(User.builder()
                    .isActive(true)
                    .username("katerina.chef")
                    .password("$2a$12$hash")
                    .role(Role.INSTRUCTOR)
                    .vat("234567890")
                    .build());

            Instructor other = Instructor.builder()
                    .isActive(true)
                    .firstname("Katerina")
                    .lastname("Dimitriou")
                    .identityNumber("AX111222")
                    .gender(Gender.FEMALE)
                    .user(user2)
                    .contactDetails(ContactDetails.builder().city("Patra").email("k.d@gmail.com").phoneNumber("6940000000").build())
                    .build();

            assertThatThrownBy(() -> instructorRepository.saveAndFlush(other))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findByUuid")
    class FindByUuid {

        @Test
        @DisplayName("returns instructor when uuid exists")
        void findByUuid_found() {
            Instructor saved = instructorRepository.save(instructor);
            String uuid = saved.getUuid();

            Optional<Instructor> result = instructorRepository.findByUuid(uuid);

            assertThat(result).isPresent();
            assertThat(result.get().getFirstname()).isEqualTo("Vasilis");
            assertThat(result.get().getLastname()).isEqualTo("Papadopoulos");
        }

        @Test
        @DisplayName("returns empty when uuid does not exist")
        void findByUuid_notFound() {
            Optional<Instructor> result = instructorRepository.findByUuid("unknown-uuid");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdentityNumber")
    class FindByIdentityNumber {

        @Test
        @DisplayName("returns instructor when identity number exists")
        void findByIdentityNumber_found() {
            instructorRepository.save(instructor);

            Optional<Instructor> result = instructorRepository.findByIdentityNumber("AX111222");

            assertThat(result).isPresent();
            assertThat(result.get().getFirstname()).isEqualTo("Vasilis");
        }

        @Test
        @DisplayName("returns empty when identity number does not exist")
        void findByIdentityNumber_notFound() {
            Optional<Instructor> result = instructorRepository.findByIdentityNumber("XX000000");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUser_Id")
    class FindByUser_Id {

        @Test
        @DisplayName("returns instructor when user id exists")
        void findByUser_Id_found() {
            Instructor saved = instructorRepository.save(instructor);
            Long userId = saved.getUser().getId();

            Optional<Instructor> result = instructorRepository.findByUser_Id(userId);

            assertThat(result).isPresent();
            assertThat(result.get().getIdentityNumber()).isEqualTo("AX111222");
        }

        @Test
        @DisplayName("returns empty when user id has no instructor")
        void findByUser_Id_notFound() {
            Optional<Instructor> result = instructorRepository.findByUser_Id(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByIdentityNumber")
    class ExistsByIdentityNumber {

        @Test
        @DisplayName("returns true when identity number exists")
        void existsByIdentityNumber_true() {
            instructorRepository.save(instructor);

            assertThat(instructorRepository.existsByIdentityNumber("AX111222")).isTrue();
        }

        @Test
        @DisplayName("returns false when identity number does not exist")
        void existsByIdentityNumber_false() {
            assertThat(instructorRepository.existsByIdentityNumber("XX000000")).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll / findById")
    class FindAllAndById {

        @Test
        @DisplayName("findAll returns all instructors")
        void findAll_returnsAll() {
            instructorRepository.save(instructor);

            User user2 = userRepository.save(User.builder()
                    .isActive(true)
                    .username("maria.chef")
                    .password("$2a$12$hash")
                    .role(Role.INSTRUCTOR)
                    .vat("234567890")
                    .build());
            Instructor i2 = Instructor.builder()
                    .isActive(true)
                    .firstname("Maria")
                    .lastname("Stamatiou")
                    .identityNumber("AX222333")
                    .gender(Gender.FEMALE)
                    .user(user2)
                    .contactDetails(ContactDetails.builder().city("Athens").email("m.stamatiou@gmail.com").phoneNumber("6910000000").build())
                    .build();
            instructorRepository.save(i2);

            List<Instructor> all = instructorRepository.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("findById returns instructor when id exists")
        void findById_found() {
            Instructor saved = instructorRepository.save(instructor);

            Optional<Instructor> result = instructorRepository.findById(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getFirstname()).isEqualTo("Vasilis");
        }

        @Test
        @DisplayName("findById returns empty when id does not exist")
        void findById_notFound() {
            Optional<Instructor> result = instructorRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }
}
