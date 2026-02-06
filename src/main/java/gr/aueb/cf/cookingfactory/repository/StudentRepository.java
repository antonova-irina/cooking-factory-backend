package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>,
        JpaSpecificationExecutor<Student> {

    boolean existsByLastname(String lastname);
    boolean existsByVat(String vat);
    boolean existsByIdentityNumber(String identityNumber);
    Optional<Student> findByUuid(String uuid);
    Optional<Student> findByVat(String vat);
    Optional<Student> findByIdentityNumber(String identityNumber);
}
