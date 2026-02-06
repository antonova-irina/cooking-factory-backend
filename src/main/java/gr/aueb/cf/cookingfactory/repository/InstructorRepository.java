package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long>,
        JpaSpecificationExecutor<Instructor> {

    boolean existsByIdentityNumber(String identityNumber);
    Optional<Instructor> findByUuid(String uuid);
    Optional<Instructor> findByIdentityNumber(String identityNumber);
    Optional<Instructor> findByUser_Id(Long userId);
}
