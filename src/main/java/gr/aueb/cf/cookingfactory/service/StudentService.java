package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.core.filters.StudentFilters;
import gr.aueb.cf.cookingfactory.core.specifications.StudentSpecification;
import gr.aueb.cf.cookingfactory.dto.StudentInsertDTO;
import gr.aueb.cf.cookingfactory.dto.StudentReadOnlyDTO;
import gr.aueb.cf.cookingfactory.dto.StudentUpdateDTO;
import gr.aueb.cf.cookingfactory.mapper.Mapper;
import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Student;
import gr.aueb.cf.cookingfactory.repository.CourseRepository;
import gr.aueb.cf.cookingfactory.repository.StudentRepository;
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
public class StudentService implements IStudentService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final Mapper mapper;

    //Save (add) a new student

    @Override
    @Transactional(rollbackOn = Exception.class)
    public StudentReadOnlyDTO saveStudent(StudentInsertDTO studentInsertDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException {

        if (studentRepository.findByVat(studentInsertDTO.vat()).isPresent()) {
            throw new AppObjectAlreadyExistsException("VAT", "Student with VAT number " + studentInsertDTO.vat() + " already exists");
        }

        if (studentRepository.findByIdentityNumber(studentInsertDTO.identityNumber()).isPresent()) {
            throw new AppObjectAlreadyExistsException("IdentityNumber", "Student with identity number " + studentInsertDTO.identityNumber() + " already exists");
        }

        Student student = mapper.mapToStudentEntity(studentInsertDTO);
        assignCoursesToStudent(student, studentInsertDTO.courseIds());

        Student savedStudent = studentRepository.save(student);

        log.info("Student with VAT number={} saved.", studentInsertDTO.vat());
        return mapper.mapToStudentReadOnlyDTO(savedStudent);
    }

    // Update an existing student

    @Override
    @Transactional(rollbackOn = Exception.class)
    public StudentReadOnlyDTO updateStudent(StudentUpdateDTO studentUpdateDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException {

        Student existingStudent = studentRepository.findById(studentUpdateDTO.id())
                .orElseThrow(() -> new AppObjectNotFoundException("Student", "Student with id " + studentUpdateDTO.id() + " not found"));

        if (!existingStudent.getVat().equals(studentUpdateDTO.vat()) &&
                studentRepository.findByVat(studentUpdateDTO.vat()).isPresent()) {
            throw new AppObjectAlreadyExistsException("Student", "Student with VAT number " + studentUpdateDTO.vat() + " already exists");
        }

        if (!existingStudent.getIdentityNumber().equals(studentUpdateDTO.identityNumber()) &&
                studentRepository.findByIdentityNumber(studentUpdateDTO.identityNumber()).isPresent()) {
            throw new AppObjectAlreadyExistsException("Student", "Student with identity number " + studentUpdateDTO.identityNumber() + " already exists");
        }

        Student studentToUpdate = mapper.mapToStudentEntity(studentUpdateDTO);
        assignCoursesToStudent(studentToUpdate, studentUpdateDTO.courseIds());

        Student updatedStudent = studentRepository.save(studentToUpdate);

        log.info("Student with id={} updated.", studentUpdateDTO.id());
        return mapper.mapToStudentReadOnlyDTO(updatedStudent);
    }

    // Get a student by uuid

    @Override
    @Transactional
    public StudentReadOnlyDTO getOneStudent(String uuid) throws AppObjectNotFoundException {
        return studentRepository
                .findByUuid(uuid)
                .map(mapper::mapToStudentReadOnlyDTO)
                .orElseThrow(() ->
                        new AppObjectNotFoundException("Student", "Student with uuid:" + uuid + " not found"));
    }

    // Get all students listed

    @Override
    @Transactional
    public List<StudentReadOnlyDTO> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(mapper::mapToStudentReadOnlyDTO)
                .collect(Collectors.toList());
    }

    // Get all students paginated, sorted by id

    @Override
    @Transactional
    public Paginated<StudentReadOnlyDTO> getPaginatedStudents(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());
        log.debug("Paginated students were returned successfully with page={} and size={}", page, size);
        var paginatedStudents = studentRepository.findAll(pageable);
        return Paginated.fromPage(paginatedStudents.map(mapper::mapToStudentReadOnlyDTO));
    }

    @Override
    @Transactional
    public Paginated<StudentReadOnlyDTO> getStudentsFilteredPaginated(StudentFilters studentFilters) {
        var filtered = studentRepository.findAll(getSpecsFromFilters(studentFilters), studentFilters.getPageable());
        log.debug("Filtered and paginated students were returned successfully with page={} and size={}", studentFilters.getPage(),
                studentFilters.getPageSize());
        return Paginated.fromPage(filtered.map(mapper::mapToStudentReadOnlyDTO));
    }

    private Specification<Student> getSpecsFromFilters(StudentFilters studentFilters) {
        return StudentSpecification.studentStringLastnameLike(studentFilters.getLastname())
                .and(StudentSpecification.studentDateOfBirthIs(studentFilters.getDateOfBirth()))
                .and(StudentSpecification.studentCourseIdIs(studentFilters.getCourseId()))
                .and(StudentSpecification.studentInstructorUuidIs(studentFilters.getInstructorUuid()));
    }

    private void assignCoursesToStudent(Student student, List<Long> courseIds) throws AppObjectNotFoundException {
        if (courseIds != null) {
            for (Long courseId : courseIds) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new AppObjectNotFoundException("Course", "Course with id " + courseId + " not found"));
                student.addCourses(course);
            }
        }
    }
}




