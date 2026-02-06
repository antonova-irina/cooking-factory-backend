package gr.aueb.cf.cookingfactory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ContactDetailsUpdateDTO(
        @NotNull
        Long id,

        @NotEmpty(message = "City is required")
        String city,
        String street,
        String streetNumber,
        String postalCode,

        @NotEmpty(message = "Email is required")
        @Email(message = "invalid Email")
        String email,

        @NotEmpty(message = "Phone number is required")
        String phoneNumber
) {}
