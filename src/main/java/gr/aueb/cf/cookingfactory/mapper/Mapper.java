package gr.aueb.cf.cookingfactory.mapper;

import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final PasswordEncoder passwordEncoder;

    public StudentReadOnlyDTO mapToStudentReadOnlyDTO(Student student) {

        ContactDetailsReadOnlyDTO contactDetailsReadOnlyDTO = new ContactDetailsReadOnlyDTO(student.getContactDetails().getId(),
                student.getContactDetails().getCity(), student.getContactDetails().getStreet(), student.getContactDetails().getStreetNumber(), student.getContactDetails().getPostalCode(),
                student.getContactDetails().getEmail(), student.getContactDetails().getPhoneNumber());

        List<Long> courseIds = student.hasCourses()
                ? student.getAllCourses().stream().map(Course::getId).collect(Collectors.toList())
                : null;

        return new StudentReadOnlyDTO(student.getId(), student.getUuid(),
                student.getIsActive(), student.getFirstname(), student.getLastname(), student.getDateOfBirth(), student.getVat(),
                student.getIdentityNumber(), student.getGender(), contactDetailsReadOnlyDTO, courseIds);
    }

    public Student mapToStudentEntity(StudentInsertDTO dto) {
        Student student = new Student();
        student.setIsActive(dto.isActive());
        student.setFirstname(dto.firstname());
        student.setLastname(dto.lastname());
        student.setDateOfBirth(dto.dateOfBirth());
        student.setVat((dto.vat()));
        student.setIdentityNumber(dto.identityNumber());
        student.setGender(dto.gender());

        ContactDetailsInsertDTO contactDetailsInsertDTO = dto.contactDetailsInsertDTO();
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setCity(contactDetailsInsertDTO.city());
        contactDetails.setStreet(contactDetailsInsertDTO.street());
        contactDetails.setStreetNumber(contactDetailsInsertDTO.streetNumber());
        contactDetails.setPostalCode(contactDetailsInsertDTO.postalCode());
        contactDetails.setEmail(contactDetailsInsertDTO.email());
        contactDetails.setPhoneNumber(contactDetailsInsertDTO.phoneNumber());
        student.setContactDetails(contactDetails);// Set ContactDetails entity to Student

        return student;
    }

    public Student mapToStudentEntity(StudentUpdateDTO dto) {
        Student student = new Student();
        student.setId(dto.id());
        student.setUuid(dto.uuid());
        student.setIsActive(dto.isActive());
        student.setFirstname(dto.firstname());
        student.setLastname(dto.lastname());
        student.setDateOfBirth(dto.dateOfBirth());
        student.setVat((dto.vat()));
        student.setIdentityNumber(dto.identityNumber());
        student.setGender(dto.gender());

        ContactDetailsUpdateDTO contactDetailsUpdateDTO = dto.contactDetailsUpdateDTO();
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setId(contactDetailsUpdateDTO.id());
        contactDetails.setCity(contactDetailsUpdateDTO.city());
        contactDetails.setStreet(contactDetailsUpdateDTO.street());
        contactDetails.setStreetNumber(contactDetailsUpdateDTO.streetNumber());
        contactDetails.setPostalCode(contactDetailsUpdateDTO.postalCode());
        contactDetails.setEmail(contactDetailsUpdateDTO.email());
        contactDetails.setPhoneNumber(contactDetailsUpdateDTO.phoneNumber());
        student.setContactDetails(contactDetails);// Set ContactDetails entity to Student

        return student;
    }

    public InstructorReadOnlyDTO mapToInstructorReadOnlyDTO(Instructor instructor) {

        UserReadOnlyDTO userReadOnlyDTO = new UserReadOnlyDTO(instructor.getUser().getId(), instructor.getUser().getUsername(),
                instructor.getUser().getRole(), instructor.getUser().getVat());

        ContactDetailsReadOnlyDTO contactDetailsReadOnlyDTO = new ContactDetailsReadOnlyDTO(instructor.getContactDetails().getId(),
                instructor.getContactDetails().getCity(), instructor.getContactDetails().getStreet(), instructor.getContactDetails().getStreetNumber(), instructor.getContactDetails().getPostalCode(),
                instructor.getContactDetails().getEmail(), instructor.getContactDetails().getPhoneNumber());

        return new InstructorReadOnlyDTO(instructor.getId(), instructor.getUuid(), instructor.getIsActive(), instructor.getFirstname(), instructor.getLastname(),
                instructor.getIdentityNumber(), instructor.getGender(), userReadOnlyDTO, contactDetailsReadOnlyDTO);
    }

    public Instructor mapToInstructorEntity(InstructorInsertDTO dto) {
        Instructor instructor = new Instructor();
        instructor.setIsActive(dto.isActive());
        instructor.setFirstname(dto.firstname());
        instructor.setLastname(dto.lastname());
        instructor.setIdentityNumber(dto.identityNumber());
        instructor.setGender(dto.gender());

        UserInsertDTO userInsertDTO = dto.userInsertDTO();
        User user = new User();
        user.setIsActive(userInsertDTO.isActive());
        user.setUsername(userInsertDTO.username());
        user.setPassword(passwordEncoder.encode(userInsertDTO.password()));
        user.setRole(userInsertDTO.role());
        user.setVat(userInsertDTO.vat());
        instructor.setUser(user);

        ContactDetailsInsertDTO contactDetailsInsertDTO = dto.contactDetailsInsertDTO();
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setCity(contactDetailsInsertDTO.city());
        contactDetails.setStreet(contactDetailsInsertDTO.street());
        contactDetails.setStreetNumber(contactDetailsInsertDTO.streetNumber());
        contactDetails.setPostalCode(contactDetailsInsertDTO.postalCode());
        contactDetails.setEmail(contactDetailsInsertDTO.email());
        contactDetails.setPhoneNumber(contactDetailsInsertDTO.phoneNumber());
        instructor.setContactDetails(contactDetails);// Set ContactDetails entity to Instructor

        return instructor;
    }

    public Instructor mapToInstructorEntity(InstructorUpdateDTO dto, String existingPassword) {
        Instructor instructor = new Instructor();
        instructor.setId(dto.id());
        instructor.setUuid(dto.uuid());
        instructor.setIsActive(dto.isActive());
        instructor.setFirstname(dto.firstname());
        instructor.setLastname(dto.lastname());
        instructor.setIdentityNumber(dto.identityNumber());
        instructor.setGender(dto.gender());

        UserUpdateDTO userUpdateDTO = dto.userUpdateDTO();
        User user = new User();
        user.setId(userUpdateDTO.id());
        user.setIsActive(userUpdateDTO.isActive());
        user.setUsername(userUpdateDTO.username());
        if (userUpdateDTO.password() != null && !userUpdateDTO.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(userUpdateDTO.password()));
        } else {
            user.setPassword(existingPassword);
        }
        user.setRole(userUpdateDTO.role());
        user.setVat(userUpdateDTO.vat());
        instructor.setUser(user);

        ContactDetailsUpdateDTO contactDetailsUpdateDTO = dto.contactDetailsUpdateDTO();
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setId(contactDetailsUpdateDTO.id());
        contactDetails.setCity(contactDetailsUpdateDTO.city());
        contactDetails.setStreet(contactDetailsUpdateDTO.street());
        contactDetails.setStreetNumber(contactDetailsUpdateDTO.streetNumber());
        contactDetails.setPostalCode(contactDetailsUpdateDTO.postalCode());
        contactDetails.setEmail(contactDetailsUpdateDTO.email());
        contactDetails.setPhoneNumber(contactDetailsUpdateDTO.phoneNumber());
        instructor.setContactDetails(contactDetails);

        return instructor;
    }

    public CourseReadOnlyDTO mapToCourseReadOnlyDTO(Course course) {
        Long instructorId = course.getInstructor() != null ? course.getInstructor().getId() : null;
        return new CourseReadOnlyDTO(course.getId(), course.getIsActive(), course.getName(),
                course.getDescription(), instructorId);
    }

    public Course mapToCourseEntity(CourseInsertDTO dto) {
        Course course = new Course();
        course.setIsActive(dto.isActive());
        course.setName(dto.name());
        course.setDescription(dto.description());

        return course;
    }

    public Course mapToCourseEntity(CourseUpdateDTO dto) {
        Course course = new Course();
        course.setId(dto.id());
        course.setIsActive(dto.isActive());
        course.setName(dto.name());
        course.setDescription(dto.description());

        return course;
    }
}
