package gr.aueb.cf.cookingfactory.service;


import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.CourseFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.core.specifications.CourseSpecification;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.mapper.Mapper;
import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.repository.CourseRepository;
import gr.aueb.cf.cookingfactory.repository.InstructorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final Mapper mapper;

    //Save (add) a new course

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CourseReadOnlyDTO saveCourse(CourseInsertDTO courseInsertDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException {

        if (courseRepository.findByName(courseInsertDTO.name()).isPresent()) {
            throw new AppObjectAlreadyExistsException("Course name", "Course with name " + courseInsertDTO.name() + " already exists");
        }

        Course course = mapper.mapToCourseEntity(courseInsertDTO);
        if (courseInsertDTO.instructorId() != null) {
            Instructor instructor = instructorRepository.findById(courseInsertDTO.instructorId())
                    .orElseThrow(() -> new AppObjectNotFoundException("Instructor", "Instructor with id " + courseInsertDTO.instructorId() + " not found"));
            course.setInstructor(instructor);
        }

        Course savedCourse = courseRepository.save(course);    // Exception

        log.info("Course with name={} saved.", courseInsertDTO.name());
        return mapper.mapToCourseReadOnlyDTO(savedCourse);
    }

    // Update an existing course

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CourseReadOnlyDTO updateCourse(CourseUpdateDTO courseUpdateDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException {

        Course existingCourse = courseRepository.findById(courseUpdateDTO.id())
                .orElseThrow(() -> new AppObjectNotFoundException("Course", "Course with id " + courseUpdateDTO.id() + " not found"));

        if (!existingCourse.getName().equals(courseUpdateDTO.name()) &&
                courseRepository.findByName(courseUpdateDTO.name()).isPresent()) {
            throw new AppObjectAlreadyExistsException("Course", "Course with name " + courseUpdateDTO.name() + " already exists");
        }

        existingCourse.setIsActive(courseUpdateDTO.isActive());
        existingCourse.setName(courseUpdateDTO.name());
        existingCourse.setDescription(courseUpdateDTO.description());
        if (courseUpdateDTO.instructorId() != null) {
            Instructor instructor = instructorRepository.findById(courseUpdateDTO.instructorId())
                    .orElseThrow(() -> new AppObjectNotFoundException("Instructor", "Instructor with id " + courseUpdateDTO.instructorId() + " not found"));
            existingCourse.setInstructor(instructor);
        } else {
            existingCourse.setInstructor(null);
        }

        Course updatedCourse = courseRepository.save(existingCourse);

        log.info("Course with id={} updated.", courseUpdateDTO.id());
        return mapper.mapToCourseReadOnlyDTO(updatedCourse);
    }

    // Get a course by id

    @Override
    @Transactional
    public CourseReadOnlyDTO getOneCourse(Long id) throws AppObjectNotFoundException {
        return courseRepository
                .findById(id)
                .map(mapper::mapToCourseReadOnlyDTO)
                .orElseThrow(() ->
                        new AppObjectNotFoundException("Course", "Course with id:" + id + " not found"));
    }

    // Get all courses listed

    @Override
    @Transactional
    public List<CourseReadOnlyDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(mapper::mapToCourseReadOnlyDTO)
                .collect(Collectors.toList());
    }

    // Get all courses paginated, sorted by id

    @Override
    @Transactional
    public Paginated<CourseReadOnlyDTO> getPaginatedCourses(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());
        log.debug("Paginated courses were returned successfully with page={} and size={}", page, size);
        var paginatedCourses = courseRepository.findAll(pageable);
        return Paginated.fromPage(paginatedCourses.map(mapper::mapToCourseReadOnlyDTO));
    }

    @Override
    @Transactional
    public Paginated<CourseReadOnlyDTO> getCoursesFilteredPaginated(CourseFilters courseFilters) {
        var filtered = courseRepository.findAll(getSpecsFromFilters(courseFilters), courseFilters.getPageable());
        log.debug("Filtered and paginated courses were returned successfully with page={} and size={}", courseFilters.getPage(),
                courseFilters.getPageSize());
        return Paginated.fromPage(filtered.map(mapper::mapToCourseReadOnlyDTO));
    }

    private Specification<Course> getSpecsFromFilters(CourseFilters courseFilters) {
        return CourseSpecification.courseStringNameLike(courseFilters.getName())
              .and(CourseSpecification.courseInstructorIdIs(courseFilters.getInstructorId()));
    }
}

