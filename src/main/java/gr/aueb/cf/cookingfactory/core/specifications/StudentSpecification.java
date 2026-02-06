package gr.aueb.cf.cookingfactory.core.specifications;

import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.model.Student;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class StudentSpecification {


    public StudentSpecification() {

    }


    public static Specification<Student> studentStringLastnameLike(String value){
        return ((root, query, criteriaBuilder) -> {

            if(value == null || value.trim().isEmpty()){
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.like(root.get("lastname"), "%" + value + "%");
        });
    }


    public static Specification<Student> studentDateOfBirthIs(LocalDate dateOfBirth){
        return ((root, query, criteriaBuilder) -> {

            if(dateOfBirth == null){
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }

            return criteriaBuilder.equal(root.get("dateOfBirth"),dateOfBirth);
        });
    }


    // Specification for getting students attending the Course with definite ID.

    public static Specification<Student> studentCourseIdIs(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }

            Join<Student, Course> courseJoin = root.join("courses");

            return criteriaBuilder.equal(courseJoin.get("id"), courseId);
        };
    }


     // Specification for filtering students by their courses Instructor UUID.

    public static Specification<Student> studentInstructorUuidIs(String instructorUuid) {
        return (root, query, criteriaBuilder) -> {

            if (instructorUuid == null || instructorUuid.isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }

            Join<Student, Course> courseJoin = root.join("courses");
            Join<Course, Instructor> coachJoin = courseJoin.join("instructor");

            return criteriaBuilder.equal(coachJoin.get("uuid"), instructorUuid);
        };
    }
}

