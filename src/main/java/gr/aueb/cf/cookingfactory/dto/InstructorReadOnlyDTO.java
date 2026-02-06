package gr.aueb.cf.cookingfactory.dto;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import lombok.Builder;

@Builder
public record InstructorReadOnlyDTO(
        Long id,
        String uuid,
        Boolean isActive,
        String firstname,
        String lastname,
        String identityNumber,
        Gender gender,
        UserReadOnlyDTO userReadOnlyDTO,
        ContactDetailsReadOnlyDTO contactDetailsReadOnlyDTO
) {}

