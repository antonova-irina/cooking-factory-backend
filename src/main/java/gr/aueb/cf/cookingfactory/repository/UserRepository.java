package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findByVat(String vat);
}
