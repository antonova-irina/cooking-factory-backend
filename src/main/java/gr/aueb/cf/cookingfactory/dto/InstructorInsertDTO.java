package gr.aueb.cf.cookingfactory.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import gr.aueb.cf.cookingfactory.core.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record InstructorInsertDTO(
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
        @JsonAlias("user")
        UserInsertDTO userInsertDTO,

        @Valid
        @NotNull(message = "Contact details are required")
        @JsonAlias("contactDetails")
        ContactDetailsInsertDTO contactDetailsInsertDTO
) {}
