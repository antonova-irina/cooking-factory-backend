package gr.aueb.cf.cookingfactory.dto;

import gr.aueb.cf.cookingfactory.core.enums.Role;
import lombok.Builder;

@Builder
public record UserReadOnlyDTO(
    Long id,
    String username,
    Role role,
    String vat
) {}
