package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.CourseFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.dto.CourseInsertDTO;
import gr.aueb.cf.cookingfactory.dto.CourseReadOnlyDTO;
import gr.aueb.cf.cookingfactory.dto.CourseUpdateDTO;
import gr.aueb.cf.cookingfactory.mapper.Mapper;
import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.repository.CourseRepository;
import gr.aueb.cf.cookingfactory.repository.InstructorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private CourseReadOnlyDTO courseReadOnlyDTO;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setIsActive(true);
        course.setName("Fresh Pasta");
        course.setDescription("100% practical pasta cooking workshop, inspired by the best Italian recipes and techniques.");

        courseReadOnlyDTO = new CourseReadOnlyDTO(1L, true, "Fresh Pasta", "100% practical pasta cooking workshop, inspired by the best Italian recipes and techniques.", null);

        instructor = new Instructor();
        instructor.setId(10L);
    }

    @Nested
    @DisplayName("saveCourse")
    class SaveCourse {

        @Test
        @DisplayName("saves course when name is unique and no instructor")
        void saveCourse_success_withoutInstructor() throws Exception {
            CourseInsertDTO insertDTO = CourseInsertDTO.builder()
                    .isActive(true)
                    .name("Greek Cuisine")
                    .description("Basic techniques of Greek traditional cooking and Mediterranean diet.")
                    .instructorId(null)
                    .build();

            when(courseRepository.findByName("Greek Cuisine")).thenReturn(Optional.empty());
            when(mapper.mapToCourseEntity(insertDTO)).thenReturn(new Course());
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
                Course c = inv.getArgument(0);
                c.setId(2L);
                return c;
            });
            when(mapper.mapToCourseReadOnlyDTO(any(Course.class))).thenReturn(
                    new CourseReadOnlyDTO(2L, true, "Greek Cuisine", "Basic techniques of Greek traditional cooking and Mediterranean diet.", null));

            CourseReadOnlyDTO result = courseService.saveCourse(insertDTO);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.name()).isEqualTo("Greek Cuisine");
            verify(courseRepository).findByName("Greek Cuisine");
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("saves course with instructor when instructorId is provided")
        void saveCourse_success_withInstructor() throws Exception {
            CourseInsertDTO insertDTO = CourseInsertDTO.builder()
                    .isActive(true)
                    .name("Sweets and Pastries")
                    .description("Confectionery and Greek desserts baking workshop.")
                    .instructorId(10L)
                    .build();

            when(courseRepository.findByName("Sweets and Pastries")).thenReturn(Optional.empty());
            when(mapper.mapToCourseEntity(insertDTO)).thenReturn(new Course());
            when(instructorRepository.findById(10L)).thenReturn(Optional.of(instructor));
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
                Course c = inv.getArgument(0);
                c.setId(2L);
                return c;
            });
            when(mapper.mapToCourseReadOnlyDTO(any(Course.class))).thenReturn(
                    new CourseReadOnlyDTO(2L, true, "Sweets and Pastries", "Confectionery and Greek desserts baking workshop.", 10L));

            CourseReadOnlyDTO result = courseService.saveCourse(insertDTO);

            assertThat(result).isNotNull();
            assertThat(result.instructorId()).isEqualTo(10L);
            verify(instructorRepository).findById(10L);
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when course name already exists")
        void saveCourse_duplicateName_throwsException() {
            CourseInsertDTO insertDTO = CourseInsertDTO.builder()
                    .isActive(true)
                    .name("Fresh Pasta")
                    .description("Practical pasta cooking workshop.")
                    .build();

            when(courseRepository.findByName("Fresh Pasta")).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseService.saveCourse(insertDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("Fresh Pasta")
                    .hasMessageContaining("already exists");
            verify(courseRepository).findByName("Fresh Pasta");
            verify(courseRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when instructorId is invalid")
        void saveCourse_invalidInstructorId_throwsException() {
            CourseInsertDTO insertDTO = CourseInsertDTO.builder()
                    .isActive(true)
                    .name("Sushi and Sashimi")
                    .description("Japanese cooking and fish cutting techniques.")
                    .instructorId(999L)
                    .build();

            when(courseRepository.findByName("Sushi and Sashimi")).thenReturn(Optional.empty());
            when(mapper.mapToCourseEntity(insertDTO)).thenReturn(new Course());
            when(instructorRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.saveCourse(insertDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Instructor")
                    .hasMessageContaining("999");
            verify(instructorRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("updates course successfully")
        void updateCourse_success() throws Exception {
            CourseUpdateDTO updateDTO = CourseUpdateDTO.builder()
                    .id(1L)
                    .isActive(false)
                    .name("Mediterranean Cuisine")
                    .description("Traditional Mediterranean cooking with olive oil and fresh ingredients.")
                    .instructorId(null)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(courseRepository.findByName("Mediterranean Cuisine")).thenReturn(Optional.empty());
            when(courseRepository.save(any(Course.class))).thenReturn(course);
            when(mapper.mapToCourseReadOnlyDTO(any(Course.class))).thenReturn(
                    new CourseReadOnlyDTO(1L, false, "Mediterranean Cuisine", "Traditional Mediterranean cooking with olive oil and fresh ingredients.", null));

            CourseReadOnlyDTO result = courseService.updateCourse(updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Mediterranean Cuisine");
            assertThat(result.isActive()).isFalse();
            verify(courseRepository).findById(1L);
            verify(courseRepository).save(course);
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when course does not exist")
        void updateCourse_courseNotFound_throwsException() {
            CourseUpdateDTO updateDTO = CourseUpdateDTO.builder()
                    .id(999L)
                    .isActive(true)
                    .name("Pasta")
                    .description("Cooking workshop.")
                    .build();

            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(updateDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Course")
                    .hasMessageContaining("999");
            verify(courseRepository).findById(999L);
            verify(courseRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when new name is taken by another course")
        void updateCourse_duplicateName_throwsException() {
            CourseUpdateDTO updateDTO = CourseUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .name("Pasta from Scratch")
                    .description("Fresh pasta making workshop from the ground up.")
                    .build();
            Course otherCourse = new Course();
            otherCourse.setId(2L);
            otherCourse.setName("Pasta from Scratch");

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(courseRepository.findByName("Pasta from Scratch")).thenReturn(Optional.of(otherCourse));

            assertThatThrownBy(() -> courseService.updateCourse(updateDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("Pasta from Scratch");
            verify(courseRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("allows same name when updating same course")
        void updateCourse_sameNameSameCourse_success() throws Exception {
            CourseUpdateDTO updateDTO = CourseUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .name("Fresh Pasta")
                    .description("Updated pasta workshop description.")
                    .instructorId(null)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            // When name unchanged, findByName is not called (short-circuit); stub only if needed
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.mapToCourseReadOnlyDTO(any(Course.class))).thenReturn(courseReadOnlyDTO);

            CourseReadOnlyDTO result = courseService.updateCourse(updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(courseRepository).findById(1L);
            verify(courseRepository).save(any(Course.class));
            verify(mapper).mapToCourseReadOnlyDTO(any(Course.class));
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when instructorId is invalid")
        void updateCourse_invalidInstructorId_throwsException() {
            CourseUpdateDTO updateDTO = CourseUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .name("Greek Desserts")
                    .description("Greek sweets workshop.")
                    .instructorId(999L)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(courseRepository.findByName("Greek Desserts")).thenReturn(Optional.empty());
            when(instructorRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(updateDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Instructor")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("getOneCourse")
    class GetOneCourse {

        @Test
        @DisplayName("returns course when id exists")
        void getOneCourse_success() throws Exception {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(mapper.mapToCourseReadOnlyDTO(course)).thenReturn(courseReadOnlyDTO);

            CourseReadOnlyDTO result = courseService.getOneCourse(1L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Fresh Pasta");
            verify(courseRepository).findById(1L);
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when id does not exist")
        void getOneCourse_notFound_throwsException() {
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getOneCourse(999L))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Course")
                    .hasMessageContaining("999");
            verify(courseRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("getAllCourses")
    class GetAllCourses {

        @Test
        @DisplayName("returns empty list when no courses")
        void getAllCourses_emptyList() {
            when(courseRepository.findAll()).thenReturn(List.of());

            List<CourseReadOnlyDTO> result = courseService.getAllCourses();

            assertThat(result).isEmpty();
            verify(courseRepository).findAll();
        }

        @Test
        @DisplayName("returns all courses")
        void getAllCourses_returnsAll() {
            when(courseRepository.findAll()).thenReturn(List.of(course));
            when(mapper.mapToCourseReadOnlyDTO(course)).thenReturn(courseReadOnlyDTO);

            List<CourseReadOnlyDTO> result = courseService.getAllCourses();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Fresh Pasta");
            verify(courseRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getPaginatedCourses")
    class GetPaginatedCourses {

        @Test
        @DisplayName("returns paginated courses")
        void getPaginatedCourses_success() {
            Pageable pageable = PageRequest.of(0, 10);
            when(courseRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(course), pageable, 1));
            when(mapper.mapToCourseReadOnlyDTO(course)).thenReturn(courseReadOnlyDTO);

            Paginated<CourseReadOnlyDTO> result = courseService.getPaginatedCourses(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getData()).hasSize(1);
            assertThat(result.getCurrentPage()).isZero();
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(courseRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("returns empty page when no courses")
        void getPaginatedCourses_emptyPage() {
            Pageable pageable = PageRequest.of(1, 5);
            when(courseRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Paginated<CourseReadOnlyDTO> result = courseService.getPaginatedCourses(1, 5);

            assertThat(result.getData()).isEmpty();
            assertThat(result.getCurrentPage()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("getCoursesFilteredPaginated")
    class GetCoursesFilteredPaginated {

        @Test
        @DisplayName("returns filtered and paginated courses")
        void getCoursesFilteredPaginated_success() {
            CourseFilters filters = CourseFilters.builder()
                    .name("Pasta")
                    .build();
            filters.setPage(0);
            filters.setPageSize(10);

            when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(course)));
            when(mapper.mapToCourseReadOnlyDTO(course)).thenReturn(courseReadOnlyDTO);

            Paginated<CourseReadOnlyDTO> result = courseService.getCoursesFilteredPaginated(filters);

            assertThat(result).isNotNull();
            assertThat(result.getData()).hasSize(1);
            verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}
