package gr.aueb.cf.cookingfactory.dto;

public record AuthenticationResponseDTO(
        String access_token,
        String vat,
        String firstname,
        String lastname,
        String role
) {}
