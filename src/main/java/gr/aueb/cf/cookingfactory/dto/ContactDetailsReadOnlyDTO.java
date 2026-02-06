package gr.aueb.cf.cookingfactory.dto;

import lombok.Builder;

@Builder
public record ContactDetailsReadOnlyDTO(
        Long id,
        String city,
        String street,
        String streetNumber,
        String postalCode,
        String email,
        String phoneNumber
) {}
