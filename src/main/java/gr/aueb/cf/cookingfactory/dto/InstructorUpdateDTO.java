package gr.aueb.cf.cookingfactory.dto;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record InstructorUpdateDTO(
        @NotNull(message = "Id field is required")
        Long id,

        @NotNull(message = "Uuid field is required")
        String uuid,

        @NotNull(message = "isActive field is required")
        Boolean isActive,

        @NotEmpty(message = "First name is required")
        String firstname,

        @NotEmpty(message = "Last name is required")
        String lastname,

        @NotEmpty(message = "Identity number is required")
        String identityNumber,

        @NotNull(message = "Gender is required")
        Gender gender,

        @Valid
        @NotNull(message = "User details are required")
        UserUpdateDTO userUpdateDTO,

        @Valid
        @NotNull(message = "Contact details are required")
        ContactDetailsUpdateDTO contactDetailsUpdateDTO
) {}
