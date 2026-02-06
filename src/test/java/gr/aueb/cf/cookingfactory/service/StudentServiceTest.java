package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.core.filters.StudentFilters;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.mapper.Mapper;
import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Student;
import gr.aueb.cf.cookingfactory.repository.CourseRepository;
import gr.aueb.cf.cookingfactory.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private StudentService studentService;

    private Student student;
    private StudentReadOnlyDTO studentReadOnlyDTO;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setUuid("uuid-123");
        student.setIsActive(true);
        student.setFirstname("Giannis");
        student.setLastname("Papadopoulos");
        student.setDateOfBirth(LocalDate.of(2000, 1, 15));
        student.setVat("123456789");
        student.setIdentityNumber("ID123");

        studentReadOnlyDTO = StudentReadOnlyDTO.builder()
                .id(1L)
                .uuid("uuid-123")
                .isActive(true)
                .firstname("Giannis")
                .lastname("Papadopoulos")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .vat("123456789")
                .identityNumber("ID123")
                .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                .contactDetailsReadOnlyDTO(ContactDetailsReadOnlyDTO.builder().id(1L).city("Athens").email("a@b.gr").phoneNumber("123").build())
                .courseIds(List.of(10L))
                .build();

        course = new Course();
        course.setId(10L);
    }

    @Nested
    @DisplayName("saveStudent")
    class SaveStudent {

        @Test
        @DisplayName("saves student when VAT and identity number are unique")
        void saveStudent_success() throws Exception {
            ContactDetailsInsertDTO contactDTO = ContactDetailsInsertDTO.builder()
                    .city("Athens")
                    .email("a@b.gr")
                    .phoneNumber("123")
                    .build();
            StudentInsertDTO insertDTO = StudentInsertDTO.builder()
                    .isActive(true)
                    .firstname("Maria")
                    .lastname("Konstantinou")
                    .dateOfBirth(LocalDate.of(2001, 5, 20))
                    .vat("987654321")
                    .identityNumber("ID456")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .contactDetailsInsertDTO(contactDTO)
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findByVat("987654321")).thenReturn(Optional.empty());
            when(studentRepository.findByIdentityNumber("ID456")).thenReturn(Optional.empty());
            when(mapper.mapToStudentEntity(insertDTO)).thenReturn(new Student());
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
                Student s = inv.getArgument(0);
                s.setId(2L);
                return s;
            });
            when(mapper.mapToStudentReadOnlyDTO(any(Student.class))).thenReturn(
                    StudentReadOnlyDTO.builder().id(2L).firstname("Maria").lastname("Konstantinou").identityNumber("ID456").gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE).build());

            StudentReadOnlyDTO result = studentService.saveStudent(insertDTO);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(2L);
            verify(studentRepository).findByVat("987654321");
            verify(studentRepository).findByIdentityNumber("ID456");
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("saves student with courses when courseIds provided")
        void saveStudent_success_withCourses() throws Exception {
            ContactDetailsInsertDTO contactDTO = ContactDetailsInsertDTO.builder()
                    .city("Athens")
                    .email("a@b.gr")
                    .phoneNumber("123")
                    .build();
            StudentInsertDTO insertDTO = StudentInsertDTO.builder()
                    .isActive(true)
                    .firstname("Maria")
                    .lastname("Konstantinou")
                    .dateOfBirth(LocalDate.of(2001, 5, 20))
                    .vat("987654321")
                    .identityNumber("ID456")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .contactDetailsInsertDTO(contactDTO)
                    .courseIds(List.of(10L))
                    .build();

            when(studentRepository.findByVat("987654321")).thenReturn(Optional.empty());
            when(studentRepository.findByIdentityNumber("ID456")).thenReturn(Optional.empty());
            when(mapper.mapToStudentEntity(insertDTO)).thenReturn(new Student());
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.mapToStudentReadOnlyDTO(any(Student.class))).thenReturn(studentReadOnlyDTO);

            StudentReadOnlyDTO result = studentService.saveStudent(insertDTO);

            assertThat(result).isNotNull();
            verify(courseRepository).findById(10L);
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when VAT already exists")
        void saveStudent_duplicateVat_throwsException() {
            StudentInsertDTO insertDTO = StudentInsertDTO.builder()
                    .isActive(true)
                    .firstname("Eleni")
                    .lastname("Kyriakou")
                    .dateOfBirth(LocalDate.of(2001, 5, 20))
                    .vat("123456789")
                    .identityNumber("ID456")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .contactDetailsInsertDTO(ContactDetailsInsertDTO.builder().city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findByVat("123456789")).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> studentService.saveStudent(insertDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("VAT")
                    .hasMessageContaining("123456789");
            verify(studentRepository).findByVat("123456789");
            verify(studentRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when identity number already exists")
        void saveStudent_duplicateIdentityNumber_throwsException() {
            StudentInsertDTO insertDTO = StudentInsertDTO.builder()
                    .isActive(true)
                    .firstname("Antonia")
                    .lastname("Nikolaou")
                    .dateOfBirth(LocalDate.of(2001, 5, 20))
                    .vat("987654321")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .contactDetailsInsertDTO(ContactDetailsInsertDTO.builder().city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findByVat("987654321")).thenReturn(Optional.empty());
            when(studentRepository.findByIdentityNumber("ID123")).thenReturn(Optional.of(student));

            assertThatThrownBy(() -> studentService.saveStudent(insertDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("identity number")
                    .hasMessageContaining("ID123");
            verify(studentRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when courseId is invalid")
        void saveStudent_invalidCourseId_throwsException() {
            StudentInsertDTO insertDTO = StudentInsertDTO.builder()
                    .isActive(true)
                    .firstname("Maria")
                    .lastname("Konstantinou")
                    .dateOfBirth(LocalDate.of(2001, 5, 20))
                    .vat("987654321")
                    .identityNumber("ID456")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .contactDetailsInsertDTO(ContactDetailsInsertDTO.builder().city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of(999L))
                    .build();

            when(studentRepository.findByVat("987654321")).thenReturn(Optional.empty());
            when(studentRepository.findByIdentityNumber("ID456")).thenReturn(Optional.empty());
            when(mapper.mapToStudentEntity(insertDTO)).thenReturn(new Student());
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.saveStudent(insertDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Course")
                    .hasMessageContaining("999");
            verify(courseRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("updateStudent")
    class UpdateStudent {

        @Test
        @DisplayName("updates student successfully")
        void updateStudent_success() throws Exception {
            ContactDetailsUpdateDTO contactUpdate = ContactDetailsUpdateDTO.builder()
                    .id(1L)
                    .city("Thessaloniki")
                    .email("b@c.gr")
                    .phoneNumber("456")
                    .build();
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(false)
                    .firstname("Giannis")
                    .lastname("Nikolaou")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .vat("123456789")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .contactDetailsUpdateDTO(contactUpdate)
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(mapper.mapToStudentEntity(updateDTO)).thenReturn(new Student());
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.mapToStudentReadOnlyDTO(any(Student.class))).thenReturn(studentReadOnlyDTO);

            StudentReadOnlyDTO result = studentService.updateStudent(updateDTO);

            assertThat(result).isNotNull();
            verify(studentRepository).findById(1L);
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when student does not exist")
        void updateStudent_notFound_throwsException() {
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .id(999L)
                    .uuid("uuid-999")
                    .isActive(true)
                    .firstname("Nikos")
                    .lastname("Dimitriou")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .vat("111111111")
                    .identityNumber("ID1")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.updateStudent(updateDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("999");
            verify(studentRepository).findById(999L);
            verify(studentRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when new VAT is taken by another student")
        void updateStudent_duplicateVat_throwsException() {
            Student other = new Student();
            other.setId(2L);
            other.setVat("999999999");
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Giannis")
                    .lastname("Papadopoulos")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .vat("999999999")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(studentRepository.findByVat("999999999")).thenReturn(Optional.of(other));

            assertThatThrownBy(() -> studentService.updateStudent(updateDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("VAT")
                    .hasMessageContaining("999999999");
            verify(studentRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when new identity number is taken")
        void updateStudent_duplicateIdentityNumber_throwsException() {
            Student other = new Student();
            other.setId(2L);
            other.setIdentityNumber("ID999");
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Giannis")
                    .lastname("Papadopoulos")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .vat("123456789")
                    .identityNumber("ID999")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            // VAT unchanged: service short-circuits, so findByVat not called
            when(studentRepository.findByIdentityNumber("ID999")).thenReturn(Optional.of(other));

            assertThatThrownBy(() -> studentService.updateStudent(updateDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("identity number")
                    .hasMessageContaining("ID999");
            verify(studentRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("allows same VAT and identity number when updating same student")
        void updateStudent_sameVatAndIdentity_success() throws Exception {
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Giannis")
                    .lastname("Papadopoulos")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .vat("123456789")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of())
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            // Same VAT/identity: service short-circuits, so findByVat/findByIdentityNumber are not called
            when(mapper.mapToStudentEntity(updateDTO)).thenReturn(new Student());
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.mapToStudentReadOnlyDTO(any(Student.class))).thenReturn(studentReadOnlyDTO);

            StudentReadOnlyDTO result = studentService.updateStudent(updateDTO);

            assertThat(result).isNotNull();
            verify(studentRepository).findById(1L);
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when courseId in update is invalid")
        void updateStudent_invalidCourseId_throwsException() {
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Giannis")
                    .lastname("Papadopoulos")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .vat("123456789")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .courseIds(List.of(999L))
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            // Same VAT/identity: service short-circuits, so no findByVat/findByIdentityNumber
            when(mapper.mapToStudentEntity(updateDTO)).thenReturn(new Student());
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.updateStudent(updateDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Course")
                    .hasMessageContaining("999");
            verify(courseRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("getOneStudent")
    class GetOneStudent {

        @Test
        @DisplayName("returns student when uuid exists")
        void getOneStudent_success() throws Exception {
            when(studentRepository.findByUuid("uuid-123")).thenReturn(Optional.of(student));
            when(mapper.mapToStudentReadOnlyDTO(student)).thenReturn(studentReadOnlyDTO);

            StudentReadOnlyDTO result = studentService.getOneStudent("uuid-123");

            assertThat(result).isNotNull();
            assertThat(result.uuid()).isEqualTo("uuid-123");
            assertThat(result.firstname()).isEqualTo("Giannis");
            verify(studentRepository).findByUuid("uuid-123");
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when uuid does not exist")
        void getOneStudent_notFound_throwsException() {
            when(studentRepository.findByUuid("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.getOneStudent("unknown"))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("unknown");
            verify(studentRepository).findByUuid("unknown");
        }
    }

    @Nested
    @DisplayName("getAllStudents")
    class GetAllStudents {

        @Test
        @DisplayName("returns empty list when no students")
        void getAllStudents_emptyList() {
            when(studentRepository.findAll()).thenReturn(List.of());

            List<StudentReadOnlyDTO> result = studentService.getAllStudents();

            assertThat(result).isEmpty();
            verify(studentRepository).findAll();
        }

        @Test
        @DisplayName("returns all students")
        void getAllStudents_returnsAll() {
            when(studentRepository.findAll()).thenReturn(List.of(student));
            when(mapper.mapToStudentReadOnlyDTO(student)).thenReturn(studentReadOnlyDTO);

            List<StudentReadOnlyDTO> result = studentService.getAllStudents();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).firstname()).isEqualTo("Giannis");
            verify(studentRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getPaginatedStudents")
    class GetPaginatedStudents {

        @Test
        @DisplayName("returns paginated students")
        void getPaginatedStudents_success() {
            Pageable pageable = PageRequest.of(0, 10);
            when(studentRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(student), pageable, 1));
            when(mapper.mapToStudentReadOnlyDTO(student)).thenReturn(studentReadOnlyDTO);

            Paginated<StudentReadOnlyDTO> result = studentService.getPaginatedStudents(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getData()).hasSize(1);
            assertThat(result.getCurrentPage()).isZero();
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(studentRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("returns empty page when no students")
        void getPaginatedStudents_emptyPage() {
            Pageable pageable = PageRequest.of(1, 5);
            when(studentRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Paginated<StudentReadOnlyDTO> result = studentService.getPaginatedStudents(1, 5);

            assertThat(result.getData()).isEmpty();
            assertThat(result.getCurrentPage()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("getStudentsFilteredPaginated")
    class GetStudentsFilteredPaginated {

        @Test
        @DisplayName("returns filtered and paginated students")
        void getStudentsFilteredPaginated_success() {
            StudentFilters filters = StudentFilters.builder()
                    .lastname("Papadopoulos")
                    .build();
            filters.setPage(0);
            filters.setPageSize(10);

            when(studentRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(student)));
            when(mapper.mapToStudentReadOnlyDTO(student)).thenReturn(studentReadOnlyDTO);

            Paginated<StudentReadOnlyDTO> result = studentService.getStudentsFilteredPaginated(filters);

            assertThat(result).isNotNull();
            assertThat(result.getData()).hasSize(1);
            verify(studentRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}
