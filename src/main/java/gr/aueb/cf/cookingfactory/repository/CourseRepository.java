package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>,
        JpaSpecificationExecutor<Course> {

    Optional<Course> findByName(String name);
    List<Course> findByInstructor_Id(Long instructorId);
}