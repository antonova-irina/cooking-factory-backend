package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import gr.aueb.cf.cookingfactory.model.ContactDetails;
import gr.aueb.cf.cookingfactory.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private ContactDetails contactDetails;
    private Student student;

    @BeforeEach
    void setUp() {
        contactDetails = ContactDetails.builder()
                .city("Athens")
                .street("Stadiou")
                .streetNumber("28")
                .postalCode("10564")
                .email("giannis.papadopoulos@gmail.com")
                .phoneNumber("6914567890")
                .build();

        student = new Student();
        student.setIsActive(true);
        student.setFirstname("Giannis");
        student.setLastname("Papadopoulos");
        student.setDateOfBirth(LocalDate.of(1996, 2, 14));
        student.setVat("111222333");
        student.setIdentityNumber("AK111222");
        student.setGender(Gender.MALE);
        student.setContactDetails(contactDetails);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists student with contact details")
        void save_persistsStudent() {
            Student saved = studentRepository.save(student);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUuid()).isNotNull();
            assertThat(saved.getFirstname()).isEqualTo("Giannis");
            assertThat(saved.getLastname()).isEqualTo("Papadopoulos");
            assertThat(saved.getVat()).isEqualTo("111222333");
            assertThat(saved.getContactDetails()).isNotNull();
            assertThat(saved.getContactDetails().getCity()).isEqualTo("Athens");
        }

        @Test
        @DisplayName("duplicate VAT throws")
        void save_duplicateVat_throws() {
            studentRepository.save(student);

            Student other = new Student();
            other.setIsActive(true);
            other.setFirstname("Maria");
            other.setLastname("Konstantinou");
            other.setDateOfBirth(LocalDate.of(1998, 5, 20));
            other.setVat("111222333");
            other.setIdentityNumber("AK999888");
            other.setGender(Gender.FEMALE);
            other.setContactDetails(ContactDetails.builder()
                    .city("Thessaloniki")
                    .email("maria.konstantinou@gmail.com")
                    .phoneNumber("6930000000")
                    .build());

            assertThatThrownBy(() -> studentRepository.saveAndFlush(other))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("duplicate identity number throws")
        void save_duplicateIdentityNumber_throws() {
            studentRepository.save(student);

            Student other = new Student();
            other.setIsActive(true);
            other.setFirstname("Eleni");
            other.setLastname("Kyriakou");
            other.setDateOfBirth(LocalDate.of(1997, 8, 10));
            other.setVat("999888777");
            other.setIdentityNumber("AK111222");
            other.setGender(Gender.FEMALE);
            other.setContactDetails(ContactDetails.builder()
                    .city("Patra")
                    .email("eleni.kyriakou@gmail.com")
                    .phoneNumber("6940000000")
                    .build());

            assertThatThrownBy(() -> studentRepository.saveAndFlush(other))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findByUuid")
    class FindByUuid {

        @Test
        @DisplayName("returns student when uuid exists")
        void findByUuid_found() {
            Student saved = studentRepository.save(student);
            String uuid = saved.getUuid();

            Optional<Student> result = studentRepository.findByUuid(uuid);

            assertThat(result).isPresent();
            assertThat(result.get().getFirstname()).isEqualTo("Giannis");
            assertThat(result.get().getLastname()).isEqualTo("Papadopoulos");
        }

        @Test
        @DisplayName("returns empty when uuid does not exist")
        void findByUuid_notFound() {
            Optional<Student> result = studentRepository.findByUuid("unknown-uuid");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByVat")
    class FindByVat {

        @Test
        @DisplayName("returns student when VAT exists")
        void findByVat_found() {
            studentRepository.save(student);

            Optional<Student> result = studentRepository.findByVat("111222333");

            assertThat(result).isPresent();
            assertThat(result.get().getFirstname()).isEqualTo("Giannis");
        }

        @Test
        @DisplayName("returns empty when VAT does not exist")
        void findByVat_notFound() {
            Optional<Student> result = studentRepository.findByVat("000000000");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdentityNumber")
    class FindByIdentityNumber {

        @Test
        @DisplayName("returns student when identity number exists")
        void findByIdentityNumber_found() {
            studentRepository.save(student);

            Optional<Student> result = studentRepository.findByIdentityNumber("AK111222");

            assertThat(result).isPresent();
            assertThat(result.get().getLastname()).isEqualTo("Papadopoulos");
        }

        @Test
        @DisplayName("returns empty when identity number does not exist")
        void findByIdentityNumber_notFound() {
            Optional<Student> result = studentRepository.findByIdentityNumber("XX000000");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsBy")
    class ExistsBy {

        @Test
        @DisplayName("existsByVat returns true when VAT exists")
        void existsByVat_true() {
            studentRepository.save(student);

            assertThat(studentRepository.existsByVat("111222333")).isTrue();
        }

        @Test
        @DisplayName("existsByVat returns false when VAT does not exist")
        void existsByVat_false() {
            assertThat(studentRepository.existsByVat("000000000")).isFalse();
        }

        @Test
        @DisplayName("existsByIdentityNumber returns true when identity exists")
        void existsByIdentityNumber_true() {
            studentRepository.save(student);

            assertThat(studentRepository.existsByIdentityNumber("AK111222")).isTrue();
        }

        @Test
        @DisplayName("existsByIdentityNumber returns false when identity does not exist")
        void existsByIdentityNumber_false() {
            assertThat(studentRepository.existsByIdentityNumber("XX000000")).isFalse();
        }

        @Test
        @DisplayName("existsByLastname returns true when lastname exists")
        void existsByLastname_true() {
            studentRepository.save(student);

            assertThat(studentRepository.existsByLastname("Papadopoulos")).isTrue();
        }

        @Test
        @DisplayName("existsByLastname returns false when lastname does not exist")
        void existsByLastname_false() {
            assertThat(studentRepository.existsByLastname("Unknown")).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll / findById")
    class FindAllAndById {

        @Test
        @DisplayName("findAll returns all students")
        void findAll_returnsAll() {
            studentRepository.save(student);

            Student s2 = new Student();
            s2.setIsActive(true);
            s2.setFirstname("Maria");
            s2.setLastname("Nikolaou");
            s2.setDateOfBirth(LocalDate.of(1999, 3, 5));
            s2.setVat("222333444");
            s2.setIdentityNumber("AK222333");
            s2.setGender(Gender.FEMALE);
            s2.setContactDetails(ContactDetails.builder().city("Larisa").email("m.nikolaou@gmail.com").phoneNumber("6950000000").build());
            studentRepository.save(s2);

            List<Student> all = studentRepository.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("findById returns student when id exists")
        void findById_found() {
            Student saved = studentRepository.save(student);

            Optional<Student> result = studentRepository.findById(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getFirstname()).isEqualTo("Giannis");
        }

        @Test
        @DisplayName("findById returns empty when id does not exist")
        void findById_notFound() {
            Optional<Student> result = studentRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }
}
