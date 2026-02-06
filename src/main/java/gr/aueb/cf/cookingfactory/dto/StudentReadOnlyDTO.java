package gr.aueb.cf.cookingfactory.dto;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StudentReadOnlyDTO(
        Long id,
        String uuid,
        Boolean isActive,
        String firstname,
        String lastname,
        LocalDate dateOfBirth,
        String vat,
        String identityNumber,
        Gender gender,
        ContactDetailsReadOnlyDTO contactDetailsReadOnlyDTO,
        List<Long> courseIds
) {}
