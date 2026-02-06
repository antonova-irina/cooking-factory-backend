package gr.aueb.cf.cookingfactory.core.specifications;

import gr.aueb.cf.cookingfactory.model.Course;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.model.Student;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public CourseSpecification() {

    }

    public static Specification<Course> courseStringNameLike(String value){
        return ((root, query, criteriaBuilder) -> {

            if(value == null || value.trim().isEmpty()){
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.like(root.get("name"), "%" + value + "%");
        });
    }

    // Specification for filtering courses by their Instructor ID.

    public static Specification<Course> courseInstructorIdIs(Long instructorId) {
        return (root, query, criteriaBuilder) -> {

            if (instructorId == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Course, Instructor> instructorJoin = root.join("instructor");

            return criteriaBuilder.equal(root.get("instructor").get("id"), instructorId);
        };
    }

}
