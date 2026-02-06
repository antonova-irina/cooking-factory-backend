package gr.aueb.cf.cookingfactory.dto;

import gr.aueb.cf.cookingfactory.core.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserUpdateDTO(
        @NotNull
        Long id,

        @NotNull(message = "isActive field is required")
        Boolean isActive,

        @NotEmpty(message = "Username is required")
        String username,

        @Pattern(regexp = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\\d)(?=.*?[@#$!%&*]).{8,}$",
                message = "Invalid Password")
        String password,

        @NotNull(message = "Role is required")
        Role role,

        @NotEmpty(message = "VAT number is required")
        @Pattern(regexp =  "\\d{9}", message = "VAT must be a 9-digit number")
        String vat
) {}
