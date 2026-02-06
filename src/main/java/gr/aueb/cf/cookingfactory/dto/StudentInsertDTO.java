package gr.aueb.cf.cookingfactory.dto;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StudentInsertDTO(
        @NotNull(message = "isActive field is required")
        Boolean isActive,

        @NotEmpty(message = "Firstname is required")
        String firstname,

        @NotEmpty(message = "Lastname is required")
        String lastname,

        @NotNull(message = "DateOf Birth is required")
        LocalDate dateOfBirth,

        @NotEmpty(message = "VAT number is required")
        @Pattern(regexp =  "\\d{9}", message = "VAT must be a 9-digit number")
        String vat,

        @NotEmpty(message = "Identity number is required")
        String identityNumber,

        @NotNull(message = "Gender is required")
        Gender gender,

        ContactDetailsInsertDTO contactDetailsInsertDTO,

        List<Long> courseIds
) {}
