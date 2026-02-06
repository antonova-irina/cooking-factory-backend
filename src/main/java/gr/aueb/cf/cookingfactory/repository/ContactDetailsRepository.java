package gr.aueb.cf.cookingfactory.repository;

import gr.aueb.cf.cookingfactory.model.ContactDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContactDetailsRepository extends JpaRepository<ContactDetails, Long>, JpaSpecificationExecutor<ContactDetails> {
}
