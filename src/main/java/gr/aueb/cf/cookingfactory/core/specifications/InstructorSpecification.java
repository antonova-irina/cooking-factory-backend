package gr.aueb.cf.cookingfactory.core.specifications;

import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.model.Student;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class InstructorSpecification {

    public InstructorSpecification() {

    }

    public static Specification<Instructor> instructorStringLastnameLike(String value){
        return ((root, query, criteriaBuilder) -> {

            if(value == null || value.trim().isEmpty()){
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.like(root.get("lastname"), "%" + value + "%");
        });
    }

    public static Specification<Instructor> instructorUuidIs(String uuid){
        return ((root, query, criteriaBuilder) -> {

            if(uuid == null || uuid.trim().isEmpty()){
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }

            return criteriaBuilder.equal(root.get("uuid"),uuid);
        });
    }
}
