package gr.aueb.cf.cookingfactory.dto;

import jakarta.validation.constraints.NotNull;

public record AuthenticationRequestDTO(
        @NotNull
        String username,

        @NotNull
        String password
) {}
