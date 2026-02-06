package gr.aueb.cf.cookingfactory.mapper;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import gr.aueb.cf.cookingfactory.core.enums.Role;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private Mapper mapper;

    private ContactDetails contactDetails;
    private Student student;
    private User user;
    private Instructor instructor;
    private Course course;

    @BeforeEach
    void setUp() {
        contactDetails = ContactDetails.builder()
                .id(1L)
                .city("Athens")
                .street("Stadiou")
                .streetNumber("28")
                .postalCode("10564")
                .email("giannis.papadopoulos@gmail.com")
                .phoneNumber("6914567890")
                .build();

        student = new Student();
        student.setId(1L);
        student.setUuid("student-uuid-1");
        student.setIsActive(true);
        student.setFirstname("Giannis");
        student.setLastname("Papadopoulos");
        student.setDateOfBirth(LocalDate.of(1996, 2, 14));
        student.setVat("111222333");
        student.setIdentityNumber("AK111222");
        student.setGender(Gender.MALE);
        student.setContactDetails(contactDetails);

        user = User.builder()
                .id(10L)
                .isActive(true)
                .username("dimitris.chef")
                .password("$2a$12$hashed")
                .role(Role.INSTRUCTOR)
                .vat("123456789")
                .build();

        instructor = Instructor.builder()
                .id(2L)
                .uuid("instructor-uuid-1")
                .isActive(true)
                .firstname("Vasilis")
                .lastname("Papadopoulos")
                .identityNumber("AX111222")
                .gender(Gender.MALE)
                .user(user)
                .contactDetails(ContactDetails.builder()
                        .id(2L)
                        .city("Thessaloniki")
                        .street("Tsimiski")
                        .streetNumber("45")
                        .postalCode("54622")
                        .email("v.papadopoulos@gmail.com")
                        .phoneNumber("6931234567")
                        .build())
                .build();

        course = new Course();
        course.setId(100L);
        course.setIsActive(true);
        course.setName("Fresh Pasta");
        course.setDescription("100% practical pasta cooking workshop.");
    }

    @Nested
    @DisplayName("mapToStudentReadOnlyDTO")
    class MapToStudentReadOnlyDTO {

        @Test
        @DisplayName("maps student with contact details and no courses to DTO")
        void mapsStudentWithoutCourses() {
            StudentReadOnlyDTO dto = mapper.mapToStudentReadOnlyDTO(student);

            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.uuid()).isEqualTo("student-uuid-1");
            assertThat(dto.isActive()).isTrue();
            assertThat(dto.firstname()).isEqualTo("Giannis");
            assertThat(dto.lastname()).isEqualTo("Papadopoulos");
            assertThat(dto.dateOfBirth()).isEqualTo(LocalDate.of(1996, 2, 14));
            assertThat(dto.vat()).isEqualTo("111222333");
            assertThat(dto.identityNumber()).isEqualTo("AK111222");
            assertThat(dto.gender()).isEqualTo(Gender.MALE);
            assertThat(dto.contactDetailsReadOnlyDTO()).isNotNull();
            assertThat(dto.contactDetailsReadOnlyDTO().id()).isEqualTo(1L);
            assertThat(dto.contactDetailsReadOnlyDTO().city()).isEqualTo("Athens");
            assertThat(dto.contactDetailsReadOnlyDTO().street()).isEqualTo("Stadiou");
            assertThat(dto.contactDetailsReadOnlyDTO().streetNumber()).isEqualTo("28");
            assertThat(dto.contactDetailsReadOnlyDTO().postalCode()).isEqualTo("10564");
            assertThat(dto.contactDetailsReadOnlyDTO().email()).isEqualTo("giannis.papadopoulos@gmail.com");
            assertThat(dto.contactDetailsReadOnlyDTO().phoneNumber()).isEqualTo("6914567890");
            assertThat(dto.courseIds()).isNull();
        }

        @Test
        @DisplayName("maps student with courses to DTO with course ids")
        void mapsStudentWithCourses() {
            student.addCourses(course);

            StudentReadOnlyDTO dto = mapper.mapToStudentReadOnlyDTO(student);

            assertThat(dto.courseIds()).containsExactly(100L);
            assertThat(dto.firstname()).isEqualTo("Giannis");
        }
    }

    @Nested
    @DisplayName("mapToStudentEntity from StudentInsertDTO")
    class MapToStudentEntityFromInsertDTO {

        @Test
        @DisplayName("maps insert DTO to student entity with contact details")
        void mapsInsertDTOToStudent() {
            ContactDetailsInsertDTO contactDto = ContactDetailsInsertDTO.builder()
                    .city("Athens")
                    .street("Stadiou")
                    .streetNumber("28")
                    .postalCode("10564")
                    .email("giannis.papadopoulos@gmail.com")
                    .phoneNumber("6914567890")
                    .build();

            StudentInsertDTO dto = StudentInsertDTO.builder()
                    .isActive(true)
                    .firstname("Giannis")
                    .lastname("Papadopoulos")
                    .dateOfBirth(LocalDate.of(1996, 2, 14))
                    .vat("111222333")
                    .identityNumber("AK111222")
                    .gender(Gender.MALE)
                    .contactDetailsInsertDTO(contactDto)
                    .courseIds(List.of())
                    .build();

            Student entity = mapper.mapToStudentEntity(dto);

            assertThat(entity.getIsActive()).isTrue();
            assertThat(entity.getFirstname()).isEqualTo("Giannis");
            assertThat(entity.getLastname()).isEqualTo("Papadopoulos");
            assertThat(entity.getDateOfBirth()).isEqualTo(LocalDate.of(1996, 2, 14));
            assertThat(entity.getVat()).isEqualTo("111222333");
            assertThat(entity.getIdentityNumber()).isEqualTo("AK111222");
            assertThat(entity.getGender()).isEqualTo(Gender.MALE);
            assertThat(entity.getContactDetails()).isNotNull();
            assertThat(entity.getContactDetails().getCity()).isEqualTo("Athens");
            assertThat(entity.getContactDetails().getEmail()).isEqualTo("giannis.papadopoulos@gmail.com");
        }
    }

    @Nested
    @DisplayName("mapToStudentEntity from StudentUpdateDTO")
    class MapToStudentEntityFromUpdateDTO {

        @Test
        @DisplayName("maps update DTO to student entity with id and contact details")
        void mapsUpdateDTOToStudent() {
            ContactDetailsUpdateDTO contactDto = ContactDetailsUpdateDTO.builder()
                    .id(5L)
                    .city("Athens")
                    .street("Stadiou")
                    .streetNumber("28")
                    .postalCode("10564")
                    .email("giannis.papadopoulos@gmail.com")
                    .phoneNumber("6914567890")
                    .build();

            StudentUpdateDTO dto = StudentUpdateDTO.builder()
                    .id(1L)
                    .uuid("student-uuid-1")
                    .isActive(true)
                    .firstname("Giannis")
                    .lastname("Papadopoulos")
                    .dateOfBirth(LocalDate.of(1996, 2, 14))
                    .vat("111222333")
                    .identityNumber("AK111222")
                    .gender(Gender.MALE)
                    .contactDetailsUpdateDTO(contactDto)
                    .courseIds(List.of())
                    .build();

            Student entity = mapper.mapToStudentEntity(dto);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getUuid()).isEqualTo("student-uuid-1");
            assertThat(entity.getFirstname()).isEqualTo("Giannis");
            assertThat(entity.getContactDetails().getId()).isEqualTo(5L);
            assertThat(entity.getContactDetails().getCity()).isEqualTo("Athens");
        }
    }

    @Nested
    @DisplayName("mapToInstructorReadOnlyDTO")
    class MapToInstructorReadOnlyDTO {

        @Test
        @DisplayName("maps instructor with user and contact details to DTO")
        void mapsInstructorToDTO() {
            InstructorReadOnlyDTO dto = mapper.mapToInstructorReadOnlyDTO(instructor);

            assertThat(dto.id()).isEqualTo(2L);
            assertThat(dto.uuid()).isEqualTo("instructor-uuid-1");
            assertThat(dto.isActive()).isTrue();
            assertThat(dto.firstname()).isEqualTo("Vasilis");
            assertThat(dto.lastname()).isEqualTo("Papadopoulos");
            assertThat(dto.identityNumber()).isEqualTo("AX111222");
            assertThat(dto.gender()).isEqualTo(Gender.MALE);
            assertThat(dto.userReadOnlyDTO()).isNotNull();
            assertThat(dto.userReadOnlyDTO().id()).isEqualTo(10L);
            assertThat(dto.userReadOnlyDTO().username()).isEqualTo("dimitris.chef");
            assertThat(dto.userReadOnlyDTO().vat()).isEqualTo("123456789");
            assertThat(dto.contactDetailsReadOnlyDTO()).isNotNull();
            assertThat(dto.contactDetailsReadOnlyDTO().id()).isEqualTo(2L);
            assertThat(dto.contactDetailsReadOnlyDTO().city()).isEqualTo("Thessaloniki");
            assertThat(dto.contactDetailsReadOnlyDTO().street()).isEqualTo("Tsimiski");
            assertThat(dto.contactDetailsReadOnlyDTO().streetNumber()).isEqualTo("45");
            assertThat(dto.contactDetailsReadOnlyDTO().postalCode()).isEqualTo("54622");
            assertThat(dto.contactDetailsReadOnlyDTO().email()).isEqualTo("v.papadopoulos@gmail.com");
        }
    }

    @Nested
    @DisplayName("mapToInstructorEntity from InstructorInsertDTO")
    class MapToInstructorEntityFromInsertDTO {

        @Test
        @DisplayName("maps insert DTO to instructor entity and encodes password")
        void mapsInsertDTOToInstructor() {
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            UserInsertDTO userDto = UserInsertDTO.builder()
                    .isActive(true)
                    .username("dimitris.chef")
                    .password("PlainPass1!")
                    .role(Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();

            ContactDetailsInsertDTO contactDto = ContactDetailsInsertDTO.builder()
                    .city("Thessaloniki")
                    .street("Tsimiski")
                    .streetNumber("45")
                    .postalCode("54622")
                    .email("v.papadopoulos@gmail.com")
                    .phoneNumber("6931234567")
                    .build();

            InstructorInsertDTO dto = InstructorInsertDTO.builder()
                    .isActive(true)
                    .firstname("Vasilis")
                    .lastname("Papadopoulos")
                    .identityNumber("AX111222")
                    .gender(Gender.MALE)
                    .userInsertDTO(userDto)
                    .contactDetailsInsertDTO(contactDto)
                    .build();

            Instructor entity = mapper.mapToInstructorEntity(dto);

            assertThat(entity.getFirstname()).isEqualTo("Vasilis");
            assertThat(entity.getLastname()).isEqualTo("Papadopoulos");
            assertThat(entity.getUser()).isNotNull();
            assertThat(entity.getUser().getUsername()).isEqualTo("dimitris.chef");
            assertThat(entity.getUser().getPassword()).isEqualTo("encodedPassword");
            assertThat(entity.getContactDetails().getCity()).isEqualTo("Thessaloniki");
        }
    }

    @Nested
    @DisplayName("mapToInstructorEntity from InstructorUpdateDTO")
    class MapToInstructorEntityFromUpdateDTO {

        @Test
        @DisplayName("encodes new password when password is provided")
        void encodesNewPasswordWhenProvided() {
            when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

            UserUpdateDTO userDto = UserUpdateDTO.builder()
                    .id(10L)
                    .isActive(true)
                    .username("dimitris.chef")
                    .password("NewPass1!")
                    .role(Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();

            ContactDetailsUpdateDTO contactDto = ContactDetailsUpdateDTO.builder()
                    .id(20L)
                    .city("Thessaloniki")
                    .street("Tsimiski")
                    .streetNumber("45")
                    .postalCode("54622")
                    .email("v.papadopoulos@gmail.com")
                    .phoneNumber("6931234567")
                    .build();

            InstructorUpdateDTO dto = InstructorUpdateDTO.builder()
                    .id(2L)
                    .uuid("instructor-uuid-1")
                    .isActive(true)
                    .firstname("Vasilis")
                    .lastname("Papadopoulos")
                    .identityNumber("AX111222")
                    .gender(Gender.MALE)
                    .userUpdateDTO(userDto)
                    .contactDetailsUpdateDTO(contactDto)
                    .build();

            Instructor entity = mapper.mapToInstructorEntity(dto, "oldHashed");

            assertThat(entity.getUser().getPassword()).isEqualTo("newEncoded");
        }

        @Test
        @DisplayName("keeps existing password when password is null")
        void keepsExistingPasswordWhenNull() {
            UserUpdateDTO userDto = UserUpdateDTO.builder()
                    .id(10L)
                    .isActive(true)
                    .username("dimitris.chef")
                    .password(null)
                    .role(Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();

            ContactDetailsUpdateDTO contactDto = ContactDetailsUpdateDTO.builder()
                    .id(20L)
                    .city("Thessaloniki")
                    .email("v.papadopoulos@gmail.com")
                    .phoneNumber("6931234567")
                    .build();

            InstructorUpdateDTO dto = InstructorUpdateDTO.builder()
                    .id(2L)
                    .uuid("instructor-uuid-1")
                    .isActive(true)
                    .firstname("Vasilis")
                    .lastname("Papadopoulos")
                    .identityNumber("AX111222")
                    .gender(Gender.MALE)
                    .userUpdateDTO(userDto)
                    .contactDetailsUpdateDTO(contactDto)
                    .build();

            Instructor entity = mapper.mapToInstructorEntity(dto, "existingHashedPassword");

            assertThat(entity.getUser().getPassword()).isEqualTo("existingHashedPassword");
        }

        @Test
        @DisplayName("keeps existing password when password is blank")
        void keepsExistingPasswordWhenBlank() {
            UserUpdateDTO userDto = UserUpdateDTO.builder()
                    .id(10L)
                    .isActive(true)
                    .username("dimitris.chef")
                    .password("   ")
                    .role(Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();

            ContactDetailsUpdateDTO contactDto = ContactDetailsUpdateDTO.builder()
                    .id(20L)
                    .city("Thessaloniki")
                    .email("v.papadopoulos@gmail.com")
                    .phoneNumber("6931234567")
                    .build();

            InstructorUpdateDTO dto = InstructorUpdateDTO.builder()
                    .id(2L)
                    .uuid("instructor-uuid-1")
                    .isActive(true)
                    .firstname("Vasilis")
                    .lastname("Papadopoulos")
                    .identityNumber("AX111222")
                    .gender(Gender.MALE)
                    .userUpdateDTO(userDto)
                    .contactDetailsUpdateDTO(contactDto)
                    .build();

            Instructor entity = mapper.mapToInstructorEntity(dto, "existingHashedPassword");

            assertThat(entity.getUser().getPassword()).isEqualTo("existingHashedPassword");
        }
    }

    @Nested
    @DisplayName("mapToCourseReadOnlyDTO")
    class MapToCourseReadOnlyDTO {

        @Test
        @DisplayName("maps course with instructor to DTO")
        void mapsCourseWithInstructor() {
            course.setInstructor(instructor);

            CourseReadOnlyDTO dto = mapper.mapToCourseReadOnlyDTO(course);

            assertThat(dto.id()).isEqualTo(100L);
            assertThat(dto.isActive()).isTrue();
            assertThat(dto.name()).isEqualTo("Fresh Pasta");
            assertThat(dto.description()).contains("practical pasta");
            assertThat(dto.instructorId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("maps course without instructor to DTO with null instructorId")
        void mapsCourseWithoutInstructor() {
            CourseReadOnlyDTO dto = mapper.mapToCourseReadOnlyDTO(course);

            assertThat(dto.name()).isEqualTo("Fresh Pasta");
            assertThat(dto.instructorId()).isNull();
        }
    }

    @Nested
    @DisplayName("mapToCourseEntity from CourseInsertDTO")
    class MapToCourseEntityFromInsertDTO {

        @Test
        @DisplayName("maps insert DTO to course entity")
        void mapsInsertDTOToCourse() {
            CourseInsertDTO dto = CourseInsertDTO.builder()
                    .isActive(true)
                    .name("Fresh Pasta")
                    .description("100% practical pasta cooking workshop, inspired by the best Italian recipes.")
                    .instructorId(null)
                    .build();

            Course entity = mapper.mapToCourseEntity(dto);

            assertThat(entity.getIsActive()).isTrue();
            assertThat(entity.getName()).isEqualTo("Fresh Pasta");
            assertThat(entity.getDescription()).isEqualTo("100% practical pasta cooking workshop, inspired by the best Italian recipes.");
        }
    }

    @Nested
    @DisplayName("mapToCourseEntity from CourseUpdateDTO")
    class MapToCourseEntityFromUpdateDTO {

        @Test
        @DisplayName("maps update DTO to course entity with id")
        void mapsUpdateDTOToCourse() {
            CourseUpdateDTO dto = CourseUpdateDTO.builder()
                    .id(100L)
                    .isActive(true)
                    .name("Mediterranean Cuisine")
                    .description("Traditional Mediterranean cooking techniques.")
                    .instructorId(2L)
                    .build();

            Course entity = mapper.mapToCourseEntity(dto);

            assertThat(entity.getId()).isEqualTo(100L);
            assertThat(entity.getName()).isEqualTo("Mediterranean Cuisine");
            assertThat(entity.getDescription()).isEqualTo("Traditional Mediterranean cooking techniques.");
        }
    }
}
